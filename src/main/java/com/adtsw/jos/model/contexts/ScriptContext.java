package com.adtsw.jos.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ScriptContext {

    private final String scriptId;
    private final Map<String, Object> variables;
    private final List<ScriptLineContext> allScriptLines;
    private final List<ScriptLineContext> compileTimeScriptLines;
    private final List<ScriptLineContext> runTimeScriptLines;
}
