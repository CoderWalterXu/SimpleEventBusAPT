package com.xlh.study.eventbus.annotation.mode;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description:
 * version:0.0.1
 */
public interface SubscriberInfo {

    // 订阅所属类，如MainActivity
    Class<?> getSubscriberClass();

    // 获取订阅所属类中的所有订阅事件的方法
    SubscriberMethod[] getSubscriberMethods();

}
