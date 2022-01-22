package com.adtsw.jos.dsl.model.contexts.compiler;

import com.adtsw.jos.dsl.model.contexts.ScriptLineContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ScriptCompilationTransientContext {

    private ScriptLineContext currentLineContext = new ScriptLineContext();
}
