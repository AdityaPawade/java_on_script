package com.adtsw.jos.dsl.utils;

import org.junit.Assert;
import org.junit.Test;

public class ObjectLoaderTest {
    
    @Test
    public void testGetLexemes() {
        
        Object obj = ObjectLoader.getObject("1D");
        Assert.assertTrue(obj instanceof String);

        obj = ObjectLoader.getObject("1.10412");
        Assert.assertTrue(obj instanceof Double);

        obj = ObjectLoader.getObject("1");
        Assert.assertTrue(obj instanceof Integer);

        obj = ObjectLoader.getObject(String.valueOf(Integer.MAX_VALUE) + "0");
        Assert.assertTrue(obj instanceof Long);
    }
}
