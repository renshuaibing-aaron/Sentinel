package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 *
 *  todo  Context即上下文，一个上下文只能通过两种方式创建，一是暴露出来的ContextUtil.enter() 方式，
 *   而是内部ContextUtil的一个子类创建，这是默认的内部创建方式，为一次访问资源创建上下文。
 *   Context是保存在ThradLocal中。可以把Conetext理解当一次访问资源的过程。Context中维护着当前调用链的元数据
 * This class holds metadata of current invocation:<br/>
 *   这个是不是跟线程进行绑定的
 * <ul>
 * <li>the {@link EntranceNode}: the root of the current invocation
 * tree.</li>
 * <li>the current {@link Entry}: the current invocation point.</li>
 * <li>the current {@link Node}: the statistics related to the
 * {@link Entry}.</li>
 * <li>the origin: The origin is useful when we want to control different
 * invoker/consumer separately. Usually the origin could be the Service Consumer's app name
 * or origin IP. </li>
 * </ul>
 * <p>
 * Each {@link SphU}#entry() or {@link SphO}#entry() should be in a {@link Context},
 * if we don't invoke {@link ContextUtil}#enter() explicitly, DEFAULT context will be used.
 * </p>
 * <p>
 * A invocation tree will be created if we invoke {@link SphU}#entry() multi times in
 * the same context.
 * </p>
 * <p>
 * Same resource in different context will count separately, see {@link NodeSelectorSlot}.
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 * @see ContextUtil
 * @see NodeSelectorSlot
 */
public class Context {

    /**
     * Context name.
     */
    private final String name;

    /**当前调用链的入口节点,即最外层的entry
     * The entrance node of current invocation tree.
     */
    private DefaultNode entranceNode;

    /** 调用链的当前节点
     * Current processing entry.
     */
    private Entry curEntry;

    /**当前调用链的调用源
     * The origin of this context (usually indicate different invokers, e.g. service consumer name or origin IP).
     */
    private String origin = "";

    private final boolean async;

    /**
     * Create a new async context.
     *
     * @param entranceNode entrance node of the context
     * @param name context name
     * @return the new created context
     * @since 0.2.0
     */
    public static Context newAsyncContext(DefaultNode entranceNode, String name) {
        return new Context(name, entranceNode, true);
    }

    public Context(DefaultNode entranceNode, String name) {
        this(name, entranceNode, false);
    }

    public Context(String name, DefaultNode entranceNode, boolean async) {
        this.name = name;
        this.entranceNode = entranceNode;
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    public String getName() {
        return name;
    }

    public Node getCurNode() {
        return curEntry == null ? null : curEntry.getCurNode();
    }

    public Context setCurNode(Node node) {
        this.curEntry.setCurNode(node);
        return this;
    }

    public Entry getCurEntry() {
        return curEntry;
    }

    public Context setCurEntry(Entry curEntry) {
        this.curEntry = curEntry;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Context setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public double getOriginTotalQps() {
        return getOriginNode() == null ? 0 : getOriginNode().totalQps();
    }

    public double getOriginBlockQps() {
        return getOriginNode() == null ? 0 : getOriginNode().blockQps();
    }

    public double getOriginPassReqQps() {
        return getOriginNode() == null ? 0 : getOriginNode().successQps();
    }

    public double getOriginPassQps() {
        return getOriginNode() == null ? 0 : getOriginNode().passQps();
    }

    public long getOriginTotalRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().totalRequest();
    }

    public long getOriginBlockRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().blockRequest();
    }

    public double getOriginAvgRt() {
        return getOriginNode() == null ? 0 : getOriginNode().avgRt();
    }

    public int getOriginCurThreadNum() {
        return getOriginNode() == null ? 0 : getOriginNode().curThreadNum();
    }

    public DefaultNode getEntranceNode() {
        return entranceNode;
    }

    /**
     * Get the parent {@link Node} of the current.
     *
     * @return the parent node of the current.
     */
    public Node getLastNode() {
        if (curEntry != null && curEntry.getLastNode() != null) {
            return curEntry.getLastNode();
        } else {
            return entranceNode;
        }
    }

    public Node getOriginNode() {
        return curEntry == null ? null : curEntry.getOriginNode();
    }

    @Override
    public String toString() {
        return "Context{" +
            "name='" + name + '\'' +
            ", entranceNode=" + entranceNode +
            ", curEntry=" + curEntry +
            ", origin='" + origin + '\'' +
            ", async=" + async +
            '}';
    }
}
