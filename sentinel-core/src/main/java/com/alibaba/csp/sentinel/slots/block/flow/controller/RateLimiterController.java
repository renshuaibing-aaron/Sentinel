package com.alibaba.csp.sentinel.slots.block.flow.controller;

import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.node.Node;

/**
 * @author jialiang.linjl
 */
public class RateLimiterController implements TrafficShapingController {

    // 排队最大时长，默认 500ms
    private final int maxQueueingTimeMs;
    // QPS 设置的值
    private final double count;
    // 上一次请求通过的时间
    private final AtomicLong latestPassedTime = new AtomicLong(-1);

    public RateLimiterController(int timeOut, double count) {
        this.maxQueueingTimeMs = timeOut;
        this.count = count;
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    /**
     * 但是存在一个问题，就是当count值设置过大，或者maxQueueingTimeMs设置过小可能会导致匀速器失效。
     * 建议count设置小于1000，maxQueueingTimeMs大于1
     * @param node resource node
     * @param acquireCount count to acquire
     * @param prioritized whether the request is prioritized
     * @return
     */
    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
        // Pass when acquire count is less or equal than 0.
        if (acquireCount <= 0) {
            return true;
        }
        // Reject when count is less or equal than 0.
        // Otherwise,the costTime will be max of long and waitTime will overflow in some cases.
        if (count <= 0) {
            return false;
        }

        long currentTime = TimeUtil.currentTimeMillis();
        // Calculate the interval between every two requests.
        // count即设置规则时QPS数,costTime 指获取信号所需要的时间
        //也就是两个请求之前的间隔 比如 QPS 限制为 10，那么间隔就是 100ms
        long costTime = Math.round(1.0 * (acquireCount) / count * 1000);

        // Expected pass time of this request.
        //预期通过的时间，latestPassedTime是前一个请求通过的时间
        long expectedTime = costTime + latestPassedTime.get();

        // 第一个请求必定进入该分支，还有一种情况 当count值设置的过大，导致costTime 为0，那么后面的
        // 绝大部分请求都会进入此分支
        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            latestPassedTime.set(currentTime);
            return true;
        } else {
            // Calculate the time to wait.
            // 计算需要的等待时间,即costTime + 上一个请求的通过的时间 - 当前时间
            long waitTime = costTime + latestPassedTime.get() - TimeUtil.currentTimeMillis();
            // 如果等待时间大于阈值，pass 失败
            if (waitTime > maxQueueingTimeMs) {
                return false;
            } else {
                // 尝试通过，设置上一次请求通过时间
                long oldTime = latestPassedTime.addAndGet(costTime);
                try {
                    // 在次获取等待时间
                    waitTime = oldTime - TimeUtil.currentTimeMillis();
                    if (waitTime > maxQueueingTimeMs) {
                        latestPassedTime.addAndGet(-costTime);
                        return false;
                    }
                    // in race condition waitTime may <= 0
                    if (waitTime > 0) {
                        Thread.sleep(waitTime);
                    }
                    return true;
                } catch (InterruptedException e) {
                }
            }
        }
        return false;
    }

}
