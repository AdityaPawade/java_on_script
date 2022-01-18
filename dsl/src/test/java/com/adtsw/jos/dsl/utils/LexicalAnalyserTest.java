package com.adtsw.jos.dsl.utils;

import com.adtsw.jcommons.utils.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

public class LexicalAnalyserTest {

    @Test
    public void testGetLexemes() {

        Object[] enumWithVariableLexemes = LexicalAnalyser.getLexemes("EnumClass.ENUM_VALUE-2");
        Object[] enumsWithNegativeLexemes = LexicalAnalyser.getLexemes("EnumClass.ENUM_VALUE--2");
        Object[] negativeLexemes = LexicalAnalyser.getLexemes("1--2");
        Object[] doubleNegativeLexemes = LexicalAnalyser.getLexemes("-1--2");
        Object[] negativeWithBracketsLexemes = LexicalAnalyser.getLexemes("1-(-2+3)");
        Object[] negativeWithVariableLexemes = LexicalAnalyser.getLexemes("(1+x)-(-2+3)");
        Object[] negativeWithBooleanLexemes = LexicalAnalyser.getLexemes("(1+x)-(-2==-3)");
        Object[] functionCallLexemes = LexicalAnalyser.getLexemes("analysis(analysisType:AnalysisType.VOLUME,interval:dataSeriesInterval)");
        Object[] ifConditionLexemes = LexicalAnalyser.getLexemes("if(!money_flowing)");
        Object[] forLoopLexemes = LexicalAnalyser.getLexemes("for(i=0,i<long_period,i=i+1)");
        Object[] stringTokenLexemes = LexicalAnalyser.getLexemes("log(\"test variable copy expression\")");

        Assert.assertEquals("[\"EnumClass.ENUM_VALUE\",\"-\",2]", JsonUtil.write(enumWithVariableLexemes));
        Assert.assertEquals("[\"EnumClass.ENUM_VALUE\",\"-\",-2]", JsonUtil.write(enumsWithNegativeLexemes));
        Assert.assertEquals("[1,\"-\",-2]", JsonUtil.write(negativeLexemes));
        Assert.assertEquals("[-1,\"-\",-2]", JsonUtil.write(doubleNegativeLexemes));
        Assert.assertEquals("[1,\"-\",\"(\",-2,\"+\",3,\")\"]", JsonUtil.write(negativeWithBracketsLexemes));
        Assert.assertEquals("[\"(\",1,\"+\",\"x\",\")\",\"-\",\"(\",-2,\"+\",3,\")\"]", JsonUtil.write(negativeWithVariableLexemes));
        Assert.assertEquals("[\"(\",1,\"+\",\"x\",\")\",\"-\",\"(\",-2,\"==\",-3,\")\"]", JsonUtil.write(negativeWithBooleanLexemes));
        Assert.assertEquals("[\"analysis\",\"(\",\"analysisType\",\":\",\"AnalysisType.VOLUME\",\",\",\"interval\",\":\",\"dataSeriesInterval\",\")\"]", JsonUtil.write(functionCallLexemes));
        Assert.assertEquals("[\"if\",\"(\",\"!\",\"money_flowing\",\")\"]", JsonUtil.write(ifConditionLexemes));
        Assert.assertEquals("[\"for\",\"(\",\"i\",\"=\",0,\",\",\"i\",\"<\",\"long_period\",\",\",\"i\",\"=\",\"i\",\"+\",1,\")\"]", JsonUtil.write(forLoopLexemes));
        Assert.assertEquals("[\"log\",\"(\",\"\\\"test variable copy expression\\\"\",\")\"]", JsonUtil.write(stringTokenLexemes));
    }
}