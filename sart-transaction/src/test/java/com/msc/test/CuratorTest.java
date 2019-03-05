package com.msc.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Test;

public class CuratorTest {
    
    private static final String TXN_SEQUENCE = "/dummy/sequences/txn2";
    
    @Test
    public void testCurator() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost").retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();
        client.start();
        final DistributedAtomicLong count = new DistributedAtomicLong(client, TXN_SEQUENCE, new RetryNTimes(10, 10));

        for (int i = 0; i < 10; i++) {
            AtomicValue<Long> value = count.increment();
            // AtomicValue<Long> value = count.decrement();
            // AtomicValue<Long> value = count.add((long)rand.nextInt(20));
            System.out.println("succeed: " + value.succeeded());

            if (value.succeeded())
                System.out.println("Increment: from " + value.preValue() + " to " + value.postValue());
        }
        client.close();
    }
}
