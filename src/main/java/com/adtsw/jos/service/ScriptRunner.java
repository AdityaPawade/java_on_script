package com.adtsw.jos.service;

import com.adtsw.jos.model.contexts.*;
import com.adtsw.jos.service.function.AbstractFunctionDefinition;
import com.adtsw.jos.service.function.LogFunction;
import com.adtsw.jos.service.function.SetExpressionValueFunction;
import com.adtsw.jos.service.function.SetValueFunction;
import com.adtsw.jos.utils.ExpressionEvaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptRunner {

    private static final Logger logger = LogManager.getLogger(ScriptRunner.class);

    private final ScriptContext scriptContext;
    private final ScriptRuntimeContext runtimeContext;
    private final Map<String, AbstractFunctionDefinition> functionDefinitions;

    public ScriptRunner(ScriptContext scriptContext, ScriptInput input,
                        Map<String, AbstractFunctionDefinition> customFunctionDefinitions) {
        this.scriptContext = scriptContext;
        this.runtimeContext = new ScriptRuntimeContext();
        this.runtimeContext.getRunTimeVariables().putAll(scriptContext.getVariables());
        this.runtimeContext.getRunTimeVariables().putAll(input.getDefaultVariables());
        this.functionDefinitions = new HashMap<>() {{
            put("LOG", new LogFunction());
            put("SETVALUE", new SetValueFunction());
            put("SETEXPRESSIONVALUE", new SetExpressionValueFunction());
            putAll(customFunctionDefinitions);
        }};
    }

    public void run() {
        List<ScriptLineContext> runTimeScriptLines = scriptContext.getRunTimeScriptLines();
        evaluateLineContexts(runTimeScriptLines, 0);
    }

    private void evaluateLineContexts(List<ScriptLineContext> scriptLineContexts, int recursionDepth) {
        for (int i = 0; i < scriptLineContexts.size(); i++) {
            ScriptLineContext lineContext = scriptLineContexts.get(i);
            try {
                long l1 = System.currentTimeMillis();
                evaluateLineContext(lineContext, recursionDepth);
                long l2 = System.currentTimeMillis();
                if((l2 - l1) >= 40) {
                    logger.warn(" delayed execution at recursion [" + recursionDepth + "], \n" + 
                        lineContext.getLineNumber() + " : " + lineContext.getLine() + "\n" + 
                        "took " + (l2 - l1) + " [ " + l1 + " to " + l2 + " ]");
                }
            } catch (Exception e) {
                logger.warn("Exception processing line " + lineContext.getLineNumber() + ":" + lineContext.getLine(), e);
            }
        }
    }

    private void evaluateLineContext(ScriptLineContext lineContext, int recursionDepth) {
        //logger.trace(lineContext.getLineNumber() + " : " + lineContext.getLine());
        switch (lineContext.getLineType()) {
            case VALUE:
            case EXPRESSION:
            case FUNCTION_CALL: {
                evaluateFunctionCallContext(lineContext, recursionDepth);
                break;
            }
        }
    }

    private void evaluateFunctionCallContext(ScriptLineContext lineContext, int recursionDepth) {
        FunctionContext functionContext = lineContext.getFunctionContext();
        String functionName = functionContext.getFunction();
        switch (functionName) {
            case "IF": {
                executeIfBlock(lineContext, recursionDepth);
                break;
            }
            case "FOR": {
                executeForBlock(lineContext, recursionDepth);
                break;
            }
            default: {
                if(functionDefinitions.containsKey(functionName)) {
                    functionDefinitions.get(functionName).execute(lineContext, runtimeContext);
                }
            }
        }
    }

    private void executeForBlock(ScriptLineContext lineContext, int recursionDepth) {
        evaluateLineContexts(lineContext.getBlockLines(), recursionDepth + 1);
    }

    private void executeIfBlock(ScriptLineContext lineContext, int recursionDepth) {
        Object[] originalLexemes = lineContext.getFunctionContext().getOriginalArgs()[0].getLexemes();
        Object[] compiledLexemes = replaceRuntimeVariables(originalLexemes);
        Object evaluationResult = (new ExpressionEvaluator()).evaluate(compiledLexemes);
        //logger.trace(evaluationResult);
        if((Boolean) evaluationResult) {
            evaluateLineContexts(lineContext.getBlockLines(), recursionDepth + 1);
        }
    }

    private Object[] replaceRuntimeVariables(Object[] originalLexemes) {

        Object[] compiledLexemes = new Object[originalLexemes.length];
        for (int i = 0; i < originalLexemes.length; i++) {
            Object lexeme = originalLexemes[i];
            if(lexeme instanceof String) {
                Object computedVariableValue = runtimeContext.getRunTimeVariables().get((String) lexeme);
                compiledLexemes[i] = Objects.requireNonNullElse(computedVariableValue, lexeme);
            } else {
                compiledLexemes[i] = lexeme;
            }
        }
        return compiledLexemes;
    }
}
