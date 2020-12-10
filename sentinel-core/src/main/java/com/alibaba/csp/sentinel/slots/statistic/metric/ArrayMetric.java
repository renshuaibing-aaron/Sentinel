package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.MetricEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.metric.occupy.OccupiableBucketLeapArray;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * The basic metric class in Sentinel using a {@link BucketLeapArray} internal.
 *   它相当于是对底层指标的代理封装 滑动窗口核心实现类
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class ArrayMetric implements Metric {

    //ArrayMetric 类唯一的属性，用来存储各个窗口的数据
    private final LeapArray<MetricBucket> data;


    //ArrayMetric 将真正的信息放在LeapArray<MetricBucket> data 里面 创建这个需要两个参数
    //滑动窗口的个数 2个 ,整个统计的时常毫秒为单位
    /**
     *
     * @param sampleCount 在一个采集间隔中抽样的个数，默认为 2，例如当 intervalInMs = 1000时，抽象两次，
     *                    则一个采集间隔中会包含两个相等的区间，一个区间就是滑动窗口
     * @param intervalInMs  表示一个采集的时间间隔，例如1秒，1分钟
     */
    public ArrayMetric(int sampleCount, int intervalInMs) {
        // SAMPLE_COUNT=2  INTERVAL=1000
        this.data = new OccupiableBucketLeapArray(sampleCount, intervalInMs);
    }

    /**
     * OccupiableBucketLeapArray和BucketLeapArray数据结构保存了实时的指标数据，
     * 简单的说是一个数组,数组元素是WindowWrap(窗口桶)。这两个类都继承自LeapArray类
     * @param sampleCount
     * @param intervalInMs
     * @param enableOccupy 是否允许抢占，即当前时间戳已经达到限制后，是否可以占用下一个时间窗口的容量，
     *                     这里对应 LeapArray 的两个实现类，如果允许抢占，则为 OccupiableBucketLeapArray，否则为 BucketLeapArray
     */
    public ArrayMetric(int sampleCount, int intervalInMs, boolean enableOccupy) {
        if (enableOccupy) {
            this.data = new OccupiableBucketLeapArray(sampleCount, intervalInMs);
        } else {
            this.data = new BucketLeapArray(sampleCount, intervalInMs);
        }
    }

    /**
     * For unit test.
     */
    public ArrayMetric(LeapArray<MetricBucket> array) {
        this.data = array;
    }

    @Override
    public long success() {
        data.currentWindow();
        long success = 0;

        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            success += window.success();
        }
        return success;
    }

    @Override
    public long maxSuccess() {
        data.currentWindow();
        long success = 0;

        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.success() > success) {
                success = window.success();
            }
        }
        return Math.max(success, 1);
    }

    @Override
    public long exception() {
        data.currentWindow();
        long exception = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            exception += window.exception();
        }
        return exception;
    }

    @Override
    public long block() {
        data.currentWindow();
        long block = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            block += window.block();
        }
        return block;
    }

    @Override
    public long pass() {
        data.currentWindow(); // 这里的作用是刷新窗口 避免获取过期的数据。
        long pass = 0;
        // 获取数组中所有WindoWrap关联的MetricBucket
        List<MetricBucket> list = data.values();
        // 遍历所有MetricBucket，获取pass数量
        for (MetricBucket window : list) {
            pass += window.pass();
        }
        return pass;
    }

    @Override
    public long occupiedPass() {
        data.currentWindow();
        long pass = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            pass += window.occupiedPass();
        }
        return pass;
    }

    @Override
    public long rt() {
        data.currentWindow();
        long rt = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            rt += window.rt();
        }
        return rt;
    }

    @Override
    public long minRt() {
        data.currentWindow();
        long rt = SentinelConfig.statisticMaxRt();
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.minRt() < rt) {
                rt = window.minRt();
            }
        }

        return Math.max(1, rt);
    }

    @Override
    public List<MetricNode> details() {
        List<MetricNode> details = new ArrayList<>();
        data.currentWindow();
        List<WindowWrap<MetricBucket>> list = data.list();
        for (WindowWrap<MetricBucket> window : list) {
            if (window == null) {
                continue;
            }

            details.add(fromBucket(window));
        }

        return details;
    }

    @Override
    public List<MetricNode> detailsOnCondition(Predicate<Long> timePredicate) {
        List<MetricNode> details = new ArrayList<>();
        data.currentWindow();
        List<WindowWrap<MetricBucket>> list = data.list();
        for (WindowWrap<MetricBucket> window : list) {
            if (window == null) {
                continue;
            }
            if (timePredicate != null && !timePredicate.test(window.windowStart())) {
                continue;
            }

            details.add(fromBucket(window));
        }

        return details;
    }

    private MetricNode fromBucket(WindowWrap<MetricBucket> wrap) {
        MetricNode node = new MetricNode();
        node.setBlockQps(wrap.value().block());
        node.setExceptionQps(wrap.value().exception());
        node.setPassQps(wrap.value().pass());
        long successQps = wrap.value().success();
        node.setSuccessQps(successQps);
        if (successQps != 0) {
            node.setRt(wrap.value().rt() / successQps);
        } else {
            node.setRt(wrap.value().rt());
        }
        node.setTimestamp(wrap.windowStart());
        node.setOccupiedPassQps(wrap.value().occupiedPass());
        return node;
    }

    @Override
    public MetricBucket[] windows() {
        data.currentWindow();
        return data.values().toArray(new MetricBucket[0]);
    }

    @Override
    public void addException(int count) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addException(count);
    }

    @Override
    public void addBlock(int count) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addBlock(count);
    }

    @Override
    public void addWaiting(long time, int acquireCount) {
        data.addWaiting(time, acquireCount);
    }

    @Override
    public void addOccupiedPass(int acquireCount) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addOccupiedPass(acquireCount);
    }

    @Override
    public void addSuccess(int count) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addSuccess(count);
    }

    @Override
    public void addPass(int count) {
        System.out.println("【addPass】");
        // 获取当前窗口  窗口的大小是sampleCount=2
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        //对MetricBucket +1 操作
        wrap.value().addPass(count);
    }

    @Override
    public void addRT(long rt) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addRT(rt);
    }

    @Override
    public void debug() {
        data.debug(System.currentTimeMillis());
    }

    @Override
    public long previousWindowBlock() {
        data.currentWindow();
        WindowWrap<MetricBucket> wrap = data.getPreviousWindow();
        if (wrap == null) {
            return 0;
        }
        return wrap.value().block();
    }

    @Override
    public long previousWindowPass() {
        data.currentWindow();
        WindowWrap<MetricBucket> wrap = data.getPreviousWindow();
        if (wrap == null) {
            return 0;
        }
        return wrap.value().pass();
    }

    public void add(MetricEvent event, long count) {
        data.currentWindow().value().add(event, count);
    }

    public long getCurrentCount(MetricEvent event) {
        return data.currentWindow().value().get(event);
    }

    /**
     * Get total sum for provided event in {@code intervalInSec}.
     *
     * @param event event to calculate
     * @return total sum for event
     */
    public long getSum(MetricEvent event) {
        data.currentWindow();
        long sum = 0;

        List<MetricBucket> buckets = data.values();
        for (MetricBucket bucket : buckets) {
            sum += bucket.get(event);
        }
        return sum;
    }

    /**
     * Get average count for provided event per second.
     *
     * @param event event to calculate
     * @return average count per second for event
     */
    public double getAvg(MetricEvent event) {
        return getSum(event) / data.getIntervalInSecond();
    }

    @Override
    public long getWindowPass(long timeMillis) {
        MetricBucket bucket = data.getWindowValue(timeMillis);
        if (bucket == null) {
            return 0L;
        }
        return bucket.pass();
    }

    @Override
    public long waiting() {
        return data.currentWaiting();
    }

    @Override
    public double getWindowIntervalInSec() {
        return data.getIntervalInSecond();
    }

    @Override
    public int getSampleCount() {
        return data.getSampleCount();
    }
}
