package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.node.Node;

/**
 * A universal interface for traffic shaping controller.
 *
 * @author jialiang.linjl
 */
public interface TrafficShapingController {

    /**
     * Check whether given resource entry can pass with provided count.
     *
     * @param node resource node
     * @param acquireCount count to acquire
     * @param prioritized whether the request is prioritized
     * @return true if the resource entry can pass; false if it should be blocked
     */
    boolean canPass(Node node, int acquireCount, boolean prioritized);

    /**
     * Check whether given resource entry can pass with provided count.
     *
     * @param node resource node
     * @param acquireCount count to acquire
     * @return true if the resource entry can pass; false if it should be blocked
     */
    boolean canPass(Node node, int acquireCount);
}
