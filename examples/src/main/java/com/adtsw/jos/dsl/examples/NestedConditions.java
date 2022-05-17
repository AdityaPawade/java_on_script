package com.adtsw.jos.dsl.examples;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.service.ScriptCompiler;
import com.adtsw.jos.dsl.service.ScriptRunner;

public class NestedConditions {
    
    public static void main(String[] args) {
        
        URL scriptURL = ClassLoader.getSystemResource("nested_conditions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("nested_conditions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object test_diff = scriptRunner.getRuntimeContext().getVariableValue("test_diff");
        System.out.println("value accessed from code : " + test_diff);
    }
}
