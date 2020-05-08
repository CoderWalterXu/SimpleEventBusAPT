package com.xlh.study.eventbus.annotation.mode;

import java.lang.reflect.Method;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description: 事件订阅方法封装类
 * version:0.0.1
 */
public class SubscriberMethod {

    // 订阅方法名
    private String methodName;
    // 订阅方法，用于最后的自动执行订阅方法
    private Method method;
    // 线程模式
    private ThreadMode threadMode;
    // 事件对象Class
    private Class<?> eventType;
    // 事件订阅优先级（实现思路：重排序集合中方法的顺序）
    private int priority;
    // 是否粘性事件（实现思路：发送时存储，注册时判断粘性再激活）
    private boolean sticky;


    public SubscriberMethod(Class subscriberClass, String methodName,
                            Class<?> eventType, ThreadMode threadMode, int priority, boolean sticky) {
        this.methodName = methodName;
        this.threadMode = threadMode;
        this.eventType = eventType;
        this.priority = priority;
        this.sticky = sticky;
        try {
            // 订阅所属类
            method = subscriberClass.getDeclaredMethod(methodName, eventType);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSticky() {
        return sticky;
    }
}
