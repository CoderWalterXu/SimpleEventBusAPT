package com.xlh.study.eventbus.annotation.mode;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description: 所有事件集合
 * version:0.0.1
 */
public class EventBeans implements SubscriberInfo {

    // 订阅者对象Class,如MainActivity
    private final Class subscriberClass;
    // 订阅方法数组
    private final SubscriberMethod[] methodInfos;

    public EventBeans(Class subscriberClass, SubscriberMethod[] methodInfos) {
        this.subscriberClass = subscriberClass;
        this.methodInfos = methodInfos;
    }

    @Override
    public Class<?> getSubscriberClass() {
        return subscriberClass;
    }

    @Override
    public synchronized SubscriberMethod[] getSubscriberMethods() {
        return methodInfos;
    }
}
