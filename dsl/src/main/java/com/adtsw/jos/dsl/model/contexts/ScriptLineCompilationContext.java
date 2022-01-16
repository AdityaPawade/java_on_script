package com.adtsw.jos.dsl.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ScriptLineCompilationContext {

    private String currentLine;
    private String compiledLine;
    private int currentLineNumber;
    private StringBuilder incompleteLines = new StringBuilder();
    private int incompleteLineNumber = -1;
    private int numBlockStarted = 0;
    private List<String> blockLines = new ArrayList<>();
    private ScriptLineContext currentLineContext = new ScriptLineContext();

    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> runTimeVariables = new ArrayList<>();

    private final List<ScriptLineContext> allScriptLines = new ArrayList<>();
    private final List<ScriptLineContext> compileTimeScriptLines = new ArrayList<>();
    private final List<ScriptLineContext> runTimeScriptLines = new ArrayList<>();
}
