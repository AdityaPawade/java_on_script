package com.adtsw.jos.dsl.examples;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import com.adtsw.jos.dsl.model.contexts.FunctionContext;
import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptRuntimeContext;
import com.adtsw.jos.dsl.service.ScriptCompiler;
import com.adtsw.jos.dsl.service.ScriptRunner;
import com.adtsw.jos.dsl.service.function.AbstractFunctionDefinition;

public class BasicUDF {
    
    public static void main(String[] args) {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_udf.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("basic_udf", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>(){{
            put("custom_udf", new AbstractFunctionDefinition() {
                @Override
                public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
                    FunctionContext functionContext = lineContext.getFunctionContext();
                    String variableName = lineContext.getVariableContext().getName();
                    Integer var1 = (Integer) getArgValue(functionContext, runtimeContext, 0);
                    Integer var2 = (Integer) getArgValue(functionContext, runtimeContext, 1);
                    runtimeContext.setVariableValue(variableName, var1 + var2);
                }
            });
        }});
        scriptRunner.run();
        Object udf_value = scriptRunner.getRuntimeContext().getVariableValue("udf_value");
        System.out.println("value accessed from code : " + udf_value);
    }
}
