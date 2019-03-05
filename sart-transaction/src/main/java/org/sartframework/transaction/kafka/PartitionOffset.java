package org.sartframework.transaction.kafka;

public class PartitionOffset {

    final int partition;

    final long offset;

    public PartitionOffset(int partition, long offset) {
        super();
        this.partition = partition;
        this.offset = offset;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "PartitionOffset [partition=" + partition + ", offset=" + offset + "]";
    }

}
