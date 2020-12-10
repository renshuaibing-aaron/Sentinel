package com.alibaba.csp.sentinel.slots.statistic.base;

/**
 * todo
 *    WindowWrap封装了表示一个时间窗口，它有三个重要的属性
 *    其内部的数据结构用 MetricBucket 表示
 * Wrapper entity class for a period of time window.
 * @param <T> data type
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class WindowWrap<T> {

    /**
     *  窗口的长度 单位是毫秒
     * Time length of a single window bucket in milliseconds.
     */
    private final long windowLengthInMs;

    /**
     * 起始毫秒数
     * Start timestamp of the window in milliseconds.
     */
    private long windowStart;

    /**
     * 指标数据，用泛型表示 一般是MetricBucket,
     * see@MetricBucket
     * Statistic data.
     */
    private T value;

    /**
     * @param windowLengthInMs a single window bucket's time length in milliseconds.
     * @param windowStart      the start timestamp of the window
     * @param value            statistic data
     */
    public WindowWrap(long windowLengthInMs, long windowStart, T value) {
        this.windowLengthInMs = windowLengthInMs;
        this.windowStart = windowStart;
        this.value = value;
    }

    public long windowLength() {
        return windowLengthInMs;
    }

    public long windowStart() {
        return windowStart;
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Reset start timestamp of current bucket to provided time.
     *
     * @param startTime valid start timestamp
     * @return bucket after reset
     */
    public WindowWrap<T> resetTo(long startTime) {
        this.windowStart = startTime;
        return this;
    }

    /**
     * Check whether given timestamp is in current bucket.
     *
     * @param timeMillis valid timestamp in ms
     * @return true if the given time is in current bucket, otherwise false
     * @since 1.5.0
     */
    public boolean isTimeInWindow(long timeMillis) {
        return windowStart <= timeMillis && timeMillis < windowStart + windowLengthInMs;
    }

    @Override
    public String toString() {
        return "WindowWrap{" +
            "windowLengthInMs=" + windowLengthInMs +
            ", windowStart=" + windowStart +
            ", value=" + value +
            '}';
    }
}
