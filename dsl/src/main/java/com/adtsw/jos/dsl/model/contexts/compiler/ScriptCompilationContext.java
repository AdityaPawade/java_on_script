package com.adtsw.jos.dsl.model.contexts.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptCompilationContext {

    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> runTimeVariables = new ArrayList<>();

    private final List<ScriptLineContext> allScriptLines = new ArrayList<>();
    private final List<ScriptLineContext> runTimeScriptLines = new ArrayList<>();
}
