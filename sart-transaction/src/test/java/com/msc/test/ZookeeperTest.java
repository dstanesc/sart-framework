package com.msc.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperTest {

    private static final String DUMMY_SEQUENCE = "/dummy-sequence";

    private static final String TXN_SEQUENCE = "/dummy/sequences/txn";

    public static class ByteUtils {

        private ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        public byte[] longToBytes(long x) {
            buffer.putLong(0, x);
            return buffer.array();
        }

        public long bytesToLong(byte[] bytes) {
            buffer.put(bytes, 0, bytes.length);
            buffer.flip();// need flip
            return buffer.getLong();
        }
    }

    final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperTest.class);

    CountDownLatch connectionLatch = new CountDownLatch(1);

    // @Test
    public void testConnect() throws Exception {
        ZooKeeper zoo = new ZooKeeper("localhost", 2000, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            }
        });

        connectionLatch.await();

        long sessionId = zoo.getSessionId();

        LOGGER.info("Zookeeper session is {}", sessionId);

        long counter = 1;

        byte[] data = new ByteUtils().longToBytes(counter);

        String sequenceNode = zoo.create(DUMMY_SEQUENCE, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        LOGGER.info("Node created {}", sequenceNode);

        Stat stat = zoo.exists(DUMMY_SEQUENCE, false);

        LOGGER.info("Node exists  1 {}", stat);

        byte[] out = zoo.getData(DUMMY_SEQUENCE, false, stat);

        long newCounter = new ByteUtils().bytesToLong(out);

        LOGGER.info("Node get first {}", newCounter);

        newCounter = newCounter + 1;

        byte[] data2 = new ByteUtils().longToBytes(newCounter);

        zoo.setData(DUMMY_SEQUENCE, data2, stat.getVersion());

        Stat stat2 = zoo.exists(DUMMY_SEQUENCE, false);

        LOGGER.info("Node exists 2 {}", stat2);

        byte[] out2 = zoo.getData(DUMMY_SEQUENCE, false, stat);

        long newCounter2 = new ByteUtils().bytesToLong(out2);

        LOGGER.info("Node get second {}", newCounter2);

        Stat stat3 = zoo.exists(DUMMY_SEQUENCE, false);

        zoo.delete(DUMMY_SEQUENCE, stat3.getVersion());

        zoo.close();
    }

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
