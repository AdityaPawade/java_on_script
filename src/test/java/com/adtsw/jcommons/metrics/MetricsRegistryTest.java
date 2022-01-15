package com.adtsw.jcommons.metrics;

import org.junit.Assert;
import org.junit.Test;

public class MetricsRegistryTest {

    @Test
    public void testEncryption() throws InterruptedException {

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        metricsRegistry.incrementCounter("c1");
        metricsRegistry.incrementCounter("c1", 2L);
        metricsRegistry.incrementCounter("c1", 3L);

        TimerContext t1Context = metricsRegistry.startTimer("t1");
        Thread.sleep(2 * 1000);
        metricsRegistry.stopTimer(t1Context);
        TimerContext t1Context2 = metricsRegistry.startTimer("t1");
        Thread.sleep(1000);
        metricsRegistry.stopTimer(t1Context2);
        TimerContext t1Context3 = metricsRegistry.startTimer("t1");
        Thread.sleep(3 * 1000);
        metricsRegistry.stopTimer(t1Context3);
        
        metricsRegistry.setStat("s1", 22L);
        metricsRegistry.setStat("s1", 2L);
        metricsRegistry.setStat("s1", 100L);

        System.out.println(metricsRegistry);

        CounterValue actualC1Value = metricsRegistry.getCounter("c1");
        Assert.assertEquals(6L, (long) actualC1Value.getVal());
    }
}
