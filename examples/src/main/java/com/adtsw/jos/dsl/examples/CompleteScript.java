package com.adtsw.jos.dsl.examples;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.service.ScriptCompiler;
import com.adtsw.jos.dsl.service.ScriptRunner;

public class CompleteScript {
    
    public static void main(String[] args) {
        
        URL scriptURL = ClassLoader.getSystemResource("complete_script.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("complete_script", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object test_diff = scriptRunner.getRuntimeContext().getRunTimeVariables().get("test_diff");
        System.out.println("value accessed from code : " + test_diff);
    }
}
