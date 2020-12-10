package com.alibaba.csp.sentinel.slots.statistic.data;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.statistic.MetricEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.LongAdder;

/**
 * Represents metrics data in a period of time span.
 *MetricBucket代表了在某一个时间窗口内的所有数据，如RT、Success、Pass、Block等等。
 *   实际上就可以理解为一个Widow
 *   指标桶，例如通过数量、阻塞数量、异常数量、成功数量、响应时间，已通过未来配额（抢占下一个滑动窗口的数量）
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class MetricBucket {

    /**
     * JDK1.8中新添加的一种原子类，对于数据统计场景比Atomic原子类具有更好的并发性
     */
    private final LongAdder[] counters;

    private volatile long minRt;

    public MetricBucket() {
        MetricEvent[] events = MetricEvent.values();
        this.counters = new LongAdder[events.length];

        for (MetricEvent event : events) {
            counters[event.ordinal()] = new LongAdder();
        }
        initMinRt();
    }

    public MetricBucket reset(MetricBucket bucket) {
        for (MetricEvent event : MetricEvent.values()) {
            counters[event.ordinal()].reset();
            counters[event.ordinal()].add(bucket.get(event));
        }
        initMinRt();
        return this;
    }

    private void initMinRt() {
        this.minRt = SentinelConfig.statisticMaxRt();
    }

    /**
     * Reset the adders.
     *
     * @return new metric bucket in initial state
     */
    public MetricBucket reset() {
        for (MetricEvent event : MetricEvent.values()) {
            counters[event.ordinal()].reset();
        }
        initMinRt();
        return this;
    }

    public long get(MetricEvent event) {
        return counters[event.ordinal()].sum();
    }

    public MetricBucket add(MetricEvent event, long n) {
        counters[event.ordinal()].add(n);
        return this;
    }

    public long pass() {
        return get(MetricEvent.PASS);
    }

    public long occupiedPass() {
        return get(MetricEvent.OCCUPIED_PASS);
    }

    public long block() {
        return get(MetricEvent.BLOCK);
    }

    public long exception() {
        return get(MetricEvent.EXCEPTION);
    }

    public long rt() {
        return get(MetricEvent.RT);
    }

    public long minRt() {
        return minRt;
    }

    public long success() {
        return get(MetricEvent.SUCCESS);
    }

    public void addPass(int n) {
        add(MetricEvent.PASS, n);
    }

    public void addOccupiedPass(int n) {
        add(MetricEvent.OCCUPIED_PASS, n);
    }

    public void addException(int n) {
        add(MetricEvent.EXCEPTION, n);
    }

    public void addBlock(int n) {
        add(MetricEvent.BLOCK, n);
    }

    public void addSuccess(int n) {
        add(MetricEvent.SUCCESS, n);
    }

    public void addRT(long rt) {
        add(MetricEvent.RT, rt);

        // Not thread-safe, but it's okay.
        if (rt < minRt) {
            minRt = rt;
        }
    }

    @Override
    public String toString() {
        return "p: " + pass() + ", b: " + block() + ", w: " + occupiedPass();
    }
}
