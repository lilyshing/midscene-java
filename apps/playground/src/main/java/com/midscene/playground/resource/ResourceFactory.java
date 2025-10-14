package com.midscene.playground.resource;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * 资源工厂抽象类 - 用于创建和管理可池化的资源对象
 * 提供了简化的API，封装了Apache Commons Pool2的复杂性
 */
public abstract class ResourceFactory<T> extends BasePooledObjectFactory<T> {

    /**
     * 创建新的资源对象
     */
    protected abstract T doCreate() throws Exception;

    /**
     * 验证资源对象是否有效
     */
    protected abstract boolean doValidate(T resource);

    /**
     * 销毁资源对象
     */
    protected abstract void doDestroy(T resource);

    /**
     * 激活资源对象
     */
    protected void doActivate(T resource) {
        // 默认实现为空
    }

    /**
     * 钝化资源对象
     */
    protected void doPassivate(T resource) {
        // 默认实现为空
    }

    // Apache Commons Pool2 接口实现
    
    @Override
    public T create() throws Exception {
        return doCreate();
    }

    @Override
    public PooledObject<T> wrap(T resource) {
        return new DefaultPooledObject<>(resource);
    }

    @Override
    public void destroyObject(PooledObject<T> p) throws Exception {
        doDestroy(p.getObject());
    }

    @Override
    public boolean validateObject(PooledObject<T> p) {
        return doValidate(p.getObject());
    }

    @Override
    public void activateObject(PooledObject<T> p) throws Exception {
        doActivate(p.getObject());
    }

    @Override
    public void passivateObject(PooledObject<T> p) throws Exception {
        doPassivate(p.getObject());
    }
}