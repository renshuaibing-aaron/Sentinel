package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.init.InitExecutor;

/**
 * Sentinel Env. This class will trigger all initialization for Sentinel.
 *
 * <p>
 * NOTE: to prevent deadlocks, other classes' static code block or static field should
 * NEVER refer to this class.
 * </p>
 *
 * @author jialiang.linjl
 */
public class Env {


    //真正的访问资源入口
    public static final Sph sph = new CtSph();

    static {
        // If init f    ails, the process will exit.
        //doInit方法会调用sendHeadBeat
        //对实时数据传输的底层通信，规则数据持久化等进行初始化。
        InitExecutor.doInit();
    }

}
