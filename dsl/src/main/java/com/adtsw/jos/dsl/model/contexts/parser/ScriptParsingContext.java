package com.adtsw.jos.dsl.model.contexts.parser;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScriptParsingContext {

    private final String scriptId;
    private final String scriptPath;
    private final List<ScriptLineParsingContext> scriptLines = new ArrayList<>();
}
