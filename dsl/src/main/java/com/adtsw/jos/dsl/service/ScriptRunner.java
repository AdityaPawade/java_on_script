package com.adtsw.jos.dsl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.adtsw.jos.dsl.model.contexts.FunctionContext;
import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptInput;
import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptRuntimeContext;
import com.adtsw.jos.dsl.model.enums.ScriptLineExecutionPhase;
import com.adtsw.jos.dsl.service.function.AbstractFunctionDefinition;
import com.adtsw.jos.dsl.service.function.LogFunction;
import com.adtsw.jos.dsl.service.function.SetExpressionValueFunction;
import com.adtsw.jos.dsl.service.function.SetValueFunction;
import com.adtsw.jos.dsl.utils.ExpressionEvaluator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class ScriptRunner {

    private static final Logger logger = LogManager.getLogger(ScriptRunner.class);

    private final ScriptContext scriptContext;
    @Getter
    private final ScriptRuntimeContext runtimeContext;
    private final Map<String, AbstractFunctionDefinition> functionDefinitions;

    public ScriptRunner(ScriptContext scriptContext, ScriptInput input,
                        Map<String, AbstractFunctionDefinition> customFunctionDefinitions) {
        this.scriptContext = scriptContext;
        this.runtimeContext = new ScriptRuntimeContext();
        this.runtimeContext.setVariableValues(scriptContext.getVariables());
        this.runtimeContext.setVariableValues(input.getDefaultVariables());
        this.functionDefinitions = new HashMap<>() {{
            put("LOG", new LogFunction());
            put("SETVALUE", new SetValueFunction());
            put("SETEXPRESSIONVALUE", new SetExpressionValueFunction());
        }};
        customFunctionDefinitions.forEach((functionName, functionDefinition) -> {
            this.functionDefinitions.put(functionName.toUpperCase(), functionDefinition);
        });
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
                logger.warn("Exception processing line " + lineContext.getLineNumber() + ":" + 
                    lineContext.getLine() + " : " + e.getMessage());
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
            default:
                throw new RuntimeException("Unsupported line type " + lineContext.getLineNumber()
                        + " : " + lineContext.getLine());
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
        List<ScriptLineContext> blockLines = getRunTimeBlockLines(lineContext);
        evaluateLineContexts(blockLines, recursionDepth + 1);
    }

    private void executeIfBlock(ScriptLineContext lineContext, int recursionDepth) {
        Object[] originalLexemes = lineContext.getFunctionContext().getOriginalArgs()[0].getLexemes();
        Object[] compiledLexemes = replaceRuntimeVariables(originalLexemes);
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
        Object evaluationResult = expressionEvaluator.evaluate(compiledLexemes);
        //logger.trace(evaluationResult);
        if((Boolean) evaluationResult) {
            List<ScriptLineContext> blockLines = getRunTimeBlockLines(lineContext);
            evaluateLineContexts(blockLines, recursionDepth + 1);
        }
    }

    private List<ScriptLineContext> getRunTimeBlockLines(ScriptLineContext lineContext) {
        List<ScriptLineContext> blockLines = lineContext.getBlockLines().stream().filter(line -> {
            return ScriptLineExecutionPhase.RUN_TIME.equals(line.getExecutionPhase());
        }).collect(Collectors.toList());
        return blockLines;
    }

    private Object[] replaceRuntimeVariables(Object[] originalLexemes) {

        Object[] compiledLexemes = new Object[originalLexemes.length];
        for (int i = 0; i < originalLexemes.length; i++) {
            Object lexeme = originalLexemes[i];
            if(lexeme instanceof String) {
                String lexemeString = (String) lexeme;
                if(!runtimeContext.doesVariableExist(lexemeString)) {
                    compiledLexemes[i] = lexemeString;    
                } else {
                    Object computedVariableValue = runtimeContext.getVariableValue(lexemeString);
                    compiledLexemes[i] = computedVariableValue;
                }
            } else {
                compiledLexemes[i] = lexeme;
            }
        }
        return compiledLexemes;
    }
}
