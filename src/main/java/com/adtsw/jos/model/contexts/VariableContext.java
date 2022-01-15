package com.adtsw.jos.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VariableContext {

    private String name;
    private String originalValue;
    private String compiledValue;
    private Object[] originalLexemes;
    private Object[] compiledLexemes;
}
