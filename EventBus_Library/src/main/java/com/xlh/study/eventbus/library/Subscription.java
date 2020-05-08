package com.xlh.study.eventbus.library;

import com.xlh.study.eventbus.annotation.mode.SubscriberMethod;

import androidx.annotation.Nullable;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description: 临时JavaBean对象，也可以直接写在EventBus做为变量
 * version:0.0.1
 */
public final class Subscription {
    // 订阅者类，如MainActivity.class
    final Object subscriber;
    // 订阅的方法
    final SubscriberMethod subscriberMethod;

    public Subscription(Object subsciber, SubscriberMethod subscriberMethod) {
        this.subscriber = subsciber;
        this.subscriberMethod = subscriberMethod;
    }


    @Override
    public boolean equals(@Nullable Object other) {
        // 必须重写该方法，检测激活粘性事件重复调用（同一对象注册多个）
        if (other instanceof Subscription) {
            Subscription otherSubscription = (Subscription) other;
            // 删除官方：subscriber == otherSubscription.subscriber判断条件
            // 原因：粘性事件Bug，多次调用和移除时重现，参考Subscription.java 37行
            return subscriberMethod.equals(otherSubscription.subscriberMethod);
        } else {
            return false;
        }
    }
}
