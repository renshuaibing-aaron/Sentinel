package com.alibaba.csp.sentinel.slots.statistic;

/**
 * 指标类型，例如通过数量、阻塞数量、异常数量、成功数量、响应时间等。
 * @author Eric Zhao
 */
public enum MetricEvent {

    /**
     * Normal pass.
     */
    PASS,
    /**
     * Normal block.
     */
    BLOCK,
    EXCEPTION,
    SUCCESS,
    RT,

    /**
     * Passed in future quota (pre-occupied, since 1.5.0).
     */
    OCCUPIED_PASS
}
