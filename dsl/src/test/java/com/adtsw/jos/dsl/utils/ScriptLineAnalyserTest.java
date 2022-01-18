package com.adtsw.jos.dsl.utils;

import org.junit.Test;

public class ScriptLineAnalyserTest {

        @Test
        public void cleanScriptLine() {

            String cleanLine1 = ScriptLineAnalyser.getCleanLine("this is \"a test\"");
            System.out.println(cleanLine1); 
            
            String cleanLine2 = ScriptLineAnalyser.getCleanLine("log(\"test variable\")");
            System.out.println(cleanLine2); 
            
            String cleanLine3 = ScriptLineAnalyser.getCleanLine("log(\"test variable : \" + test_variable + \" value \")");
            System.out.println(cleanLine3); 
        }
}
