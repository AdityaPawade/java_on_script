package com.adtsw.jos.service.function;

import com.adtsw.jos.model.contexts.ScriptLineContext;
import com.adtsw.jos.model.contexts.ScriptRuntimeContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NoOpFunction extends AbstractFunctionDefinition {

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {

        
    }
}
