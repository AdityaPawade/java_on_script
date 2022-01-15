package com.adtsw.jos.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ScriptRuntimeContext {

    private final Map<String, Object> runTimeVariables = new HashMap<>();
}
