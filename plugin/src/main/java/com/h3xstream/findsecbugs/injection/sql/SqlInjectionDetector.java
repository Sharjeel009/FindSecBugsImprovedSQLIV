/**
 * Find Security Bugs
 * Copyright (c) Philippe Arteau, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.injection.BasicInjectionDetector;
import com.h3xstream.findsecbugs.taintanalysis.Taint;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import org.apache.bcel.classfile.Method;

import java.util.ArrayList;
import java.util.List;

public class SqlInjectionDetector extends BasicInjectionDetector {

    BugReporter bugReporter = null;
    List<String> oListClasses = new ArrayList<String>();

    public SqlInjectionDetector(BugReporter bugReporter) {
        super(bugReporter);
        this.bugReporter = bugReporter;

        // Just for testing
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00104");
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00436");
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00437");
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00851");
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00514");
        oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00517");

        //oListClasses.add("org/owasp/benchmark/testcode/BenchmarkTest00517");
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
    
    @Override
    protected int getPriority(Taint taint) {
        if (!taint.isSafe() && taint.hasTag(Taint.Tag.SQL_INJECTION_SAFE)) {
            return Priorities.IGNORE_PRIORITY;
        } else if (!taint.isSafe() && taint.hasTag(Taint.Tag.APOSTROPHE_ENCODED)) {
            return Priorities.LOW_PRIORITY;
        } else {
            return super.getPriority(taint);
        }
    }

    @Override
    public void visitClassContext(ClassContext classContext)
    {
        if(!shouldAnalyzeClass(classContext))
        {
            return;
        }

        else {
            for (Method method : classContext.getMethodsInCallOrder()) {
                if (classContext.getMethodGen(method) == null) {
                    continue;
                }
                try {
                    analyzeMethod(classContext, method);
                } catch (CheckedAnalysisException e) {
                    logException(classContext, method, e);
                } catch (RuntimeException e) {
                    logException(classContext, method, e);
                }
            }
        }
    }

    private boolean isClassFromSelectedClasses(ClassContext classContext)
    {
        boolean ret_val = false;

        System.out.println(classContext.getClassDescriptor().getClassName());
        if (oListClasses.contains(classContext.getClassDescriptor().getClassName()))
            ret_val = true;
        else
            ret_val = false;

        return false; //true;    // temp
        //return ret_val;
    }
}
