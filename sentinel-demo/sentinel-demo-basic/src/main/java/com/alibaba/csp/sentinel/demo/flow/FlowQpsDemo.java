package com.alibaba.csp.sentinel.demo.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_DEFAULT;

/**
 * @author jialiang.linjl
 */
public class FlowQpsDemo {

    // 资源名
    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger num = new AtomicInteger();

    private static volatile boolean stop = false;

    private static final int threadCount = 32;

    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {
        initFlowQpsRule();

        tick();
        // first make the system run on a very low condition
        simulateTraffic();

        System.out.println("===== begin to do flow control");
        System.out.println("only 20 requests per second can pass");

    }

    /**
     * 流量控制规则
     */
    private static void initFlowQpsRule() {
        // 规则对应的类为FlowRule，用List保存，可以有多个规则
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        //setResource(KEY)方法是设置资源名，也就是限流规则的作用对象，更通俗的讲：本条规则对哪个资源生效。
        rule1.setResource(KEY);

        //count是限流阈值，当我们定义的是流量控制规则是根据QPS进行限流时，它表示QPS的阈值，当然如果是根据线程数限流，它表示线程数。
        // set limit qps to 20
        rule1.setCount(1);


        // 设置限流类型：根据qps
        //grade表示限流阈值类型，是按照 QPS 还是线程数默认根据 QPS
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);

       //controlBehavior表示发流量控制模式，默认超过阈值是直接拒绝，
        // 如果设置为匀速器模式等待则还需要设置maxQueueingTimeMs（最大排队时间）还有一种是冷启动方式
        /**
         * 根据线程数的流量限制也是类似。只是规则设置代码不同。还有一个问题就是根据线程数的流量限制中，Sentinel没有对线程的控制权限，内部只是对请求线程的统计。如果超出阈值，新的请求会被立即拒绝。但是根据QPS进行流量控制中可以有多个选择：1. 直接拒绝（CONTROL_BEHAVIOR_DEFAULT），2. 慢启动也叫冷启动（CONTROL_BEHAVIOR_WARM_UP）过"冷启动"，让通过的流量缓慢增加，在一定时间内逐渐增加到阈值上限，给冷系统一个预热的时间，避免冷系统被压垮的情况。，3. 匀速通过（CONTROL_BEHAVIOR_RATE_LIMITER），这种方式严格控制了请求通过的间隔时间，也即是让请求以均匀的速度通过，内部实现是漏桶算法
         */
        rule1.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        rule1.setLimitApp("default");
        rules.add(rule1);

        //加载限流的规则
        FlowRuleManager.loadRules(rules);
    }

    private static void simulateTraffic() {
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("simulate-traffic-Task");
            t.start();
        }
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("begin to statistic!!!");

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.println(seconds + " send qps is: " + oneSecondTotal);
                System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                    + ", pass:" + oneSecondPass
                    + ", block:" + oneSecondBlock);

                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get());
            System.exit(0);
        }
    }

    static class RunTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;

                try {
                    //这个方法会去申请一个entry，如果能够申请成功，则说明没有被限流，否则会抛出BlockException，表明已经被限流了
                    //
                    entry = SphU.entry(KEY);
                    // token acquired, means pass
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    num.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                } catch (InterruptedException e) {
                    // ignore
                }
              /*  if(num.get()==2){
                    stop=true;
                }*/
            }
        }
    }
}
