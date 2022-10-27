package com.adtsw.jos.dsl.service.function;

import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptRuntimeContext;
import com.adtsw.jos.dsl.utils.ExpressionEvaluator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetExpressionValueFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
        String variableName = lineContext.getVariableContext().getName();
        Object[] originalLexemes = lineContext.getFunctionContext().getOriginalArgs()[0].getLexemes();
        Object[] compiledLexemes = replaceRuntimeVariables(runtimeContext, originalLexemes);
        Object variableValue = (new ExpressionEvaluator()).evaluate(compiledLexemes);
        runtimeContext.setVariableValue(variableName, variableValue);
        runtimeContext.appendToRuntimeLog("SETV", variableName + " = " + String.valueOf(variableValue));
    }
}
