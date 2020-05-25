package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.List;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 *
 * metric是sentinel中用来进行实时数据统计的度量接口，node就是通过metric来进行数据统计的。
 * Represents a basic structure recording invocation metrics of protected resources.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public interface Metric extends DebugSupport {

    /**
     * Get total success count.
     *
     * @return success count
     */
    long success();

    /**
     * Get max success count.
     *
     * @return max success count
     */
    long maxSuccess();

    /**
     * Get total exception count.
     *
     * @return exception count
     */
    long exception();

    /**
     * Get total block count.
     *
     * @return block count
     */
    long block();

    /**
     * Get total pass count. not include {@link #occupiedPass()}
     *
     * @return pass count
     */
    long pass();

    /**
     * Get total response time.
     *
     * @return total RT
     */
    long rt();

    /**
     * Get the minimal RT.
     *
     * @return minimal RT
     */
    long minRt();

    /**
     * Get aggregated metric nodes of all resources.
     *
     * @return metric node list of all resources
     */
    List<MetricNode> details();

    /**
     * Generate aggregated metric items that satisfies the time predicate.
     *
     * @param timePredicate time predicate
     * @return aggregated metric items
     * @since 1.7.0
     */
    List<MetricNode> detailsOnCondition(Predicate<Long> timePredicate);

    /**
     * Get the raw window array.
     *
     * @return window metric array
     */
    MetricBucket[] windows();

    /**
     * Add current exception count.
     *
     * @param n count to add
     */
    void addException(int n);

    /**
     * Add current block count.
     *
     * @param n count to add
     */
    void addBlock(int n);

    /**
     * Add current completed count.
     *
     * @param n count to add
     */
    void addSuccess(int n);

    /**
     * Add current pass count.
     *
     * @param n count to add
     */
    void addPass(int n);

    /**
     * Add given RT to current total RT.
     *
     * @param rt RT
     */
    void addRT(long rt);

    /**
     * Get the sliding window length in seconds.
     *
     * @return the sliding window length
     */
    double getWindowIntervalInSec();

    /**
     * Get sample count of the sliding window.
     *
     * @return sample count of the sliding window.
     */
    int getSampleCount();

    /**
     * Note: this operation will not perform refreshing, so will not generate new buckets.
     *
     * @param timeMillis valid time in ms
     * @return pass count of the bucket exactly associated to provided timestamp, or 0 if the timestamp is invalid
     * @since 1.5.0
     */
    long getWindowPass(long timeMillis);

    // Occupy-based (@since 1.5.0)

    /**
     * Add occupied pass, which represents pass requests that borrow the latter windows' token.
     *
     * @param acquireCount tokens count.
     * @since 1.5.0
     */
    void addOccupiedPass(int acquireCount);

    /**
     * Add request that occupied.
     *
     * @param futureTime   future timestamp that the acquireCount should be added on.
     * @param acquireCount tokens count.
     * @since 1.5.0
     */
    void addWaiting(long futureTime, int acquireCount);

    /**
     * Get waiting pass account
     *
     * @return waiting pass count
     * @since 1.5.0
     */
    long waiting();

    /**
     * Get occupied pass count.
     *
     * @return occupied pass count
     * @since 1.5.0
     */
    long occupiedPass();

    // Tool methods.

    long previousWindowBlock();

    long previousWindowPass();
}
