package com.alibaba.csp.sentinel.slotchain;

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.util.IdUtil;
import com.alibaba.csp.sentinel.util.MethodUtil;

/**
 * Resource wrapper for method invocation.
 *
 * @author qinan.qn
 */
public class MethodResourceWrapper extends ResourceWrapper {

    private final transient Method method;



    public MethodResourceWrapper(Method method, EntryType e) {
        this(method, e, ResourceTypeConstants.COMMON);
    }

    public MethodResourceWrapper(Method method, EntryType e, int resType) {

        // 通过反射获取到一个方法的唯一名称，重载/重写的的方法获取的结果不同
        super(MethodUtil.resolveMethodName(method), e, resType);
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String getShowName() {
        return name;
    }

    @Override
    public String toString() {
        return "MethodResourceWrapper{" +
            "name='" + name + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            '}';
    }
}
