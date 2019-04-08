package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.common.Kripke;
import com.h3xstream.findsecbugs.common.KripkeSigma;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NuSMVFactory {
    private int num_output_bits;

    public String getNuSMVCode(Kripke kripke) {

        String code = "";

        try {

            code += "MODULE main" + getNewLine(2);

            // VARS
            code += "VAR" + getNewLine(1);
            code += "state : " + getStateCode(kripke) + ";" + getNewLine(1);
            code += getOutputVarString(kripke);
            code += getNewLine(2);

            code += "IVAR" + getNewLine(1);

            List<String> inputSymbols = kripke.getInputSymbols();
            for(String str_input : inputSymbols)
            {
                code += str_input + " : boolean;" + getNewLine(1);
            }

            code += getNewLine(2);

            code += "ASSIGN" + getNewLine(1) + getInitCode(kripke);

            code += getNewLine(2);

            code += "next(state) := case" + getNewLine(1);
            List<String> states = kripke.getQ();
            //Map<String, List<String>> R = kripke.getR();

            Map<String, List<Integer>> L = kripke.getL();

            List<KripkeSigma> Sigma = kripke.getSigma();
            for(KripkeSigma CurrSigma : Sigma)
            {

                String s1 =  CurrSigma.getState();
                String s2 = CurrSigma.getNext_state();
                String input_symbol = CurrSigma.getInput_symbol();

                //code += "\t" + "state = " + "s" + s1 + " & " + input_symbol + " =  TRUE" + " : " + "s" + s2 + " ;" + getNewLine(1);
                code += "\t" + "state = " + "s" + s1 + " & " + input_symbol + " = TRUE " + " : " + "s" + s2 + " ;" + getNewLine(1);
            }


            code += "\t" + "TRUE : " + "sd;" + getNewLine(1);
            code += "esac;";


            code += getNewLine(2);

            for(int j = 0; j < num_output_bits; j++)
            {
                String var_name = "";
                if(j == 0)
                    var_name = "cb1";
                else if(j == 1)
                    var_name = "cb2";
                else
                {
                    int var_ind = (j - 2) / 2;
                    var_name = "v" + var_ind + "b";
                    if(j%2 == 0)    // first bit is divided by 2
                        var_name += "1";
                    else
                        var_name += "2";
                }

                System.out.println("-- " + var_name);

                code += "next(" + var_name + ") := case" + getNewLine(1);


                Sigma = kripke.getSigma();
                for(KripkeSigma CurrSigma : Sigma) {

                    String s1 =  CurrSigma.getState();
                    String s2 = CurrSigma.getNext_state();
                    String input_symbol = CurrSigma.getInput_symbol();

                    // why used L.get(s2).get(j)
                    //code += "\t" + "state = " + "s" + s1 + " & " + input_symbol + " = TRUE" + " : " + intToBool(L.get(s2).get(j)) + " ;" + getNewLine(1);
                    code += "\t" + "state = " + "s" + s1 + " & " + input_symbol + " = TRUE " + " : " + intToBool(L.get(s2).get(j)) + " ;" + getNewLine(1);
                }

                code += "TRUE : " + "FALSE" + ";" + getNewLine(1);
                code += "esac;";

                code += getNewLine(2);
            }

            code += getNewLine(2);


            code += "SPEC" + getNewLine(1); //LTL

            code += getLTLSPEC(kripke) + getNewLine(1);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return code;

    }

    // can be much better for example the condition can be translated taine_source & stuff
    private String getLTLSPEC(Kripke kripke)
    {
        String str_val = "";

        int var_ind = (num_output_bits - 2) / 2;


        for(int i = 0; i < num_output_bits; i++)
        {
            if(i > 1 && i % 2 == 0) // first bit is divided by 2
            {
                String first_bit = getVarNameByIndex(i);
                String second_bit = getVarNameByIndex(i + 1);

                String nan = "(cb1 = TRUE & cb2 = FALSE)";
                String taint = "(" + first_bit + " = FALSE & " + second_bit + " = TRUE)";
                String taint_sink = "(" + first_bit + " = TRUE & " + second_bit + " = TRUE)";
                String taint_kill = "(" + first_bit + " = TRUE & " + second_bit + " = FALSE)";

                //str_val += "G ( ( " + taint + " -> !" + taint_kill + ")" + " -> !" + taint_sink + ")";
                ////str_val  += "G (  " + taint  + "  -> (X(!F" + taint_sink + ") | X (!G(" + taint_kill + " -> X(!F" + taint_sink + "))))) ";
                //////str_val += "(!EF" + taint_sink +  " | !EG(" + taint + " -> EX((EF("   + taint_kill + " & " + nan + ") -> (EX(EF" + taint_sink + " & " + nan + "))))))";
                str_val += "(!EF" + taint_sink + "| !EF" + taint +  " | !EG(" + taint + " -> EX((EF("   + taint_kill  + ") -> (EX(EF" + taint_sink + "))))))";

                // only for the first bit the second bit is
                if(i != num_output_bits - 2)    // if its not the last statements -2 becoz ignoring odd bits
                {
                    str_val += " & ";
                }
            }
        }
        return str_val;
    }

    private String intToBool(Integer integer)
    {
        if(integer == 1)
            return "TRUE";
        else
            return "FALSE";
    }

    private String getOutputVarString(Kripke kripke)
    {
        String str  = "";

        try {
            Map<String, List<Integer>> L = kripke.getL();
            List<String> S = kripke.getQ();
            List<Integer> integers = L.get(S.get(0));// unsafe code might get exception
            int int_size = integers.size();


            this.num_output_bits =  int_size;
            int varbits = int_size - 2;
            int num_vars = varbits/2;

            str += "cb1 : boolean;" + getNewLine(1);    // condition bit 1
            str += "cb2 : boolean;" + getNewLine(1);    // condition bit 1

            for(int i = 0; i < num_vars ; i++)
            {
                str += "v" + i + "b1 : boolean;" + getNewLine(1);    // condition bit 1
                str += "v" + i + "b2 : boolean;" + getNewLine(1);    // condition bit 1
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return str;
    }

    public String getNewLine(int n)
    {
        String str = "";

        for(int i = 0; i < n; i++)
            str += "\n\r";

        return str;
    }

    public String getInitCode(Kripke kripke)
    {
        String str = "";

        List<String> states = kripke.getQ();
        Map<String, List<Integer>> L = kripke.getL();
        String init_state = "s" + kripke.getQ0();

        if(states.size() > 0)
        {
            str += "init(state) := " + init_state + ";" + getNewLine(1);
            //str += "init(taint) := " + TRUE + ";" + getNewLine(1);

            List<Integer> integers = L.get(kripke.getQ0());
            for(int i = 0; i < integers.size(); i++)
            {
                String var_name = getVarNameByIndex(i);
                int int_val = integers.get(i);
                str += "init(" + var_name + ") := ";
                str += (int_val == 1) ? "TRUE;"  + getNewLine(1) : "FALSE;" + getNewLine(1);
            }
        }

        return str;
    }

    private String getVarNameByIndex(int i)
    {
        String var_name = "";

        if(i == 0)
            var_name = "cb1";
        else if(i == 1)
            var_name = "cb2";
        else
        {

            int var_ind = (i - 2) / 2;
            var_name = "v" + var_ind + "b";
            if(i%2 == 0)    // first bit is divided by 2
                var_name += "1";
            else
                var_name += "2";
        }


        return var_name;
    }

    private String getStateCode(Kripke kripke)
    {
        String str = "{";

        List<String> states = kripke.getQ();

        str += "sd, ";

        for(int i = 0; i < states.size(); i++)
        {
            String state_name = "s" + states.get(i);

            str += state_name;

            if(i < states.size() - 1)
                str += ",";
        }

        str += "}";

        return str;
    }

}
