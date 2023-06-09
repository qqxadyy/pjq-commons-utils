/*
 * Copyright © 2023 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-commons-utils
 * 项目描述：个人整理的工具类
 * 项目地址：https://github.com/qqxadyy/pjq-commons-utils
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2023 pengjianqiang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pjq.commons.utils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 字符串、集合的判断工具类
 *
 * @author pengjianqiang
 * @date 2019年1月15日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CheckUtils {
    public static boolean isEmpty(String str) {
        // u200b字符串是不可见的，控制台等打印不出，但是有实际长度
        // "　"编码是12288，按空格处理
        String invisibleChar = "\u200b|\u200B|　";
        String ns = (null == str ? null : str.trim().replaceAll(invisibleChar, ""));
        return StringUtils.isBlank(ns) || "null".equalsIgnoreCase(ns) || "undefined".equalsIgnoreCase(ns);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Object[] array) {
        if (array instanceof String[]) {
            return areEmpty((String[]) array);
        } else {
            return ArrayUtils.isEmpty(array);
        }
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isNull(Object obj) {
        return null == obj;
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static boolean areEmpty(String... strArray) {
        if (ArrayUtils.isEmpty(strArray)) {
            return true;
        } else {
            for (String str : strArray) {
                if (!isEmpty(str)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean areNotEmpty(String... strArray) {
        if (ArrayUtils.isEmpty(strArray)) {
            return false;
        } else {
            for (String str : strArray) {
                if (isEmpty(str)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean areNull(Object... objArray) {
        if (ArrayUtils.isEmpty(objArray)) {
            return true;
        } else {
            for (Object obj : objArray) {
                if (!isNull(obj)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean areNotNull(Object... objArray) {
        if (ArrayUtils.isEmpty(objArray)) {
            return false;
        } else {
            for (Object obj : objArray) {
                if (isNull(obj)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 判断字符串是否为空，为空时抛异常
     *
     * @param str
     * @param msg
     * @param exceptionFunc
     *         为空时默认抛出{@link RuntimeException}，否则抛出指定的异常类型
     */
    @SafeVarargs
    public static void checkNotEmpty(String str, String msg,
                                     Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (isEmpty(str)) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotNull(Object obj, String msg,
                                    Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (isNull(obj)) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotEmpty(Collection<?> collection, String msg,
                                     Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (isEmpty(collection)) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotEmpty(Map<?, ?> map, String msg,
                                     Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (isEmpty(map)) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotEmpty(Object[] array, String msg,
                                     Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (isEmpty(array)) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotFalse(boolean boolVal, String msg,
                                     Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (!boolVal) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    public static void checkNotTrue(boolean boolVal, String msg,
                                    Function<String, ? extends RuntimeException>... exceptionFunc) {
        if (boolVal) {
            throw exceptionGetter(exceptionFunc).apply(msg);
        }
    }

    @SafeVarargs
    private static Function<String, ? extends RuntimeException> exceptionGetter(
            Function<String, ? extends RuntimeException>... exceptionFunc) {
        Supplier<Function<String, ? extends RuntimeException>> defaultExceptionGetter = () -> (msg -> new RuntimeException(
                msg));
        return DefaultValueGetter.get(defaultExceptionGetter, exceptionFunc);
    }
}