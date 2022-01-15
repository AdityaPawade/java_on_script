package com.adtsw.jos.service.function;

import com.adtsw.jos.model.contexts.ScriptLineContext;
import com.adtsw.jos.model.contexts.ScriptRuntimeContext;
import com.adtsw.jos.utils.ExpressionEvaluator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetExpressionValueFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
        String objectName = lineContext.getVariableContext().getName();
        Object[] originalLexemes = lineContext.getVariableContext().getOriginalLexemes();
        Object[] compiledLexemes = replaceRuntimeVariables(runtimeContext, originalLexemes);
        Object evaluationResult = (new ExpressionEvaluator()).evaluate(compiledLexemes);
        runtimeContext.getRunTimeVariables().put(objectName, evaluationResult);
    }
}
