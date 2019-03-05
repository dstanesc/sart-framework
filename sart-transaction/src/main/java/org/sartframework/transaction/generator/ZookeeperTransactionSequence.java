package org.sartframework.transaction.generator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.sartframework.config.SartConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//https://curator.apache.org/curator-recipes/index.html

@Component
public class ZookeeperTransactionSequence implements TransactionSequence {

    final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperTransactionSequence.class);

    final static String SART_TRANSACTION_SEQUENCE = "/sart/sequences/txnid";

    SartConfiguration sartConfiguration;

    CuratorFramework client;

    DistributedAtomicLong sequence;

    @Autowired
    public ZookeeperTransactionSequence(SartConfiguration sartConfiguration) {
        super();
        client = CuratorFrameworkFactory.builder().connectString("localhost").retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
    }

    @Override
    public TransactionSequence start() {
        this.client.start();
        try {
            this.client.blockUntilConnected();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.sequence = new DistributedAtomicLong(client, SART_TRANSACTION_SEQUENCE, new RetryNTimes(10, 10));
        return this;
    }

    @Override
    public TransactionSequence stop() {
        client.close();
        return this;
    }

    @Override
    public long next() {

        AtomicValue<Long> value;

        try {
            value = sequence.increment();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (value.succeeded()) {
            long counter = value.postValue();
            LOGGER.info("generated xid={}", counter);
            return counter;
        } else
            throw new RuntimeException("Cannot obtain seqence number");

    }

}
