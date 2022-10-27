package com.adtsw.jos.dsl.model.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ScriptRuntimeContext {

    private final Map<String, Object> runTimeVariables = new HashMap<>();
    @Getter
    private final List<String> runtimeLog = new ArrayList<>();

    public boolean doesVariableExist(String variableName) {
        return runTimeVariables.containsKey(variableName);
    }

    public Object getVariableValue(String variableName) {
        Object variableValue = runTimeVariables.get(variableName);
        return variableValue;
    }

    public void setVariableValue(String variableName, Object variableValue) {
        this.runTimeVariables.put(variableName, variableValue);
    }

    public void setVariableValues(Map<String, Object> variableValues) {
        variableValues.forEach((variableName, variableValue) -> {
            setVariableValue(variableName, variableValue);
        });
    }

    public void appendToRuntimeLog(String logCode, String logStatement) {
        runtimeLog.add(logCode + " : " + logStatement);
    }
}
