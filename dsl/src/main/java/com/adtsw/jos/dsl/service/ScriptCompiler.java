package com.adtsw.jos.dsl.service;

import com.adtsw.jcommons.utils.JsonUtil;
import com.adtsw.jos.dsl.model.contexts.*;
import com.adtsw.jos.dsl.model.enums.ScriptLineExecutionPhase;
import com.adtsw.jos.dsl.model.enums.ScriptLineType;
import com.adtsw.jos.dsl.utils.LexicalAnalyser;
import com.adtsw.jos.dsl.utils.PatternFinder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScriptCompiler {

    private static final Logger logger = LogManager.getLogger(ScriptCompiler.class);

    private final String scriptId;
    private final String scriptsPath;
    private final Map<String, String> defaultVariables;
    private final List<String> scriptLines;
    private final boolean writeCompiledScript;

    public ScriptCompiler(String scriptId, String scriptsPath, Map<String, String> defaultVariables) {
        this.scriptId = scriptId;
        this.scriptsPath = scriptsPath;
        this.defaultVariables = defaultVariables;
        this.writeCompiledScript = true;
        String strategyScriptPath = scriptsPath + "/" + scriptId + ".js";
        try {
            this.scriptLines = FileUtils.readLines(new File(strategyScriptPath), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Unable to find script file : \n" + strategyScriptPath);
        }
    }

    public ScriptCompiler(String scriptId, List<String> scriptLines, Map<String, String> defaultVariables) {
        this.defaultVariables = defaultVariables;
        this.scriptId = scriptId;
        this.scriptsPath = "/tmp";
        this.writeCompiledScript = false;
        this.scriptLines = scriptLines;
    }

    public ScriptContext compile() {
        ScriptLineCompilationContext compilationContext = new ScriptLineCompilationContext();
        compileScriptLines(scriptLines, compilationContext);
        ScriptContext scriptContext = new ScriptContext(scriptId,
            compilationContext.getVariables(), compilationContext.getAllScriptLines(), 
            compilationContext.getCompileTimeScriptLines(), compilationContext.getRunTimeScriptLines()
        );
        if(this.writeCompiledScript) {
            String strategyScriptCompilationPath = scriptsPath + "/" + scriptId + ".out";
            try {
                FileUtils.writeStringToFile(
                    new File(strategyScriptCompilationPath), JsonUtil.write(scriptContext, true), Charset.defaultCharset()
                );
            } catch (IOException e) {
                throw new RuntimeException("Unable to write to script compilation file : \n" + strategyScriptCompilationPath);
            }
        }
        return scriptContext;
    }

    private void compileScriptLines(List<String> scriptLines, ScriptLineCompilationContext compilationContext) {

        this.defaultVariables.forEach((key, value) -> {
            compilationContext.getVariables().put(key, value);
        });
        
        for (int i = 0, scriptLinesSize = scriptLines.size(); i < scriptLinesSize; i++) {
            String scriptLine = scriptLines.get(i);
            int lineNumber = i + 1;
            logger.trace(lineNumber + " : " + scriptLine);
            compilationContext.setCurrentLine(scriptLine);
            compilationContext.setCompiledLine(scriptLine);
            compilationContext.setCurrentLineNumber(lineNumber);
            compileScriptLine(compilationContext);
        }
    }

    private void compileScriptLine(ScriptLineCompilationContext compilationContext) {
        String line = compilationContext.getCurrentLine().replaceAll("\\s", "");
        int lineNumber = compilationContext.getCurrentLineNumber();
        if(StringUtils.isNotEmpty(line) && !line.startsWith("//")
            && !line.endsWith("{") && !line.endsWith("}") && !line.endsWith(";")) {
            compilationContext.getIncompleteLines().append(line);
            updateIncompleteLines(compilationContext, line, lineNumber);
            return;
        }
        if(compilationContext.getIncompleteLines().length() != 0) {
            compilationContext.getAllScriptLines().add(new ScriptLineContext(
                lineNumber, line, ScriptLineType.INCOMPLETE_LINE, null, null,
                ScriptLineExecutionPhase.NONE, null
            ));
            line = compilationContext.getIncompleteLines().append(line).toString();
            lineNumber = compilationContext.getIncompleteLineNumber();
            compilationContext.setIncompleteLineNumber(-1);
            compilationContext.getIncompleteLines().delete(0, compilationContext.getIncompleteLines().length());
        }

        if(compilationContext.getNumBlockStarted() > 0) {
            if(!line.endsWith("}")) {
                if(line.endsWith("{")) {
                    compilationContext.setNumBlockStarted(compilationContext.getNumBlockStarted() + 1);
                }
                updateBlockLines(compilationContext, line, lineNumber);
                return;
            } else {
                compilationContext.setNumBlockStarted(compilationContext.getNumBlockStarted() - 1);
                if(compilationContext.getNumBlockStarted() > 0) {
                    updateBlockLines(compilationContext, line, lineNumber);
                } else {
                    handleBlockCompletion(compilationContext, line, lineNumber);
                }
                return;
            }
        }
        
        line = line.replaceAll(";$", "");
        boolean blockStart = line.endsWith("{");
        if(blockStart) {
            compilationContext.setNumBlockStarted(compilationContext.getNumBlockStarted() + 1);
            line = line.replaceAll("\\{$", "");
        }

        ScriptLineContext currentLineContext = new ScriptLineContext(lineNumber, line, getScriptLineType(line));
        compilationContext.setCurrentLineContext(currentLineContext);
        compilationContext.setCompiledLine(line);

        if(!blockStart) {
            setLineTypeSpecificContext(
                compilationContext, currentLineContext.getLineType(),
                compilationContext.getNumBlockStarted() == 0
            );
        }

        //logger.trace(lineNumber - 1 + " : " + line);
        compilationContext.getAllScriptLines().add(lineNumber - 1, currentLineContext);
    }

    private void handleBlockCompletion(ScriptLineCompilationContext compilationContext, String line, int lineNumber) {
        compileBlockLines(compilationContext);
        //logger.trace(this.allScriptLines.size() + " : " + line);
        compilationContext.getAllScriptLines().add(new ScriptLineContext(
            lineNumber, line, ScriptLineType.BLOCK_LINE, null, null,
            ScriptLineExecutionPhase.NONE, null
        ));
        setLineTypeSpecificContext(
            compilationContext, compilationContext.getCurrentLineContext().getLineType(),
            compilationContext.getNumBlockStarted() == 0
        );
    }

    private void compileBlockLines(ScriptLineCompilationContext compilationContext) {
        ScriptLineCompilationContext blockCompilationContext = new ScriptLineCompilationContext();
        ArrayList<String> blockLines = new ArrayList<>(compilationContext.getBlockLines());
        blockCompilationContext.getVariables().putAll(compilationContext.getVariables());
        blockCompilationContext.getRunTimeVariables().addAll(compilationContext.getRunTimeVariables());
        compileScriptLines(blockLines, blockCompilationContext);
        List<ScriptLineContext> allBlockScriptLines = blockCompilationContext.getAllScriptLines();
        ScriptLineContext currentLineContext = compilationContext.getCurrentLineContext();
        currentLineContext.setBlockLines(allBlockScriptLines);
        compilationContext.setBlockLines(new ArrayList<>());
    }

    private void updateBlockLines(ScriptLineCompilationContext compilationContext, String line, int lineNumber) {
        compilationContext.getBlockLines().add(line);
        //logger.trace(this.allScriptLines.size() + " : " + line);
        compilationContext.getAllScriptLines().add(new ScriptLineContext(
            lineNumber, line, ScriptLineType.BLOCK_LINE, null, null,
            ScriptLineExecutionPhase.NONE, null
        ));
    }

    private void updateIncompleteLines(ScriptLineCompilationContext compilationContext, String line, int lineNumber) {
        if(compilationContext.getIncompleteLineNumber() == -1) {
            compilationContext.setIncompleteLineNumber(lineNumber);
        } else {
            compilationContext.getAllScriptLines().add(new ScriptLineContext(
                lineNumber, line, ScriptLineType.INCOMPLETE_LINE, null, null,
                ScriptLineExecutionPhase.NONE, null
            ));
        }
    }

    private void setLineTypeSpecificContext(ScriptLineCompilationContext compilationContext, 
                                            ScriptLineType scriptLineType, boolean evaluate) {
        switch (scriptLineType) {
            case VALUE: {
                setValueContext(compilationContext, evaluate);
                break;
            }
            case EXPRESSION: {
                setExpressionContext(compilationContext, evaluate);
                break;
            }
            case FUNCTION_CALL: {
                setFunctionCallContext(compilationContext);
                break;
            }
            default: {
                compilationContext.getCurrentLineContext().setExecutionPhase(ScriptLineExecutionPhase.NONE);
            }
        }
        if(compilationContext.getCurrentLineContext().getExecutionPhase() == ScriptLineExecutionPhase.COMPILE_TIME) {
            compilationContext.getCompileTimeScriptLines().add(compilationContext.getCurrentLineContext());
        } else if(compilationContext.getCurrentLineContext().getExecutionPhase() == ScriptLineExecutionPhase.RUN_TIME) {
            compilationContext.getRunTimeScriptLines().add(compilationContext.getCurrentLineContext());
        }
    }

    private void setValueContext(ScriptLineCompilationContext compilationContext, boolean evaluate) {
        ScriptLineObjectsContext objectsContext = getObjectPair(compilationContext, compilationContext.getCompiledLine());
        ScriptLineContext lineContext = compilationContext.getCurrentLineContext();
        lineContext.setVariableContext(new VariableContext(
            objectsContext.getVariableName(), objectsContext.getOriginalValue(), objectsContext.getCompiledValue(),
            objectsContext.getOriginalLexemes(), objectsContext.getCompiledLexemes()
        ));
        boolean compileTimeExpression = objectsContext.getYetToBeComputedObjects().isEmpty();
        lineContext.setExecutionPhase(ScriptLineExecutionPhase.RUN_TIME);
        if(evaluate && compileTimeExpression) {
            evaluateValue(compilationContext, objectsContext.getVariableName(), objectsContext.getCompiledValue());
        }
        lineContext.setFunctionContext(new FunctionContext("SETVALUE", null, null));
    }

    private void setExpressionContext(ScriptLineCompilationContext compilationContext, boolean evaluate) {
        ScriptLineObjectsContext objectsContext = getObjectPair(compilationContext, compilationContext.getCompiledLine());
        ScriptLineContext lineContext = compilationContext.getCurrentLineContext();
        lineContext.setVariableContext(new VariableContext(
            objectsContext.getVariableName(), objectsContext.getOriginalValue(), objectsContext.getCompiledValue(),
            objectsContext.getOriginalLexemes(), objectsContext.getCompiledLexemes()
        ));
        boolean compileTimeExpression = objectsContext.getYetToBeComputedObjects().isEmpty();
        if (!compileTimeExpression) {
            compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
        }
        lineContext.setExecutionPhase(ScriptLineExecutionPhase.RUN_TIME);    
        if(evaluate && compileTimeExpression) {
            try {
                Object evaluationResult = evaluateExpression(objectsContext.getCompiledValue());
                compilationContext.getVariables().put(objectsContext.getVariableName(), String.valueOf(evaluationResult));
            } catch (RuntimeException re) {
                compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
            }
        }
        lineContext.setFunctionContext(new FunctionContext("SETEXPRESSIONVALUE", null, null));
    }

    protected Object evaluateExpression(String objectValue) {
        try {
            org.codehaus.janino.ExpressionEvaluator evaluator = new ExpressionEvaluator();
            evaluator.cook(objectValue);
            return evaluator.evaluate((Object[]) null);
        } catch (CompileException | InvocationTargetException e) {
            throw new RuntimeException("Unable to evaluate expression " + objectValue);
        }
    }

    private void setFunctionCallContext(ScriptLineCompilationContext compilationContext) {
        ScriptLineContext lineContext = compilationContext.getCurrentLineContext();
        ScriptLineObjectsContext objectsContext = getObjectPair(compilationContext, lineContext.getLine());
        lineContext.setVariableContext(new VariableContext(
            objectsContext.getVariableName(), objectsContext.getOriginalValue(), objectsContext.getCompiledValue(),
            objectsContext.getOriginalLexemes(), objectsContext.getCompiledLexemes()
        ));
        String compiledObjectValue = objectsContext.getCompiledValue();
        String[] compiledObjectValueSplits = compiledObjectValue.split("\\(");
        String[] compiledArgs = compiledObjectValueSplits[1].split("\\)")[0].split(",");
        ArgumentContext[] compiledArgsList = getArguments(compiledArgs);
        String originalObjectValue = objectsContext.getOriginalValue();
        String[] originalObjectValueSplits = originalObjectValue.split("\\(");
        String[] originalArgs = originalObjectValueSplits[1].split("\\)")[0].split(",");
        ArgumentContext[] originalArgsList = getArguments(originalArgs);
        String fn = originalObjectValueSplits[0].toUpperCase();
        FunctionContext functionContext = new FunctionContext(fn, originalArgsList, compiledArgsList);
        lineContext.setFunctionContext(functionContext);
        if(objectsContext.getVariableName() != null) {
            compilationContext.getRunTimeVariables().add(objectsContext.getVariableName());
        }
        evaluateFunctionCall(lineContext, functionContext);
    }

    private ArgumentContext[] getArguments(String[] args) {
        ArgumentContext[] arguments = new ArgumentContext[args.length];
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String argName;
            String argValue;
            if (arg.contains(":")) {
                String[] argSplits = arg.split("\\:");
                argName = argSplits[0];
                argValue = argSplits[1];
            } else {
                argName = null;
                argValue = arg;
            }
            arguments[i] = new ArgumentContext(argName, argValue, LexicalAnalyser.getLexemes(argValue));
        }
        return arguments;
    }

    private ScriptLineObjectsContext getObjectPair(ScriptLineCompilationContext compilationContext, String line) {
        String objectValue;
        String objectName = null;
        if(PatternFinder.isAssignmentPattern(line)) {
            String[] lineSplits = line.split("=");
            objectName = lineSplits[0];
            objectValue = line.substring(objectName.length() + 1);
        } else {
            objectValue = line;
        }
        return getObjectContext(compilationContext, objectValue, objectName);
    }

    private ScriptLineObjectsContext getObjectContext(ScriptLineCompilationContext compilationContext, 
                                                      String objectValue, String objectName) {
        ScriptLineObjectsContext objectsContext = new ScriptLineObjectsContext();
        objectsContext.setVariableName(objectName);
        objectsContext.setOriginalValue(objectValue);
        replaceVariables(
            compilationContext.getVariables(), compilationContext.getRunTimeVariables(), objectsContext
        );
        return objectsContext;
    }

    private void evaluateValue(ScriptLineCompilationContext compilationContext, String objectName, String objectValue) {
        compilationContext.getVariables().put(objectName, objectValue);
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

        ScriptCompiler forCompiler = new ScriptCompiler(
            scriptId + "_for_" + lineContext.getLineNumber(),
            Arrays.asList(
                initExpression + ";",
                "if(" + endCondition + ") {",
                "}",
                incrementalAction + ";"
            ),
            this.defaultVariables
        );

        ScriptContext compilationResult = forCompiler.compile();
        List<ScriptLineContext> allForScriptLines = compilationResult.getAllScriptLines();

        // create a self referencing if block
        
        // add block statements to if block
        List<ScriptLineContext> forBlockLines = allForScriptLines.get(1).getBlockLines();
        forBlockLines.addAll(lineContext.getBlockLines());
        // add incremental action to end of conditional block
        forBlockLines.add(forBlockLines.size() - 1, allForScriptLines.get(3));
        // add if condition to end of conditional block
        forBlockLines.add(forBlockLines.size() - 1, allForScriptLines.get(1));
        // remove incremental action since it is included in the loop
        allForScriptLines.get(3).setExecutionPhase(ScriptLineExecutionPhase.NONE);
        
        lineContext.setBlockLines(allForScriptLines);
    }
    
    private ScriptLineType getScriptLineType(String objectValue) {
        ScriptLineType lineType;
        if(StringUtils.isEmpty(objectValue)) {
            lineType = ScriptLineType.BLANK;
        } else if(objectValue.startsWith("//")) {
            lineType = ScriptLineType.COMMENT;
        } else if(PatternFinder.isValuePattern(objectValue)) {
            lineType = ScriptLineType.VALUE;
        } else if(PatternFinder.isExpressionPattern(objectValue)) {
            lineType = ScriptLineType.EXPRESSION;
        } else if(PatternFinder.isFunctionPattern(objectValue)) {
            lineType = ScriptLineType.FUNCTION_CALL;
        } else {
            lineType = ScriptLineType.UNKNOWN;
        }
        logger.trace(lineType);
        return lineType;
    }

    private void replaceVariables(Map<String, Object> computedVariables,
                                    List<String> yetToBeComputedVariables,
                                    ScriptLineObjectsContext objectsContext) {

        String objectValue = objectsContext.getOriginalValue();
        Object[] originalLexemes = objectsContext.getOriginalLexemes() == null ?
            LexicalAnalyser.getLexemes(objectValue) : objectsContext.getOriginalLexemes();
        Object[] compiledLexemes = new Object[originalLexemes.length];
        //System.out.println(JsonUtil.write(expressionSplits));
        for (int i = 0; i < originalLexemes.length; i++) {
            Object lexeme = originalLexemes[i];
            Object computedVariableValue = computedVariables.get(lexeme);
            if (computedVariableValue != null) {
                String lexemeVariableValue = String.valueOf(computedVariableValue);
                objectsContext.getComputedObjects().add(String.valueOf(lexeme));
                objectValue = objectValue.replaceAll("\\b" + lexeme + "\\b", lexemeVariableValue);
                compiledLexemes[i] = lexemeVariableValue;
            } else if (yetToBeComputedVariables != null && yetToBeComputedVariables.contains(lexeme)) {
                objectsContext.getYetToBeComputedObjects().add(String.valueOf(lexeme));
                compiledLexemes[i] = lexeme;
            }
        }
        //System.out.println(objectValue);
        objectsContext.setOriginalLexemes(originalLexemes);
        objectsContext.setCompiledValue(objectValue);
        objectsContext.setCompiledLexemes(compiledLexemes);
    }
}
