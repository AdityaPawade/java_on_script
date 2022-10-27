package com.adtsw.jos.dsl.service.function;

import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptRuntimeContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetValueFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
        String variableName = lineContext.getVariableContext().getName();
        Object variableValue = lineContext.getFunctionContext().getOriginalArgs()[0].getLexemes()[0];
        variableValue = getObject(runtimeContext, variableValue);
        runtimeContext.setVariableValue(variableName, variableValue);
        runtimeContext.appendToRuntimeLog("SETV",  variableName + " = " + String.valueOf(variableValue));
    }
}
