package com.adtsw.jcommons.ds;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SortedHashMapTest {

    @Test
    public void testIteration() {

        int retention = 5;
        SortedHashMap<Long, String> map = new SortedHashMap<>(retention);
        map.put(1L, "1L");
        map.put(2L, "2L");
        map.put(3L, "3L");
        map.put(4L, "4L");
        map.put(5L, "5L");
        map.put(6L, "6L");

        System.out.println("forEach");
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        map.forEach((id, value) -> {
            System.out.println(id + " " + value);
            atomicInteger.incrementAndGet();
        });
        Assert.assertEquals(retention, atomicInteger.get());

        System.out.println("forEach reverse");
        atomicInteger.set(0);
        map.forEach((id, value) -> {
            System.out.println(id + " " + value);
            atomicInteger.incrementAndGet();
        }, true);
        Assert.assertEquals(retention, atomicInteger.get());

        System.out.println("compute");
        atomicInteger.set(0);
        map.compute(new MapIteratorComputer<>() {
            @Override
            public void compute(Long id, String value) {
                System.out.println(id + " " + value);
                atomicInteger.incrementAndGet();
            }
        });
        Assert.assertEquals(retention, atomicInteger.get());

        System.out.println("compute reverse");
        atomicInteger.set(0);
        map.compute(new MapIteratorComputer<>() {
            @Override
            public void compute(Long id, String value) {
                System.out.println(id + " " + value);
                atomicInteger.incrementAndGet();
            }
        }, true);
        Assert.assertEquals(retention, atomicInteger.get());

//        Assert.assertEquals(plaintext, decryptedString);
    }
    
    @Test
    public void testIndexOf() {

        int retention = 5;
        SortedHashMap<Long, String> map = new SortedHashMap<>(retention);
        map.put(1L, "1L");
        map.put(4L, "4L");
        map.put(3L, "3L");
        map.put(2L, "2L");

        Assert.assertEquals(3, map.indexOf(4L));
        Assert.assertEquals(-1, map.indexOf(5L));
    }
}
