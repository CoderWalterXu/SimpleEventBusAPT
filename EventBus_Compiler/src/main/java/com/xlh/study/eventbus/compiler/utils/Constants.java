package com.xlh.study.eventbus.compiler.utils;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description: 常量类
 * version:0.0.1
 */
public class Constants {

    // 注解处理器中支持的注解类型
    public static final String SUBSCRIBE_ANNOTATION_TYPES = "com.xlh.study.eventbus.annotation.WxSubscribe";

    // APT生成类文件所属包名
    public static final String PACKAGE_NAME = "packageName";

    // APT生成类文件的类名
    public static final String CLASS_NAME = "className";

    // 所有的事件订阅方法，生成索引接口
    public static final String SUBSCRIBERINFO_INDEX = "com.xlh.study.eventbus.annotation.SubscriberInfoIndex";

    // 全局属性名
    public static final String FIELD_NAME = "SUBSCRIBER_INDEX";

    // putIndex方法的参数对象名
    public static final String PUTINDEX_PARAMETER_NAME = "info";

    // 加入Map集合方法名
    public static final String PUTINDEX_METHOD_NAME = "putIndex";

    // getSubscriberInfo方法的参数对象名
    public static final String GETSUBSCRIBERINFO_PARAMETER_NAME = "subscriberClass";

    // 通过订阅者对象（如MainActivity.class）获取所有订阅方法的方法名
    public static final String GETSUBSCRIBERINFO_METHOD_NAME = "getSubscriberInfo";


}
