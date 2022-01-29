package com.adtsw.jos.dsl.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.adtsw.jcommons.utils.JsonUtil;
import com.adtsw.jos.dsl.model.contexts.ArgumentContext;
import com.adtsw.jos.dsl.model.contexts.FunctionContext;
import com.adtsw.jos.dsl.model.contexts.ScriptContext;
import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptLineTokensContext;
import com.adtsw.jos.dsl.model.contexts.VariableContext;
import com.adtsw.jos.dsl.model.contexts.compiler.ScriptCompilationContext;
import com.adtsw.jos.dsl.model.contexts.compiler.ScriptCompilationTransientContext;
import com.adtsw.jos.dsl.model.contexts.parser.ScriptLineParsingContext;
import com.adtsw.jos.dsl.model.contexts.parser.ScriptParsingContext;
import com.adtsw.jos.dsl.model.enums.ScriptLineExecutionPhase;
import com.adtsw.jos.dsl.model.enums.ScriptLineType;
import com.adtsw.jos.dsl.utils.ExpressionEvaluator;
import com.adtsw.jos.dsl.utils.LexicalAnalyser;
import com.adtsw.jos.dsl.utils.PatternFinder;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptCompiler {

    private static final Logger logger = LogManager.getLogger(ScriptCompiler.class);

    private final ScriptParsingContext parsingContext;
    private final Map<String, String> defaultVariables;
    private final boolean writeCompiledScript;

    public ScriptCompiler(String scriptId, String scriptPath, Map<String, String> defaultVariables) {
        ScriptParser parser = new ScriptParser(scriptId, scriptPath);
        ScriptParsingContext parsingContext = parser.parse();
        this.parsingContext = parsingContext;
        this.defaultVariables = defaultVariables;
        this.writeCompiledScript = true;
    }

    public ScriptCompiler(ScriptParsingContext parsingContext, Map<String, String> defaultVariables) {
        this.parsingContext = parsingContext;
        this.defaultVariables = defaultVariables;
        this.writeCompiledScript = true;
    }

    public ScriptContext compile() {
        ScriptCompilationContext compilationContext = new ScriptCompilationContext();
        ScriptCompilationTransientContext transientContext = new ScriptCompilationTransientContext();

        compileScriptLines(parsingContext, compilationContext, transientContext);
        ScriptContext scriptContext = new ScriptContext(parsingContext.getScriptId(),
                compilationContext.getVariables(), compilationContext.getAllScriptLines(),
                compilationContext.getRunTimeScriptLines());
        if (this.writeCompiledScript) {
            String strategyScriptCompilationPath = parsingContext.getScriptPath() + "/" + parsingContext.getScriptId()
                    + ".out";
            try {
                FileUtils.writeStringToFile(
                        new File(strategyScriptCompilationPath), JsonUtil.write(scriptContext, true),
                        Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(
                        "Unable to write to script compilation file : \n" + strategyScriptCompilationPath);
            }
        }
        return scriptContext;
    }

    private void compileScriptLines(ScriptParsingContext parsingContext, ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext) {

        this.defaultVariables.forEach((key, value) -> {
            compilationContext.getVariables().put(key, value);
        });

        for (int i = 0, scriptLinesSize = parsingContext.getScriptLines().size(); i < scriptLinesSize; i++) {

            ScriptLineParsingContext lineParsingContext = parsingContext.getScriptLines().get(i);
            compileScriptLine(compilationContext, transientContext, lineParsingContext);
        }
    }

    private void compileScriptLine(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext,
            ScriptLineParsingContext lineParsingContext) {

        String line = lineParsingContext.getLine();
        int lineNumber = lineParsingContext.getLineNumber();
        ScriptLineType lineType = lineParsingContext.getLineType();
        logger.trace(lineNumber + " : " + line + " [ " + lineType + " ]");

        ScriptLineContext currentLineContext = new ScriptLineContext(lineNumber, line, lineType);

        transientContext.setCurrentLineContext(currentLineContext);

        if (lineParsingContext.isBlock()) {
            handleBlockCompletion(compilationContext, transientContext, lineParsingContext);
        }

        setLineTypeSpecificContext(compilationContext, transientContext, currentLineContext.getLineType());

        // logger.trace(lineNumber - 1 + " : " + line);
        compilationContext.getAllScriptLines().add(lineNumber - 1, currentLineContext);
    }

    private void handleBlockCompletion(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext,
            ScriptLineParsingContext lineParsingContext) {

        ScriptCompilationContext blockCompilationContext = new ScriptCompilationContext();
        ScriptCompilationTransientContext blockCompilationTransientContext = new ScriptCompilationTransientContext();
        ScriptParsingContext blockParsingContext = new ScriptParsingContext(
                parsingContext.getScriptId() + "_" + lineParsingContext.getLineNumber(),
                parsingContext.getScriptPath());
        blockParsingContext.getScriptLines().addAll(lineParsingContext.getBlockLines());

        blockCompilationContext.getVariables().putAll(compilationContext.getVariables());
        blockCompilationContext.getRunTimeVariables().addAll(compilationContext.getRunTimeVariables());

        compileScriptLines(blockParsingContext, blockCompilationContext, blockCompilationTransientContext);

        List<ScriptLineContext> allBlockScriptLines = blockCompilationContext.getAllScriptLines();
        ScriptLineContext currentLineContext = transientContext.getCurrentLineContext();
        currentLineContext.setBlockLines(allBlockScriptLines);
    }

    private void setLineTypeSpecificContext(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext, ScriptLineType scriptLineType) {
        
        switch (scriptLineType) {
            case VALUE: {
                setValueContext(compilationContext, transientContext);
                break;
            }
            case EXPRESSION: {
                setExpressionContext(compilationContext, transientContext);
                break;
            }
            case FUNCTION_CALL: {
                setFunctionCallContext(compilationContext, transientContext);
                break;
            }
            default: {
                transientContext.getCurrentLineContext().setExecutionPhase(ScriptLineExecutionPhase.NONE);
            }
        }
        if (transientContext.getCurrentLineContext().getExecutionPhase() == ScriptLineExecutionPhase.RUN_TIME) {
            compilationContext.getRunTimeScriptLines().add(transientContext.getCurrentLineContext());
        }
    }

    private void setValueContext(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext) {
        ScriptLineContext lineContext = transientContext.getCurrentLineContext();
        ScriptLineTokensContext objectsContext = getTokensContext(compilationContext, lineContext.getLine());
        lineContext.setVariableContext(new VariableContext(objectsContext.getVariableName()));
        boolean compileTimeExpression = objectsContext.getYetToBeComputedObjects().isEmpty();
        if (compileTimeExpression) {
            compilationContext.getVariables().put(
                    objectsContext.getVariableName(), objectsContext.getCompiledLexemes()[0]);
        }
        lineContext.setExecutionPhase(ScriptLineExecutionPhase.RUN_TIME);
        ArgumentContext[] originalArgs = new ArgumentContext[1];
        originalArgs[0] = new ArgumentContext(null, String.valueOf(objectsContext.getOriginalLexemes()[0]),
                objectsContext.getOriginalLexemes());
        ArgumentContext[] compiledArgs = new ArgumentContext[1];
        compiledArgs[0] = new ArgumentContext(null, String.valueOf(objectsContext.getCompiledLexemes()[0]),
                objectsContext.getCompiledLexemes());
        lineContext.setFunctionContext(new FunctionContext("SETVALUE", originalArgs, compiledArgs));
    }

    private void setExpressionContext(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext) {
        ScriptLineContext lineContext = transientContext.getCurrentLineContext();
        ScriptLineTokensContext objectsContext = getTokensContext(compilationContext, lineContext.getLine());
        lineContext.setVariableContext(new VariableContext(objectsContext.getVariableName()));
        boolean compileTimeExpression = objectsContext.getYetToBeComputedObjects().isEmpty();
        if (!compileTimeExpression) {
            compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
        } else {
            try {
                ExpressionEvaluator evaluator = new ExpressionEvaluator();
                Object evaluationResult = evaluator.evaluate(objectsContext.getCompiledLexemes());
                compilationContext.getVariables().put(objectsContext.getVariableName(),evaluationResult);
            } catch (RuntimeException re) {
                compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
            }
        }
        lineContext.setExecutionPhase(ScriptLineExecutionPhase.RUN_TIME);
        ArgumentContext[] originalArgs = new ArgumentContext[1];
        originalArgs[0] = new ArgumentContext(null, String.valueOf(objectsContext.getOriginalLexemes()[0]),
                objectsContext.getOriginalLexemes());
        ArgumentContext[] compiledArgs = new ArgumentContext[1];
        compiledArgs[0] = new ArgumentContext(null, String.valueOf(objectsContext.getCompiledLexemes()[0]),
                objectsContext.getCompiledLexemes());
        lineContext.setFunctionContext(new FunctionContext("SETEXPRESSIONVALUE", originalArgs, compiledArgs));
    }

    private void setFunctionCallContext(ScriptCompilationContext compilationContext,
            ScriptCompilationTransientContext transientContext) {
        
        ScriptLineContext lineContext = transientContext.getCurrentLineContext();
        ScriptLineTokensContext objectsContext = getTokensContext(compilationContext, lineContext.getLine());
        lineContext.setVariableContext(new VariableContext(objectsContext.getVariableName()));

        ArgumentContext[] compiledArgsList = extractArgContext(objectsContext.getCompiledLexemes());
        ArgumentContext[] originalArgsList = extractArgContext(objectsContext.getOriginalLexemes());
        String fn = String.valueOf(objectsContext.getCompiledLexemes()[0]).toUpperCase();

        FunctionContext functionContext = new FunctionContext(fn, originalArgsList, compiledArgsList);
        lineContext.setFunctionContext(functionContext);
        if (objectsContext.getVariableName() != null) {
            compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
        }
        evaluateFunctionCall(lineContext, functionContext);
    }

    private ArgumentContext[] extractArgContext(Object[] lexemes) {
        int argCount = 0;
        for (int i = 2; i < lexemes.length - 1; i++) {
            if(String.valueOf(lexemes[i]).equals(",")) {
                argCount = argCount + 1;
            }
        }
        ArgumentContext[] args = new ArgumentContext[argCount + 1];
        int currentArgCount = 0;
        for (int i = 2; i < lexemes.length - 1; i++) {
            String argName = null;
            if(String.valueOf(lexemes[i + 1]).equals(":")) {
                argName = String.valueOf(lexemes[i]);
                i = i + 2;
            }
            List<Object> argValueObjects = new ArrayList<>();
            StringBuilder argValueBuilder = new StringBuilder();
            for(int j = i; j < lexemes.length - 1 && !String.valueOf(lexemes[j]).equals(","); j++) {
                argValueObjects.add(lexemes[j]);
                argValueBuilder.append(String.valueOf(lexemes[j]));
                i = j;
            }
            i = i + 1;
            Object[] argLexemes = argValueObjects.toArray();
            args[currentArgCount] = new ArgumentContext(argName, argValueBuilder.toString(), argLexemes);
            currentArgCount = currentArgCount + 1;
        }
        return args;
    }

    private ScriptLineTokensContext getTokensContext(ScriptCompilationContext compilationContext, String line) {
        String objectValue;
        String objectName = null;
        if (PatternFinder.isAssignmentPattern(line)) {
            String[] lineSplits = line.split("=");
            objectName = lineSplits[0];
            objectValue = line.substring(objectName.length() + 1);
        } else {
            objectValue = line;
        }
        ScriptLineTokensContext objectsContext = new ScriptLineTokensContext();
        objectsContext.setVariableName(objectName);
        objectsContext.setOriginalValue(objectValue);
        replaceVariables(compilationContext.getVariables(), compilationContext.getRunTimeVariables(), objectsContext);
        return objectsContext;
    }

    private void evaluateFunctionCall(ScriptLineContext lineContext, FunctionContext functionContext) {

        switch (functionContext.getFunction()) {
            case "FOR":
                evaluateForFunction(lineContext);
                break;
        }
        lineContext.setExecutionPhase(ScriptLineExecutionPhase.RUN_TIME);
    }

    private void evaluateForFunction(ScriptLineContext lineContext) {
        String initExpression = lineContext.getFunctionContext().getOriginalArgs()[0].getValue();
        String endCondition = lineContext.getFunctionContext().getOriginalArgs()[1].getValue();
        String incrementalAction = lineContext.getFunctionContext().getOriginalArgs()[2].getValue();

        String conditionScriptId = parsingContext.getScriptId() + "_for_" + lineContext.getLineNumber();
        List<String> conditionLines = Arrays.asList(
                initExpression + ";",
                "if(" + endCondition + ") {",
                "}",
                incrementalAction + ";");

        ScriptParser conditionParser = new ScriptParser(conditionScriptId, conditionLines);
        ScriptParsingContext parsingContext = conditionParser.parse();

        ScriptCompiler forCompiler = new ScriptCompiler(
                parsingContext, this.defaultVariables);

        ScriptContext compilationResult = forCompiler.compile();
        List<ScriptLineContext> allForScriptLines = compilationResult.getAllScriptLines();

        // create a self referencing if block

        // add block statements to if block
        allForScriptLines.get(1).setBlockLines(new ArrayList<>());
        List<ScriptLineContext> forBlockLines = allForScriptLines.get(1).getBlockLines();
        forBlockLines.addAll(lineContext.getBlockLines());
        // add incremental action to end of conditional block
        forBlockLines.add(forBlockLines.size() - 1, allForScriptLines.get(3));
        // add if condition to end of conditional block
        forBlockLines.add(forBlockLines.size() - 1, allForScriptLines.get(1));
        // remove incremental action since it is included in the loop
        // allForScriptLines.get(3).setExecutionPhase(ScriptLineExecutionPhase.NONE);

        lineContext.setBlockLines(allForScriptLines);
    }

    private void replaceVariables(Map<String, Object> computedVariables,
            List<String> yetToBeComputedVariables,
            ScriptLineTokensContext objectsContext) {

        String objectValue = objectsContext.getOriginalValue();
        Object[] originalLexemes = LexicalAnalyser.getLexemes(objectValue);
        Object[] compiledLexemes = new Object[originalLexemes.length];
        // System.out.println(JsonUtil.write(expressionSplits));
        for (int i = 0; i < originalLexemes.length; i++) {
            Object lexeme = originalLexemes[i];
            Object computedVariableValue = computedVariables.get(lexeme);
            if (computedVariableValue != null) {
                //String lexemeVariableValue = String.valueOf(computedVariableValue);
                objectsContext.getComputedObjects().add(String.valueOf(lexeme));
                //objectValue = objectValue.replaceAll("\\b" + lexeme + "\\b", lexemeVariableValue);
                compiledLexemes[i] = computedVariableValue;
            } else if (yetToBeComputedVariables != null && yetToBeComputedVariables.contains(lexeme)) {
                objectsContext.getYetToBeComputedObjects().add(String.valueOf(lexeme));
                compiledLexemes[i] = lexeme;
            } else {
                compiledLexemes[i] = lexeme;
            }
        }
        objectsContext.setOriginalLexemes(originalLexemes);
        objectsContext.setCompiledLexemes(compiledLexemes);
    }
}
