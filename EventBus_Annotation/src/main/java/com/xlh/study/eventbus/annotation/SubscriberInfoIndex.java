package com.xlh.study.eventbus.annotation;

import com.xlh.study.eventbus.annotation.mode.SubscriberInfo;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description:所有的事件订阅方法，生成索引接口
 * version:0.0.1
 */
public interface SubscriberInfoIndex {

    /**
     * 生成索引接口，通过订阅者对象，获取所有订阅方法
     *
     * @param subscriberClass 订阅者对象Class,如MainActivity.class
     * @return 事件订阅方法封装类
     */
    SubscriberInfo getSubscriberInfo(Class<?> subscriberClass);

}
