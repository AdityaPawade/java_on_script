package com.adtsw.jcommons.jmx;

import com.adtsw.jcommons.jmx.exceptions.EmptyJmxMetricListenerListException;
import com.adtsw.jcommons.jmx.exceptions.MetricNotAvailableException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JmxMetricProviderServiceTest {

    @Test
    public void testJmxMetrics() {

        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("processing");
                    Thread.sleep(5 * 1000);
                    System.out.println("done");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        })).start();

        List<String> memoryPools = Arrays.asList(
            "java.lang:name=G1 Old Gen,type=MemoryPool",
            "java.lang:name=G1 Eden Space,type=MemoryPool",
            "java.lang:name=G1 Survivor Space,type=MemoryPool"
        );

        try {
            (new JmxMetricProvider(Arrays.asList(new JmxMetricsListener() {

                @Override
                public void metricChange(JmxMetric metric) {
                    if(memoryPools.contains(metric.getObjectName()) &&
                        metric.getName().equals("Usage")) {
                        try {
                            Object value = ((long) metric.getValue()) / 1024L / 1024L;
                            System.out.println(metric + "\n" + value);
                        } catch (MetricNotAvailableException e) {
                            System.out.println(e);
                        }
                    }
                }

                @Override
                public void metricRemoval(JmxMetric metric) {
                    System.out.println("asd");
                }

                @Override
                public void close() {

                }
            }))).start(1, 10, TimeUnit.SECONDS);
        } catch (EmptyJmxMetricListenerListException e) {
            System.out.println(e);
        }

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
