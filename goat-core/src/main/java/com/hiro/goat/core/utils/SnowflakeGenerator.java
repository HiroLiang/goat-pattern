package com.hiro.goat.core.utils;

import com.hiro.goat.api.identity.IdentityGenerator;

public class SnowflakeGenerator implements IdentityGenerator {

    /**
     * if you need to use the other generator, define another epoch
     */
    private final long epoch;

    /**
     * allow 0 ~ 1023 number of devices
     */
    private final long deviceId;
    private final long deviceBits = 10L;

    /**
     * last ID's generate timestamp
     */
    private long lastTimestamp = -1L;

    /**
     * sequence number allow 0 ~ 4095
     */
    private long sequence = 0L;

    /**
     * Constructor
     * @param deviceId allow 0 ~ 1023 devices
     */
    public SnowflakeGenerator(long deviceId) {
        this.epoch = 1695657600000L;
        final long maxDatacenterId = ~(-1L << deviceBits);
        if (deviceId > maxDatacenterId || deviceId < 0) {
            throw new IllegalArgumentException("Datacenter ID need to be in 0~" + maxDatacenterId);
        }
        this.deviceId = deviceId;
    }

    /**
     * Get next ID
     * @return long
     */
    @Override
    public synchronized long nextId() {
        final long sequenceBits = 12L;

        final long maxSequence = ~(-1L << sequenceBits);

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

        return ((timestamp - epoch) << deviceBits + sequenceBits)
                | (deviceId << sequenceBits)
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
