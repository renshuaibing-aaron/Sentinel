package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;

/**
 * Common string resource wrapper.
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class StringResourceWrapper extends ResourceWrapper {

    public StringResourceWrapper(String name, EntryType e) {
        super(name, e, ResourceTypeConstants.COMMON);
    }

    public StringResourceWrapper(String name, EntryType e, int resType) {
        super(name, e, resType);
    }

    @Override
    public String getShowName() {
        return name;
    }

    @Override
    public String toString() {
        return "StringResourceWrapper{" +
            "name='" + name + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            '}';
    }
}
