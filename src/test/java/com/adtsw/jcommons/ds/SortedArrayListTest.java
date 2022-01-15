package com.adtsw.jcommons.ds;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SortedArrayListTest {

    @Test
    public void testIteration() {

        int retention = 5;
        SortedArrayList<Long> map = new SortedArrayList<>(retention);
        map.insertSorted(1L);
        map.insertSorted(2L);
        map.insertSorted(3L);
        map.insertSorted(4L);
        map.insertSorted(5L);
        map.insertSorted(6L);

        System.out.println("forEach");
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        map.forEach((id) -> {
            System.out.println(id);
            atomicInteger.incrementAndGet();
        });
        Assert.assertEquals(retention, atomicInteger.get());
    }
}
