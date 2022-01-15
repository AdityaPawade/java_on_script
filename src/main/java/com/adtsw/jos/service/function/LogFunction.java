package com.adtsw.jos.service.function;

import com.adtsw.jos.model.contexts.FunctionContext;
import com.adtsw.jos.model.contexts.ScriptLineContext;
import com.adtsw.jos.model.contexts.ScriptRuntimeContext;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
public class LogFunction extends AbstractFunctionDefinition {

    private static final Logger logger = LogManager.getLogger(LogFunction.class);

    @Override
    public void execute(ScriptLineContext lineContext, ScriptRuntimeContext runtimeContext) {
        FunctionContext functionContext = lineContext.getFunctionContext();
        Object value = getArgValue(functionContext, runtimeContext, 0);
        logger.debug(value);
    }
}
