package com.adtsw.jos.dsl.examples;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.service.ScriptCompiler;
import com.adtsw.jos.dsl.service.ScriptRunner;

public class BasicIfConditions {
    
    public static void main(String[] args) {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_if_conditions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler scriptCompilerV2 = new ScriptCompiler("basic_if_conditions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = scriptCompilerV2.compile();
        //ScriptCompiler compiler = new ScriptCompiler("basic_if_conditions", resourceDirectory, new HashMap<>());
        //ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object incremental_variable = scriptRunner.getRuntimeContext().getVariableValue("incremental_variable");
        System.out.println("value accessed from code : " + incremental_variable);
    }
}
