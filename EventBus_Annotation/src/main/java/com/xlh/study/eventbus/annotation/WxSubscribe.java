package com.xlh.study.eventbus.annotation;

import com.xlh.study.eventbus.annotation.mode.ThreadMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.ElementType;


/**
 * @author: Watler Xu
 * time:2020/5/7
 * description:
 * version:0.0.1
 */

@Target(ElementType.METHOD) // 该注解作用在方法之上
@Retention(RetentionPolicy.CLASS) // 在编译期进行一些预处理操作，注解会在class文件中存在
public @interface WxSubscribe {

    // 线程模式，默认POSTING（订阅、发布在同一线程）
    ThreadMode threadMode() default ThreadMode.POSTING;

    // 是否使用粘性事件
    boolean sticky() default false;

    // 事件订阅优先级，在同一个线程中。数值越大优先级越高
    int priority() default 0;

}
