package com.xlh.study.eventbus.library;

import android.os.Handler;
import android.os.Looper;

import com.xlh.study.eventbus.annotation.SubscriberInfoIndex;
import com.xlh.study.eventbus.annotation.mode.SubscriberInfo;
import com.xlh.study.eventbus.annotation.mode.SubscriberMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description:
 * version:0.0.1
 */
public class WxEventBus {

    // volatile修饰的变量不允许线程内部缓存和指令重排序，即直接修改内存
    private static volatile WxEventBus defaultInstance;
    // 索引接口
    private SubscriberInfoIndex subscriberInfoIndexs;
    /**
     * 订阅者类型集合，比如：订阅者MainActivity订阅了哪些EventBean，或者解除订阅的缓存。
     * key：订阅者MainActivity.class，value：EventBean集合
     */
    private Map<Object, List<Class<?>>> typesBySubscriber;
    /**
     * 方法缓存
     * key：订阅者MainActivity.class，value：订阅方法集合
     */
    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * EventBean缓存
     * key：UserInfo.class，value：订阅者（可以是多个Activity）中所有订阅的方法集合
     */
    private Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    /**
     * 粘性事件缓存
     * key：MessageBean.class，value：MessageBean
     */
    private final Map<Class<?>, Object> stickyEvents;
    // 发送（子线程），订阅（主线程）
    private Handler mHandler;
    // 发送（主线程），订阅（子线程）
    private ExecutorService mExecutorService;

    private WxEventBus() {
        // 初始化缓存集合
        typesBySubscriber = new HashMap<>();
        subscriptionsByEventType = new HashMap<>();
        stickyEvents = new HashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
        mExecutorService = Executors.newCachedThreadPool();
    }

    public static WxEventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (WxEventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new WxEventBus();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * 添加索引
     *
     * @param index
     */
    public void addIndex(SubscriberInfoIndex index) {
        subscriberInfoIndexs = index;
    }

    /**
     * 注册/订阅事件
     *
     * @param subscriber
     */
    public void register(Object subscriber) {
        // 获取注册/订阅类，如MainActivity.class
        Class<?> subscribetClass = subscriber.getClass();
        // 寻找注册/订阅类中的订阅方法集合
        List<SubscriberMethod> subscriberMethods = findSubscriberMethods(subscribetClass);
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                // 遍历后，开始订阅
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    /**
     * 寻找注册/订阅类中的订阅方法集合
     *
     * @param subscriberClass
     * @return
     */
    private List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        // 先从方法缓存中获取
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        // 找到了缓存，直接返回
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        // 找不到，则从APT生成的类文件中寻找
        subscriberMethods = findUsingInfo(subscriberClass);
        if (subscriberMethods != null) {
            // 存入缓存
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
        }
        return subscriberMethods;
    }

    /**
     * 从APT生成的类文件中寻找订阅方法
     *
     * @param subscriberClass
     * @return
     */
    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        // 运行时寻找索引，报错了则说明没有初始化索引方法
        if (subscriberInfoIndexs == null) {
            throw new RuntimeException("未添加索引方法：addIndex()");
        }
        // 接口持有实现类的引用
        SubscriberInfo info = subscriberInfoIndexs.getSubscriberInfo(subscriberClass);
        // 数组转List集合
        if (info != null) {
            return Arrays.asList(info.getSubscriberMethods());
        }
        return null;
    }

    /**
     * 订阅
     *
     * @param subscriber
     * @param subscriberMethod
     */
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        // 获取订阅方法参数类型，也就是发送的消息类型
        Class<?> eventType = subscriberMethod.getEventType();
        // 临时对象存储
        Subscription subscription = new Subscription(subscriber, subscriberMethod);
        // 读取EventBean缓存
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            // 初始化集合
            subscriptions = new CopyOnWriteArrayList<>();
            // 存入集合
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            if (subscriptions.contains(subscription)) {
                // 重复注册粘性事件
                sticky(subscriberMethod, eventType, subscription);
                return;
            }
        }

        // 订阅方法优先级处理。第一次进来是0
        int size = subscriptions.size();
        // 这里的i <= size，否则进不了下面条件
        for (int i = 0; i <= size; i++) {
            // 如果满足任一条件则进入循环（第1次 i = size = 0）
            // 第2次，size不为0，新加入的订阅方法匹配集合中所有订阅方法的优先级
            if (i == size || subscriberMethod.getPriority() > subscriptions.get(i).subscriberMethod.getPriority()) {
                // 如果新加入的订阅方法优先级大于集合中某订阅方法优先级，则插队到它之前一位
                if (!subscriptions.contains(subscription)) subscriptions.add(i, subscription);
                // 优化：插队成功就跳出（找到了加入集合点）
                break;
            }
        }

        // 订阅者类型集合，比如：订阅者MainActivity订阅了哪些EventBean,或者解除订阅的缓存
        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            // 存入缓存
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        //
        subscribedEvents.add(eventType);

        sticky(subscriberMethod, eventType, subscription);

    }

    /**
     * 执行多次粘性事件，而不会出现闪退
     *
     * @param subscriberMethod
     * @param eventType
     * @param subscription
     */
    private void sticky(SubscriberMethod subscriberMethod, Class<?> eventType, Subscription subscription) {

        // 粘性事件触发：注册事件就激活方法，因为整个源码只有此处遍历了。
        // 最佳切入点原因：1，粘性事件的订阅方法加入了缓存。2，注册时只有粘性事件直接激活方法（隔离非粘性事件）
        // 新增开关方法弊端：粘性事件未在缓存中，无法触发订阅方法。且有可能多次执行post()方法
        if (subscriberMethod.isSticky()) {
            Object stickyEvent = stickyEvents.get(eventType);
            // 发送事件到订阅者的所有订阅方法，并激活方法
            if (stickyEvent != null) {
                postToSubscription(subscription, stickyEvent);
            }
        }

    }

    /**
     * 发送事件到订阅者的所有订阅方法
     *
     * @param subscription
     * @param event
     */
    private void postToSubscription(final Subscription subscription, final Object event) {
        // 匹配订阅方的线程模式
        switch (subscription.subscriberMethod.getThreadMode()) {
            case POSTING:
                // 订阅、发布在同一线程
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
                // 发布至主线程
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // 订阅是主线程，发布是主线程
                    invokeSubscriber(subscription, event);
                } else {
                    // 订阅方是子线程，发布是主线程
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invokeSubscriber(subscription, event);
                        }
                    });
                }
                break;
            case ASYNC:
                // 发布至子线程
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // 订阅方是主线程,发布是子线程
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            invokeSubscriber(subscription, event);
                        }
                    });
                } else {
                    // 订阅方是子线程,发布是子线程
                    invokeSubscriber(subscription, event);
                }
                break;
            default:
                break;
        }


    }

    /**
     * 执行订阅方法（被注解方法自动执行）
     *
     * @param subscription
     * @param event
     */
    private void invokeSubscriber(Subscription subscription, Object event) {
        try {
            // 反射执行。（无论3.0之前还是之后。最后一步终究逃不过反射！）
            subscription.subscriberMethod.getMethod().invoke(subscription.subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否已经注册/订阅
     * 同步锁保证并发安全
     *
     * @param subsciber
     * @return
     */
    public synchronized boolean isRegistered(Object subsciber) {
        return typesBySubscriber.containsKey(subsciber);
    }

    /**
     * 解除某订阅者关系
     * 同步锁保证并发安全
     *
     * @param subscriber
     */
    public synchronized void unregister(Object subscriber) {
        // 从缓存中移除
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            // 移除前清空集合
            subscribedTypes.clear();
            typesBySubscriber.remove(subscriber);
        }
    }

    /**
     * 发送粘性事件，最终还是调用了post方法
     *
     * @param event
     */
    public void postSticky(Object event) {
        // 同步锁保证并发安全
        synchronized (stickyEvents) {
            // 加入粘性事件缓存集合
            stickyEvents.put(event.getClass(), event);
        }
        // 只要参数匹配，粘性/非粘性订阅方法全部执行
        // post(event);
    }

    /**
     * 获取执行类型的粘性事件
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        // 同步锁保证并发安全
        synchronized (stickyEvents) {
            // cast方法做转换类型时安全措施
            return eventType.cast(stickyEvents.get(eventType));
        }
    }

    /**
     * 移除指定类型的粘性事件
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        // 同步锁保证并发安全
        synchronized (stickyEvents) {
            // cast方法做转换类型时安全措施
            return eventType.cast(stickyEvents.remove(eventType));
        }
    }

    /**
     * 移除所有粘性事件
     */
    public void removeAllStickyEvents() {
        // 同步锁保证并发安全
        synchronized (stickyEvents) {
            // 清理集合
            stickyEvents.clear();
        }
    }


    /**
     * 发送消息事件
     *
     * @param event
     */
    public void post(Object event) {
        postSingleEventForEventType(event, event.getClass());
    }

    /**
     * 为EventBean事件类型发布单个事件
     * 参数类型必须一致
     *
     * @param event
     * @param eventClass
     */
    private void postSingleEventForEventType(Object event, Class<?> eventClass) {
        // 从EventBean缓存中，获取所有订阅者和订阅方法
        CopyOnWriteArrayList<Subscription> subscriptions;
        // 同步锁保证并发安全
        synchronized (this) {
            subscriptions = subscriptionsByEventType.get(eventClass);
        }

        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                // 遍历，寻找发送方指定的EventBean,匹配订阅方法的EventBean
                postToSubscription(subscription, event);
            }
        }

    }


    /**
     * 清理静态缓存
     */
    public static void clearCaches() {
        METHOD_CACHE.clear();
    }


}
