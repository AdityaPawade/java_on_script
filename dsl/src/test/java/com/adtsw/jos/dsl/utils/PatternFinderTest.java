package com.adtsw.jos.dsl.utils;

import org.junit.Assert;
import org.junit.Test;

public class PatternFinderTest {
    
    @Test
    public void isExpressionPatternTest() {

        String expressionLine1 = "test_variable=14";
        boolean isExpressionLine1 = PatternFinder.isExpressionPattern(expressionLine1);
        
        String expressionLine2 = "test_variable_copy=test_variable";
        boolean isExpressionLine2 = PatternFinder.isExpressionPattern(expressionLine2);
        
        String expressionLine3 = "test_variable_expression=test_variable*2";
        boolean isExpressionLine3 = PatternFinder.isExpressionPattern(expressionLine3);
        
        String expressionLine4 = "test_14_variable_expression=test_variable*2";
        boolean isExpressionLine4 = PatternFinder.isExpressionPattern(expressionLine4);
        
        String negativeExpressionLine4 = "test_variable_copy_expression=-1*-52+4";
        boolean isNegativeExpressionLine4 = PatternFinder.isExpressionPattern(negativeExpressionLine4);

        String doubleNegativeExpressionLine4 = "test_variable_copy_expression=-1*-52+(-4)";
        boolean isDoubleNegativeExpressionLine4 = PatternFinder.isExpressionPattern(doubleNegativeExpressionLine4);
        
        String stringExpressionLine4 = "test_variable_copy_expression=\"test_variable_copy\"*\"2\"";
        boolean isStringExpressionLine4 = PatternFinder.isExpressionPattern(stringExpressionLine4);
        
        String stringWithSpaceExpressionLine4 = "test_variable_copy_expression=\"test variable copy\"*\"2\"";
        boolean isStringWithSpaceExpressionLine4 = PatternFinder.isExpressionPattern(stringWithSpaceExpressionLine4);
        
        String bracketExpressionLine4 = "test_variable_copy_expression=\"test variable copy\"*(2+4*(4/6))";
        boolean isBracketExpressionLine4 = PatternFinder.isExpressionPattern(bracketExpressionLine4);

        String openingBracketExpressionLine4 = "test_variable=((for_loop_value*1.0)/(test_variable_expression*1.0))*100.0";
        boolean isOpeningBracketExpressionLine4 = PatternFinder.isExpressionPattern(openingBracketExpressionLine4);
        
        Assert.assertTrue(isExpressionLine1);
        Assert.assertTrue(isExpressionLine2);
        Assert.assertTrue(isExpressionLine3);
        Assert.assertTrue(isExpressionLine4);
        Assert.assertTrue(isNegativeExpressionLine4);
        Assert.assertTrue(isDoubleNegativeExpressionLine4);
        Assert.assertTrue(isStringExpressionLine4);
        Assert.assertTrue(isStringWithSpaceExpressionLine4);
        Assert.assertTrue(isBracketExpressionLine4);
        Assert.assertTrue(isOpeningBracketExpressionLine4);
    }

    @Test
    public void isNotExpressionPatternTest() {

        String expressionLine1 = "14=abc";
        boolean isExpressionLine1 = PatternFinder.isExpressionPattern(expressionLine1);
        
        String expressionLine2 = "\"test_variable_copy\"=test_variable";
        boolean isExpressionLine2 = PatternFinder.isExpressionPattern(expressionLine2);
        
        String expressionLine3 = "test_variable_expression=test_variable;2";
        boolean isExpressionLine3 = PatternFinder.isExpressionPattern(expressionLine3);
        
        String expressionLine4 = "test_variable_copy_expression=hello(test_variable_copy*2)";
        boolean isExpressionLine4 = PatternFinder.isExpressionPattern(expressionLine4);
        
        String negativeExpressionLine4 = "test_variable_copy_expression=hello(--1*-52+4)";
        boolean isNegativeExpressionLine4 = PatternFinder.isExpressionPattern(negativeExpressionLine4);
        
        Assert.assertFalse(isExpressionLine1);
        Assert.assertFalse(isExpressionLine2);
        Assert.assertFalse(isExpressionLine3);
        Assert.assertFalse(isExpressionLine4);
        Assert.assertFalse(isNegativeExpressionLine4);
    }

    @Test
    public void isFunctionPatternTest() {

        String functionLine1 = "test_variable=test_function(14)";
        boolean isFunctionLine1 = PatternFinder.isFunctionPattern(functionLine1);
        
        String withoutAssignmentFunctionLine1 = "test_variable=test_function(14)";
        boolean isWithoutAssignmentFunctionLine1 = PatternFinder.isFunctionPattern(withoutAssignmentFunctionLine1);
        
        String functionLine2 = "test_variable_copy=test_variable_function(test_variable)";
        boolean isFunctionLine2 = PatternFinder.isFunctionPattern(functionLine2);
        
        String functionLine3 = "test_variable_function=test_expression_function(test_variable*2)";
        boolean isFunctionLine3 = PatternFinder.isFunctionPattern(functionLine3);
        
        String negativeFunctionLine4 = "test_variable_copy_function=test_negative_function(-1*-52+4)";
        boolean isNegativeFunctionLine4 = PatternFinder.isFunctionPattern(negativeFunctionLine4);

        String doubleNegativeFunctionLine4 = "test_variable_copy_function=test_negative_function_with_brackets((-1)*-52+(-4))";
        boolean isDoubleNegativeFunctionLine4 = PatternFinder.isFunctionPattern(doubleNegativeFunctionLine4);
        
        String stringFunctionLine4 = "test_variable_copy_function=test_string_function(\"test_variable_copy\"*\"2\")";
        boolean isStringFunctionLine4 = PatternFinder.isFunctionPattern(stringFunctionLine4);
        
        String bracketFunctionLine4 = "test_variable_copy_function=test_complex_bracket_function(\"test variable copy\"*(2+4*(4/6)))";
        boolean isBracketFunctionLine4 = PatternFinder.isFunctionPattern(bracketFunctionLine4);
        
        Assert.assertTrue(isFunctionLine1);
        Assert.assertTrue(isWithoutAssignmentFunctionLine1);
        Assert.assertTrue(isFunctionLine2);
        Assert.assertTrue(isFunctionLine3);
        Assert.assertTrue(isNegativeFunctionLine4);
        Assert.assertTrue(isDoubleNegativeFunctionLine4);
        Assert.assertTrue(isStringFunctionLine4);
        Assert.assertTrue(isBracketFunctionLine4);
    }

    @Test
    public void isNotFunctionPatternTest() {

        String functionLine1 = "test_variable=test_function+14";
        boolean isFunctionLine1 = PatternFinder.isFunctionPattern(functionLine1);
        
        String functionLine2 = "test_variable_copy=test_variable_function+(test_variable)";
        boolean isFunctionLine2 = PatternFinder.isFunctionPattern(functionLine2);
        
        String functionLine3 = "test_variable_function=test_expression_function";
        boolean isFunctionLine3 = PatternFinder.isFunctionPattern(functionLine3);
        
        String operationPlusFunctionLine4 = "test_variable_copy_function=1+test_negative_function(-1*-52+4)";
        boolean isOperationPlusFunctionLine4 = PatternFinder.isFunctionPattern(operationPlusFunctionLine4);

        String bracketFunctionLine4 = "test_variable_copy_function=test_complex(_bracket_function(\"test variable copy\"*(2+4*(4/6)))";
        boolean isBracketFunctionLine4 = PatternFinder.isFunctionPattern(bracketFunctionLine4);
        
        Assert.assertFalse(isFunctionLine1);
        Assert.assertFalse(isFunctionLine2);
        Assert.assertFalse(isFunctionLine3);
        Assert.assertFalse(isOperationPlusFunctionLine4);
        Assert.assertFalse(isBracketFunctionLine4);
    }
}
