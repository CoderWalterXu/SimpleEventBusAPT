package com.xlh.study.eventbus.compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author: Watler Xu
 * time:2020/5/7
 * description: 字符串、集合判空工具
 * version:0.0.1
 */
public class EmptyUtils {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
