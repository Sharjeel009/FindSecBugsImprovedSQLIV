/**
 * Find Security Bugs
 * BetterSqlInjection Detector class used in SQLInjectionDetector
 */

package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.FindSecBugsGlobalConfig;
import com.h3xstream.findsecbugs.common.matcher.TaintTypes;
import com.h3xstream.findsecbugs.injection.*;
import com.h3xstream.findsecbugs.taintanalysis.Taint;
import com.h3xstream.findsecbugs.taintanalysis.TaintAnalysis;
import com.h3xstream.findsecbugs.taintanalysis.TaintDataflow;
import com.h3xstream.findsecbugs.taintanalysis.TaintFrame;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.CFG;

import edu.umd.cs.findbugs.ba.*;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.util.ClassName;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.io.*;
import java.util.*;


import com.h3xstream.findsecbugs.common.*;

public class BetterSqlInjectionDetector extends AbstractInjectionDetector implements Detector {

    private final Map<String, InjectionPoint> injectionMap = new HashMap<String, InjectionPoint>();

    private static final SinksLoader SINKS_LOADER = new SinksLoader();

    protected final Map<String, Set<InjectionSink>> injectionSinks = new HashMap<String, Set<InjectionSink>>();
    private final Map<MethodAndSink, Taint> sinkTaints = new HashMap<MethodAndSink, Taint>();
    private int parameterStackSize;
    DFG oDFG = new DFG(); // todo: temp moved here

    BugReporter bugReporter;
    boolean constant_int_exist = false;
    Number val = 0;


    private List<IntInfo> int_list = new ArrayList<>();
    String str_out_dfg = "";

    private ConstantPoolGen cpg;




    public BetterSqlInjectionDetector(BugReporter bugReporter)
    {

        super(bugReporter);
        this.bugReporter = bugReporter;
        loadConfiguredSinks("sql-hibernate.txt", "SQL_INJECTION_HIBERNATE");
        loadConfiguredSinks("sql-jdo.txt", "SQL_INJECTION_JDO");
        loadConfiguredSinks("sql-jpa.txt", "SQL_INJECTION_JPA");
        loadConfiguredSinks("sql-jdbc.txt", "SQL_INJECTION_JDBC");
        loadConfiguredSinks("sql-spring.txt", "SQL_INJECTION_SPRING_JDBC");
        loadConfiguredSinks("sql-scala-slick.txt", "SCALA_SQL_INJECTION_SLICK");
        loadConfiguredSinks("sql-scala-anorm.txt", "SCALA_SQL_INJECTION_ANORM");
        loadConfiguredSinks("sql-turbine.txt", "SQL_INJECTION_TURBINE");
        //TODO : Add org.springframework.jdbc.core.simple.SimpleJdbcTemplate (Spring < 3.2.1)
    }



    protected void loadConfiguredSinks(String filename, String bugType) {
        SINKS_LOADER.loadConfiguredSinks(filename, bugType, new SinksLoader.InjectionPointReceiver() {
            @Override
            public void receiveInjectionPoint(String fullMethodName, InjectionPoint injectionPoint) {
                addParsedInjectionPoint(fullMethodName, injectionPoint);
            }
        });
    }

    protected void addParsedInjectionPoint(String fullMethodName, InjectionPoint injectionPoint) {
        assert !injectionMap.containsKey(fullMethodName) : "Duplicate method name loaded: " + fullMethodName;
        injectionMap.put(fullMethodName, injectionPoint);
    }


    @Override
    public void visitClassContext(ClassContext classContext) {
    }


    public boolean visitBetterClassContext(ClassContext classContext) {

        boolean ret_val = true;

        if(classContext != null) {

            List<Method> methodsInCallOrder = classContext.getMethodsInCallOrder();
            cpg = classContext.getConstantPoolGen();
            JavaClass obj = classContext.getJavaClass();

            System.out.println("--======= New Class Context ========--");
            System.out.println("ClassName : " + obj.getClassName());

            boolean isReturningClass = false;

            if(obj.getClassName().endsWith("Test"))
                isReturningClass = true;

            System.out.println("Returning class is : " + isReturningClass);
            analyzeClass(classContext, isReturningClass);


        }
        return ret_val;
    }

    private boolean analyzeClass(ClassContext classContext, boolean isReturningClass) {

        boolean ret_val = true;
        List<Method> methodsInCallOrder = classContext.getMethodsInCallOrder();

        try {

            boolean is_webservlet = false;
            JavaClass javaClass = classContext.getJavaClass();
            for (AnnotationEntry annotationEntry : javaClass.getAnnotationEntries()) {
                if (annotationEntry.getAnnotationType().equals("Ljavax/servlet/annotation/WebServlet;"))
                    is_webservlet = true;

            }
            ;

            for (int j = 0; j < methodsInCallOrder.size(); j++) {

                int_list = new ArrayList<>();
                Method method = methodsInCallOrder.get(j);

                // <init> methods are to be ignored
                if (!method.getName().contains("<init>")) //method.getName().startsWith("doPost")) // || (!method.getName().startsWith("<"))) //if(!method.getName().startsWith("<"))
                {
                    CFG cfg = classContext.getCFG(method);
                    final Iterator<Location> locationIterator = cfg.locationIterator();

                    String str_instructions = "";

                    while (locationIterator.hasNext()) {
                        Location next = locationIterator.next();
                        InstructionHandle handle = next.getHandle();
                        Instruction instruction = handle.getInstruction();

                        str_instructions += instruction.toString() + "\n\r";

                    }

                    PrintWriter pw_all_instructions = new PrintWriter(Settings.output_Path + "all_instructions.txt");
                    pw_all_instructions.println(str_instructions);
                    pw_all_instructions.close();

                    if (doesHaveTaintSink(method, classContext) || true) {
                        List<VarInfo> oVarList = get_var_list(classContext, method, is_webservlet);
                        String str_vars = "";

                        LineNumberTable lineNumberTable = method.getLineNumberTable();
                        MethodGen methodGen = classContext.getMethodGen(method);


                        LocalVariableGen[] localVariables = methodGen.getLocalVariables();
                        LocalVariableTable localVariableOrg = methodGen.getLocalVariableTable(cpg);

                        // localVariables lists are added
                        for (int i = 0; i < localVariables.length; i++) {
                            LocalVariableGen lvg = localVariables[i];

                            // for debugging
                            System.out.println("lvg Index : " + lvg.getIndex() + " :: lvg Type : " + lvg.getType() + " :: lvg Name : " + lvg.getName());
                        }

                        int numParameters = 0;
                        for (int u = 0; u < oVarList.size(); u++) {
                             if(oVarList.get(u).getIsParameter())
                                 numParameters++;
                        }

                        List<VarInfo> oTempVarList = new ArrayList<>();

                        for (int p = 0; p < localVariables.length; p++) {
                            LocalVariableGen lvg = localVariables[p];
                            VarInfo varInfo = null, newvarInfo = null;

                            for (int u = 0; u < oVarList.size(); u++) {
                                VarInfo tempVarInfo = oVarList.get(u);
                                if (tempVarInfo.getIndex() == lvg.getIndex())
                                {
                                    varInfo = tempVarInfo;
                                    break;
                                }
                            }

                            if (varInfo != null)
                            {
                                if (lvg.getType() instanceof ObjectType) {
                                    varInfo.setType(((ObjectType) lvg.getType()).getClassName());
                                } else {
                                    varInfo.setType(lvg.getType().toString());
                                }
                                varInfo.setName(lvg.getName());
                            }

                            newvarInfo = new VarInfo(varInfo);
                            newvarInfo.setIndex(oTempVarList.size());

                            if(!newvarInfo.isIs_parameter()) {
                                newvarInfo.setUltimateTaintType(TaintTypes.UNKNOWN);
                                System.out.println("Var at index : " + newvarInfo.getIndex() +  " is set to " + "UNKNOWN");
                            }

                            oTempVarList.add(newvarInfo);

                        }

                        oVarList = oTempVarList;


                        // --------------------------------------------------- Variable List ---------------------------------


                        System.out.println("--=-=-=-=-=-=-==-=-= Variable list -=-=-=-=-=-=-=-=-=-==-==--");

                        boolean twoVarsSameNameExist = false;

                        for (int f = 0; f < oVarList.size(); f++) {
                            VarInfo varInfo = oVarList.get(f);
                            System.out.println("INDEX : " + varInfo.getIndex() + " :: TYPE : " + varInfo.getType() + " :: NAME : " + varInfo.getName());

                            for(int u = 0; u < oVarList.size(); u++)
                            {
                                VarInfo varInfo2 = oVarList.get(u);
                                if(u != f)
                                {
                                    if(varInfo.getName().equals(varInfo2.getName()))
                                    {
                                        System.out.println("Var is of same names");
                                        twoVarsSameNameExist = true;
                                    }
                                }
                            }
                        }


                        System.out.println("--=-=-=-=-=-=-==-=-= End Variable list -=-=-=-=-=-=-=-=-=-==-==--");


                        PrintWriter out_vars = new PrintWriter(Settings.output_Path + "variables_" + method.getName() + ".txt");
                        out_vars.println(str_vars);
                        out_vars.close();


                        // --------------------------------------------------- Create DFG ---------------------------------

                        System.out.println("Method : " + method.getName());
                        DFGFactory oFactory = new DFGFactory(classContext, cpg);
                        DFG oDFG = oFactory.createDFG(method);
                        //List<BlockVar> oListBlockVar = oFactory.oListBlockVars;

                        PrintWriter out_dfg = new PrintWriter(Settings.output_Path + "dfg_" + method.getName() + ".txt");
                        List<BlockVar> oListBlockVars = oFactory.getoListBlockVars();

                        String str_val = "";

                        for (BlockVar bv : oListBlockVars) {
                            str_val += bv.toString() + "\n\r";
                        }


                        PrintWriter bv = new PrintWriter(Settings.output_Path + "BlockVar_" + method.getName() + ".txt");
                        bv.println(str_val);
                        bv.close();


                        out_dfg.println(oDFG.toString());
                        out_dfg.close();

                        // --------------------------------------------------- Taint Graph ---------------------------------

                        TGFactory oTaintFactory = new TGFactory(classContext, cpg, oListBlockVars, isReturningClass);
                        TaintGraph oGraph = oTaintFactory.createTaintGraph(method, oVarList, lineNumberTable, localVariableOrg, twoVarsSameNameExist);

                        // Block access information - that contains whether to always or never access block
                        List<BlockAccessInfo> blockAccessInfos = oTaintFactory.getoListBlockAccessInfo();

                        // output taint graph to a file for debugging
                        PrintWriter out_taint = new PrintWriter( Settings.output_Path + "taintgraph_" + method.getName() + ".txt");
                        out_taint.println(oGraph.toString());
                        out_taint.close();


                        // --------------------------------------------------- Kripke Structure ---------------------------------

                        // create the Kripke Factory
                        KSFactory ks = new KSFactory();
                        Kripke kripke = ks.createKripke(oGraph, oVarList, blockAccessInfos, isReturningClass, numParameters);

                        PrintWriter out_taint_edges = new PrintWriter(Settings.output_Path + "out_taint_edges" + method.getName() + ".txt");
                        out_taint_edges.println(oGraph.toStringFromEdges());
                        out_taint_edges.close();


                        PrintWriter block_access = new PrintWriter(Settings.output_Path + "block_access_info" + method.getName() + ".txt");
                        String str_block_access = "";

                        for (BlockAccessInfo bai : blockAccessInfos) {
                            str_block_access += bai.getBlockID() + " : " + bai.getAccessInfo() + "\n\r";
                        }

                        block_access.println(str_block_access);
                        block_access.close();


                        PrintWriter uuu1 = new PrintWriter(Settings.output_Path + "taint_short" + method.getName() + ".txt");
                        uuu1.println(oGraph.toStringShort());
                        uuu1.close();


                        block_access.println(str_block_access);
                        block_access.close();



                        KSFactory ksfactory = new KSFactory();

                        Kripke kripke1 = ksfactory.createKripke(oGraph, oVarList, blockAccessInfos, isReturningClass, numParameters);
                        PrintWriter kpw = new PrintWriter(Settings.output_Path + method.getName() + ".txt");
                        kpw.println(kripke1.toString());
                        kpw.close();

                        // --------------------------------------------------- Model checking through NuSMV ---------------------------------

                        NuSMVFactory oNFactory = new NuSMVFactory();
                        String strNu = oNFactory.getNuSMVCode(kripke1);


                        String str = Settings.output_Path + "NuSMVCode_" + method.getName() + ".smv";

                        PrintWriter N_pw = new PrintWriter(str);
                        N_pw.println(strNu.toString());
                        N_pw.close();


                        Process process = Runtime.getRuntime().exec("NuSMV " + str);
                        InputStream stdin = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(stdin);
                        BufferedReader br = new BufferedReader(isr);

                        String line = "", str_output = "";
                        while ((line = br.readLine()) != null)
                            str_output += line; //System.out.println(line);

                        System.out.println(str_output);

                        if (str_output.contains("is true")) {
                            ret_val = false;
                        }
                        else
                        {
                            if (!isReturningClass) {
                                Iterator<Location> locationIterator1 = cfg.locationIterator();
                                Location next = locationIterator1.next();

                                InstructionHandle handle = next.getHandle();

                                InjectionSink injectionSink = new InjectionSink(this, "SQL_INJECTION_JDBC", Priorities.HIGH_PRIORITY,
                                        classContext, method, handle, method.getName());
                                bugReporter.reportBug(injectionSink.generateBugInstance(true));
                            }
                        }


                    }


                    PrintWriter out1 = new PrintWriter( "str_out_dfg_" + method.getName() + ".txt");

                    str_out_dfg += oDFG.toString();
                    out1.println(str_out_dfg);
                    out1.close();

                }
            }
        } catch (Exception e) {

            e.printStackTrace();

            try {
                //  output the exception into a file
                PrintWriter out5 = new PrintWriter( Settings.output_Path + "Exception.txt");
                out5.println(e.getMessage());
                out5.println(e.getLocalizedMessage());
                out5.println(e.getStackTrace().toString());

                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));

                out5.println(errors.toString());
                out5.close();
            } catch (Exception ex) {

            }
        }

        return ret_val;

    }

    private boolean doesHaveTaintSink(Method method, ClassContext classContext)
    {
        boolean ret_val = false;

        try
        {
            CFG cfg = classContext.getCFG(method);
            Iterator<BasicBlock> basicBlockIterator = cfg.blockIterator();
            while(basicBlockIterator.hasNext())
            {
                BasicBlock oBlock = basicBlockIterator.next();
                BasicBlock.InstructionIterator jfk = oBlock.instructionIterator();

                while (jfk.hasNext()) {
                    InstructionHandle handle = jfk.next();
                    Instruction ins = handle.getInstruction();
                    if(ins instanceof InvokeInstruction)
                    {
                        InvokeInstruction ii = (InvokeInstruction)ins;
                        String fullMethodName = getFullMethodName(ii, cpg);
                    }
                }
            }
        }
        catch(Exception ex)
        {

        }

        return ret_val;
    }

    private String getFullMethodName(InvokeInstruction invoke, ConstantPoolGen cpg)
    {
        return ClassName.toSlashedClassName(invoke.getReferenceType(cpg).toString())
                + "." + invoke.getMethodName(cpg) + invoke.getSignature(cpg);
    }

    @Override
    public void report() {

    }

    @Override
    protected InjectionPoint getInjectionPoint(InvokeInstruction invoke, ConstantPoolGen cpg, InstructionHandle handle) {
        return null;
    }

    public List<VarInfo> get_var_list(ClassContext classContext, Method method,boolean is_webservlet) throws CheckedAnalysisException {

        List<VarInfo> oVarList = new ArrayList<>();

        System.out.println("is_webservlet : " + is_webservlet);

        TaintDataflow dataflow = getTaintDataFlow(classContext, method);
        TaintAnalysis analysis = dataflow.getAnalysis();

        CFG cfg = classContext.getCFG(method);

        // get the variables
        BasicBlock entry_bb = cfg.getEntry(); // will contain the stuff : read DataFlow.java
        TaintFrame fact = analysis.getStartFact(entry_bb);


        fact.setValid();
        fact.clearStack();

        String strToWrite = "";

        this.parameterStackSize = analysis.getParameterStackSize();

        boolean inMainMethod = analysis.isInMainMethod();
        int numSlots = fact.getNumSlots();
        int numLocals = fact.getNumLocals();
        for (int i = 0; i < numSlots; ++i)
        {
            boolean is_parameter = false;

            Taint value = new Taint(Taint.State.UNKNOWN);
            if (i < numLocals)
            {
                if (i < parameterStackSize)
                {
                    is_parameter = true;

                    if(is_webservlet)
                    {
                        value = new Taint(Taint.State.TAINTED);
                    }
                    else
                    {

                        if (analysis.isTaintedByAnnotation(i - 1))
                        {
                            value = new Taint(Taint.State.TAINTED);
                        }

                        else if (inMainMethod)
                        {
                            if (FindSecBugsGlobalConfig.getInstance().isTaintedMainArgument())
                            {
                                value = new Taint(Taint.State.TAINTED);
                            }

                            else
                            {
                                value = new Taint(Taint.State.SAFE);
                            }
                        }

                        else
                        {
                            int stackOffset = parameterStackSize - i - 1;
                            value.addParameter(stackOffset);
                        }
                    }
                }
                value.setVariableIndex(i);
            }

            fact.setValue(i, value);
            int taint_type = value.isTainted() ? TaintTypes.TAINTED : TaintTypes.UN_TAINTED;

            VarInfo varInfo = new VarInfo(i, taint_type );
            varInfo.setIs_parameter(is_parameter);

            oVarList.add(varInfo);
        }

        System.out.println("oVarList.size() : " + oVarList.size());

        return oVarList;
    }
}
