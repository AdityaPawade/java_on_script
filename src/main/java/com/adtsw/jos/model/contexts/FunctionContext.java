package com.adtsw.jos.model.contexts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FunctionContext {

    private String function;
    private ArgumentContext[] originalArgs;
    private ArgumentContext[] compiledArgs;
}
