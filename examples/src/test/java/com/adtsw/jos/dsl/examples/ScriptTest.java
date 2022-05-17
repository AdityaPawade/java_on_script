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

import org.junit.Assert;
import org.junit.Test;

public class ScriptTest {
    
    @Test
    public void basicExpressionsTest() {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_expressions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("basic_expressions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object test_variable_expression_value = scriptRunner.getRuntimeContext().getVariableValue("test_variable_copy_expression");
        Assert.assertEquals(28, test_variable_expression_value);
    }

    @Test
    public void basicForLoopTest() {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_for_loop.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("basic_for_loop", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object for_loop_value = scriptRunner.getRuntimeContext().getVariableValue("for_loop_value");
        Assert.assertEquals(14, for_loop_value);
    }

    @Test
    public void basicIfConditionTest() {
        
        URL scriptURL = ClassLoader.getSystemResource("basic_if_conditions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("basic_if_conditions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object incremental_variable = scriptRunner.getRuntimeContext().getVariableValue("incremental_variable");
        Assert.assertEquals(1, incremental_variable);
    }

    @Test
    public void basicUDFTest() {
        
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
        Assert.assertEquals(42, udf_value);
    }

    @Test
    public void nestedConditionsTest() {
        
        URL scriptURL = ClassLoader.getSystemResource("nested_conditions.js");
        String resourceDirectory = (new File(scriptURL.getPath())).getParentFile().getPath();
        ScriptCompiler compiler = new ScriptCompiler("nested_conditions", resourceDirectory, new HashMap<>());
        ScriptContext scriptContext = compiler.compile();
        ScriptRunner scriptRunner = new ScriptRunner(scriptContext, new ScriptInput(new HashMap<>()), new HashMap<>());
        scriptRunner.run();
        Object test_diff = scriptRunner.getRuntimeContext().getVariableValue("test_diff");
        Assert.assertEquals(100.0, test_diff);
    }
}
