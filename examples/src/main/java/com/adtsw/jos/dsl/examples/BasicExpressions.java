package com.adtsw.jos.dsl.examples;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.service.ScriptCompiler;
import com.adtsw.jos.dsl.service.ScriptRunner;

public class BasicExpressions {
    
    public static void main(String[] args) {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_expressions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("basic_expressions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object test_variable_expression_value = scriptRunner.getRuntimeContext().getVariableValue("test_variable_copy_expression");
        System.out.println("value accessed from code : " + test_variable_expression_value);
    }
}
