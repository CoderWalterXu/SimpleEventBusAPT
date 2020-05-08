package com.xlh.study.eventbus.annotation.mode;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description:
 * version:0.0.1
 */
public enum ThreadMode {

    // 订阅、发布在同一线程。避免了线程切换，也是推荐的默认模式
    POSTING,

    // 主线程中被调用，切勿进行耗时操作
    MAIN,

    // 用于网络访问等耗时操作，事件总线已完成的异步订阅通知线程。并使用线程池进行有效复用
    ASYNC

}
