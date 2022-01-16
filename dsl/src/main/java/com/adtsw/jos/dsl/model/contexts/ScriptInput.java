package com.adtsw.jos.dsl.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ScriptInput {

    private final Map<String, Object> defaultVariables;
}
