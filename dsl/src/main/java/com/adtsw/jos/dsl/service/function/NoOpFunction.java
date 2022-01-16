package com.adtsw.jos.dsl.service.function;

import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;
import com.adtsw.jos.dsl.model.contexts.ScriptRuntimeContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NoOpFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {

        
    }
}
