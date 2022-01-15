package com.adtsw.jos.service.function;

import com.adtsw.jos.model.contexts.ScriptLineContext;
import com.adtsw.jos.model.contexts.ScriptRuntimeContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetValueFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
        String objectName = lineContext.getVariableContext().getName();
        Object objectValue = lineContext.getVariableContext().getOriginalLexemes()[0];
        objectValue = getObject(runtimeContext, objectValue);
        runtimeContext.getRunTimeVariables().put(objectName, objectValue);
    }
}
