package com.adtsw.jos.dsl.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.adtsw.jcommons.utils.JsonUtil;
import com.adtsw.jos.dsl.model.contexts.parser.ScriptLineParsingContext;
import com.adtsw.jos.dsl.model.contexts.parser.ScriptParsingContext;
import com.adtsw.jos.dsl.model.contexts.parser.ScriptParsingTransientContext;
import com.adtsw.jos.dsl.model.enums.ScriptLineExecutionPhase;
import com.adtsw.jos.dsl.model.enums.ScriptLineType;
import com.adtsw.jos.dsl.utils.PatternFinder;
import com.adtsw.jos.dsl.utils.ScriptLineAnalyser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptParser {

    private static final Logger logger = LogManager.getLogger(ScriptCompiler.class);

    private final String scriptId;
    private final String scriptsPath;
    private final List<String> scriptLines;
    private final boolean writeParsedScript;

    public ScriptParser(String scriptId, String scriptsPath) {
        this.scriptId = scriptId;
        this.scriptsPath = scriptsPath;
        this.writeParsedScript = true;
        String strategyScriptPath = scriptsPath + "/" + scriptId + ".js";
        try {
            this.scriptLines = FileUtils.readLines(new File(strategyScriptPath), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Unable to find script file : \n" + strategyScriptPath);
        }
    }

    public ScriptParser(String scriptId, List<String> scriptLines) {
        this.scriptId = scriptId;
        this.scriptsPath = "/tmp";
        this.writeParsedScript = false;
        this.scriptLines = scriptLines;
    }

    public ScriptParsingContext parse() {
        ScriptParsingContext parsingContext = new ScriptParsingContext(scriptId, scriptsPath);
        ScriptParsingTransientContext transientContext = new ScriptParsingTransientContext();
        parseScriptLines(scriptLines, parsingContext, transientContext);
        if(this.writeParsedScript) {
            String strategyScriptParsingPath = scriptsPath + "/" + scriptId + ".pout";
            try {
                FileUtils.writeStringToFile(
                    new File(strategyScriptParsingPath), JsonUtil.write(parsingContext, true), Charset.defaultCharset()
                );
            } catch (IOException e) {
                throw new RuntimeException("Unable to write to script parsing file : \n" + strategyScriptParsingPath);
            }
        }
        return parsingContext;
    }

    private void parseScriptLines(List<String> scriptLines, ScriptParsingContext parsingContext, 
            ScriptParsingTransientContext transientContext) {

        for (int i = 0, scriptLinesSize = scriptLines.size(); i < scriptLinesSize; i++) {
            String scriptLine = scriptLines.get(i);
            int lineNumber = i + 1;
            logger.trace(lineNumber + " : " + scriptLine);
            transientContext.setCurrentLine(scriptLine);
            transientContext.setCompiledLine(scriptLine);
            transientContext.setCurrentLineNumber(lineNumber);
            parseScriptLine(parsingContext, transientContext);
        }
    }

    private void parseScriptLine(ScriptParsingContext parsingContext, ScriptParsingTransientContext transientContext) {
        
        String line = ScriptLineAnalyser.getCleanLine(transientContext.getCurrentLine());
        int lineNumber = transientContext.getCurrentLineNumber();
        
        boolean isComment = isLineAComment(line);
        boolean isBlockStart = isLineStartOfABlock(line);

        if(isLineIncomplete(line)) {
            transientContext.getIncompleteLines().append(line);
            updateIncompleteLines(parsingContext, transientContext, line, lineNumber);
            return;
        }
        // code reaches here if current line is not incomplete. till then, it gets collected
        if(transientContext.handlingIncompleteLines()) {
            updateIncompleteLines(parsingContext, transientContext, line, lineNumber);
            line = transientContext.getIncompleteLines().append(line).toString();
            lineNumber = transientContext.getIncompleteLineNumber();
            transientContext.setIncompleteLineNumber(-1);
            transientContext.getIncompleteLines().delete(0, transientContext.getIncompleteLines().length());
        }

        if(transientContext.handlingIncompleteBlocks()) {
            if(!isLineEndOfABlock(line)) {
                if(isLineStartOfABlock(line)) {
                    transientContext.setNumBlockStarted(transientContext.getNumBlockStarted() + 1);
                }
                updateBlockLines(parsingContext, transientContext, line, lineNumber);
                return;
            } else {
                transientContext.setNumBlockStarted(transientContext.getNumBlockStarted() - 1);
                if(transientContext.handlingIncompleteBlocks()) {
                    updateBlockLines(parsingContext, transientContext, line, lineNumber);
                } else {
                    handleBlockCompletion(parsingContext, transientContext, line, lineNumber);
                }
                return;
            }
        }
        
        line = line.replaceAll(";$", "");

        if(!isComment && isBlockStart) {
            transientContext.setNumBlockStarted(transientContext.getNumBlockStarted() + 1);
            line = line.replaceAll("\\{$", "");
        }

        ScriptLineType scriptLineType = getScriptLineType(line);
        ScriptLineParsingContext currentLineContext = new ScriptLineParsingContext(lineNumber, line, scriptLineType);
        transientContext.setCurrentLineContext(currentLineContext);
        transientContext.setCompiledLine(line);

        //logger.trace(lineNumber - 1 + " : " + line);
        parsingContext.getScriptLines().add(lineNumber - 1, currentLineContext);
    }

    private boolean isLineIncomplete(String line) {
        return StringUtils.isNotEmpty(line) && !isLineAComment(line)
            && !isLineStartOfABlock(line) && !isLineEndOfABlock(line) && 
            !line.endsWith(";");
    }

    private boolean isLineEndOfABlock(String line) {
        return line.endsWith("}");
    }

    private boolean isLineStartOfABlock(String line) {
        return line.endsWith("{");
    }

    private boolean isLineAComment(String line) {
        return line.startsWith("//");
    }

    private void handleBlockCompletion(ScriptParsingContext parsingContext, ScriptParsingTransientContext transientContext, 
            String line, int lineNumber) {
        parseBlockLines(parsingContext, transientContext);
        //logger.trace(this.allScriptLines.size() + " : " + line);
        parsingContext.getScriptLines().add(new ScriptLineParsingContext(
            lineNumber, line, ScriptLineType.BLOCK_LINE,
            ScriptLineExecutionPhase.NONE, null
        ));
    }

    private void parseBlockLines(ScriptParsingContext parsingContext, ScriptParsingTransientContext transientContext) {
        ScriptParsingContext blockParsingContext = new ScriptParsingContext(scriptId + "_" + transientContext.getCurrentLineNumber(), scriptsPath);
        ScriptParsingTransientContext blockParsingTransientContext = new ScriptParsingTransientContext();
        ArrayList<String> blockLines = new ArrayList<>(transientContext.getBlockLines());
        parseScriptLines(blockLines, blockParsingContext, blockParsingTransientContext);
        List<ScriptLineParsingContext> allBlockScriptLines = blockParsingContext.getScriptLines();
        ScriptLineParsingContext currentLineContext = transientContext.getCurrentLineContext();
        currentLineContext.setBlockLines(allBlockScriptLines);
        transientContext.setBlockLines(new ArrayList<>());
    }

    private void updateBlockLines(ScriptParsingContext parsingContext, ScriptParsingTransientContext transientContext, String line, int lineNumber) {
        transientContext.getBlockLines().add(line);
        //logger.trace(this.allScriptLines.size() + " : " + line);
        parsingContext.getScriptLines().add(new ScriptLineParsingContext(
            lineNumber, line, ScriptLineType.BLOCK_LINE,
            ScriptLineExecutionPhase.NONE, null
        ));
    }

    private void updateIncompleteLines(ScriptParsingContext parsingContext, ScriptParsingTransientContext transientContext,  
            String line, int lineNumber) {
        
                // all context is added to the start of the incomplete line. it will be added to all script lines at the end of the context completion
        if(transientContext.getIncompleteLineNumber() == -1) {
            transientContext.setIncompleteLineNumber(lineNumber);
        } else {
            ScriptLineParsingContext incompleteScriptLineContext = new ScriptLineParsingContext(
                lineNumber, line, ScriptLineType.INCOMPLETE_LINE,
                ScriptLineExecutionPhase.NONE, null
            );
            parsingContext.getScriptLines().add(incompleteScriptLineContext);
        }
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
}
