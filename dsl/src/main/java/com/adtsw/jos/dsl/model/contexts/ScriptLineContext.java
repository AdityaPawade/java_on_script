package com.adtsw.jos.dsl.model.contexts;

import com.adtsw.jos.dsl.model.enums.ScriptLineExecutionPhase;
import com.adtsw.jos.dsl.model.enums.ScriptLineType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class ScriptLineContext {

    private int lineNumber;
    private String line;
    private ScriptLineType lineType;
    private VariableContext variableContext;
    private FunctionContext functionContext;
    private ScriptLineExecutionPhase executionPhase;
    private List<ScriptLineContext> blockLines;

    public ScriptLineContext(int lineNumber, String line, ScriptLineType lineType) {
        this.lineNumber = lineNumber;
        this.line = line;
        this.lineType = lineType;
    }
}
