package com.adtsw.jos.dsl.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ScriptLineTokensContext {

    private String variableName;
    private String originalValue;
    private Object[] originalLexemes;
    private Object[] compiledLexemes;
    private final List<String> computedObjects;
    private final List<String> yetToBeComputedObjects;

    public ScriptLineTokensContext() {
        this.computedObjects = new ArrayList<>();
        this.yetToBeComputedObjects = new ArrayList<>();
    }
}
