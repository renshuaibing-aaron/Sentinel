package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * Linked entry within current context.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
class CtEntry extends Entry {

    protected Entry parent = null;
    protected Entry child = null;

    protected ProcessorSlot<Object> chain;
    protected Context context;

    CtEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {

        super(resourceWrapper);
        this.chain = chain;
        this.context = context;

        setUpEntryFor(context);
    }

    private void setUpEntryFor(Context context) {
        // The entry should not be associated to NullContext.
        if (context instanceof NullContext) {
            return;
        }

        //回顾Context的构造过程，curEntry属性在第一次构造并没有给它赋值，因此为null。然后属性curEntry为当前Entry对象
        this.parent = context.getCurEntry();
        if (parent != null) {
            ((CtEntry)parent).child = this;
        }
        context.setCurEntry(this);
    }

    @Override
    public void exit(int count, Object... args) throws ErrorEntryFreeException {
        trueExit(count, args);
    }

    /**
     * 通知slotChain，Entry退出了，其中重点是在StatisticSlot#exit方法中。
     * 如果当前Entry的父Entry为null时，删除context
     * @param context
     * @param count
     * @param args
     * @throws ErrorEntryFreeException
     */
    protected void exitForContext(Context context, int count, Object... args) throws ErrorEntryFreeException {
        System.out.println("【CtEntry#exitForContext】");
        if (context != null) {
            // Null context should exit without clean-up.
            if (context instanceof NullContext) {
                return;
            }
            if (context.getCurEntry() != this) {
                String curEntryNameInContext = context.getCurEntry() == null ? null : context.getCurEntry().getResourceWrapper().getName();
                // Clean previous call stack.
                CtEntry e = (CtEntry)context.getCurEntry();
                while (e != null) {
                    e.exit(count, args);
                    e = (CtEntry)e.parent;
                }
                String errorMessage = String.format("The order of entry exit can't be paired with the order of entry"
                    + ", current entry in context: <%s>, but expected: <%s>", curEntryNameInContext, resourceWrapper.getName());
                throw new ErrorEntryFreeException(errorMessage);
            } else {
                if (chain != null) {
                    chain.exit(context, resourceWrapper, count, args);
                }
                // Restore the call stack.
                // 设置CurEntry为Eenry的父亲
                context.setCurEntry(parent);
                if (parent != null) {
                    ((CtEntry)parent).child = null; // 将父Entry节点的child 置为null
                }
                if (parent == null) {
                    //当前Entry的父Entry为null时，此时说明该Entry已经是最顶层的根节点了，可以清除context（ThreadLocal中）
                    // Default context (auto entered) will be exited automatically.
                    if (ContextUtil.isDefaultContext(context)) {
                        ContextUtil.exit();
                    }
                }
                // Clean the reference of context in current entry to avoid duplicate exit.
                clearEntryContext();
            }
        }
    }

    protected void clearEntryContext() {
        this.context = null;
    }

    @Override
    protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
        exitForContext(context, count, args);

        return parent;
    }

    @Override
    public Node getLastNode() {
        return parent == null ? null : parent.getCurNode();
    }
}