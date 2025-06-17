package com.hiro.goat.core.utils;

public class SnowflakeGenerator {

    private final long datacenterId;
    private final long workerId;

    private final long epoch = 1748700000000L;

    private final long datacenterBits = 5L;
    private final long workerBits = 5L;
    private final long sequenceBits = 12L;

    private final long maxSequence = ~(-1L << sequenceBits);       // 4095

    private final long workerShift = sequenceBits;
    private final long datacenterShift = sequenceBits + workerBits;
    private final long timestampShift = sequenceBits + workerBits + datacenterBits;

    private long lastTimestamp = -1L;

    private long sequence = 0L;

    public SnowflakeGenerator(long datacenterId, long workerId) {
        final long maxDatacenterId = ~(-1L << datacenterBits);
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID need to be in 0~" + maxDatacenterId);
        }

        final long maxWorkerId = ~(-1L << workerBits);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Worker ID need to be in 0~" + maxWorkerId);
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards.");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampShift)
                | (datacenterId << datacenterShift)
                | (workerId << workerShift)
                | sequence;
    }

    private long waitNextMillis(long current) {
        long ts = currentTime();
        while (ts <= current) {
            ts = currentTime();
        }
        return ts;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

}
