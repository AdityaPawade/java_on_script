package com.adtsw.jos.dsl.model.contexts;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScriptRuntimeContext {

    private final Map<String, Object> runTimeVariables = new HashMap<>();

    public boolean doesVariableExist(String variableName) {
        return runTimeVariables.containsKey(variableName);
    }

    public Object getVariableValue(String variableName) {
        return runTimeVariables.get(variableName);
    }

    public void setVariableValue(String variableName, Object variableValue) {
        this.runTimeVariables.put(variableName, variableValue);
    }

    public void setVariableValues(Map<String, Object> variableValues) {
        this.runTimeVariables.putAll(variableValues);
    }
}
