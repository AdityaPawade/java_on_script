package com.adtsw.jos.dsl.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ExpressionEvaluatorTest {

    @Test
    public void testExpressionEvaluator() {

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Object result;

        result = evaluator.evaluate("2.0*3.0/(4.0+4.0)");
        Assert.assertEquals(0.75D, (Double) result, 0D);

        result = evaluator.evaluate("2.0%3.0");
        Assert.assertEquals(2.0D, (Double) result, 0D);

        result = evaluator.evaluate(Arrays.asList(2, "*", 3, "/", "(", 4, "+", 4, ")").toArray());
        Assert.assertEquals(0, (Integer) result, 0D);

        result = evaluator.evaluate(Arrays.asList(2.0, "*", 3.0, "/", "(", 4.0, "+", 4.0, ")").toArray());
        Assert.assertEquals(0.75D, (Double) result, 0D);

        result = evaluator.evaluate(Arrays.asList(
            2, ">=", 3, "||", "(", 4, "<=", 5, "&&", true, "&&", 4, "!=", "(", 4, "/", 2, ")", ")"
        ).toArray());
        Assert.assertTrue((Boolean) result);

        result = evaluator.evaluate(Arrays.asList(2.0, ">=", 1.0, "&&", 4.0, "==", 4.0).toArray());
        Assert.assertTrue((Boolean) result);

        result = evaluator.evaluate(Arrays.asList("!", false).toArray());
        Assert.assertTrue((Boolean) result);

        result = evaluator.evaluate(Arrays.asList(2.0, ">=", 1.0, "&&", "!", false).toArray());
        Assert.assertTrue((Boolean) result);
        
        result = evaluator.evaluate(Arrays.asList(1647923160L, "%", 2.0, "==", 0).toArray());
        Assert.assertTrue((Boolean) result);
    }
}