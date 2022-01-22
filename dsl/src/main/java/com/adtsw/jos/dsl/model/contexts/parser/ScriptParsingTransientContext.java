package com.adtsw.jos.dsl.model.contexts.parser;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ScriptParsingTransientContext {

    private String currentLine;
    private String compiledLine;
    private int currentLineNumber;
    private StringBuilder incompleteLines = new StringBuilder();
    private int incompleteLineNumber = -1;
    private int numBlockStarted = 0;
    private List<String> blockLines = new ArrayList<>();
    private ScriptLineParsingContext currentLineContext = new ScriptLineParsingContext();

    public boolean handlingIncompleteLines() {
        return getIncompleteLines().length() != 0;
    }

    public boolean handlingIncompleteBlocks() {
        return getNumBlockStarted() > 0;
    }
}
