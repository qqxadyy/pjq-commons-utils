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

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 默认值获取工具
 *
 * @author pengjianqiang
 * @date 2021年3月10日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultValueGetter {
    /**
     * 获取value，如果value为空，则获取defaultValue
     *
     * @param defaultValue
     * @param value
     * @param <T>
     * @return
     */
    public static <T> T getValue(T defaultValue, T value) {
        if (CheckUtils.isNotNull(value)) {
            if (value instanceof String) {
                if (CheckUtils.isNotEmpty((String) value)) {
                    return value;
                } else {
                    return defaultValue;
                }
            }
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取value，如果value为空，则获取defaultValue
     *
     * @param defaultValue
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> T getValue(T defaultValue, T... values) {
        if (CheckUtils.isNotEmpty(values) && CheckUtils.isNotNull(values[0])) {
            if (values instanceof String[]) {
                if (CheckUtils.isNotEmpty((String) values[0])) {
                    return values[0];
                } else {
                    return defaultValue;
                }
            }
            return values[0];
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取value，如果value为空，则通过defaultValueGetter获取defaultValue
     *
     * @param defaultValueGetter
     * @param value
     * @param <T>
     * @return
     */
    public static <T> T get(Supplier<T> defaultValueGetter, T value) {
        if (CheckUtils.isNotNull(value)) {
            if (value instanceof String) {
                if (CheckUtils.isNotEmpty((String) value)) {
                    return value;
                } else {
                    return defaultValueGetter.get();
                }
            }
            return value;
        } else {
            return defaultValueGetter.get();
        }
    }

    /**
     * 获取value，如果value为空，则通过defaultValueGetter获取defaultValue
     *
     * @param defaultValueGetter
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> T get(Supplier<T> defaultValueGetter, T... values) {
        if (CheckUtils.isNotEmpty(values) && CheckUtils.isNotNull(values[0])) {
            if (values instanceof String[]) {
                if (CheckUtils.isNotEmpty((String) values[0])) {
                    return values[0];
                } else {
                    return defaultValueGetter.get();
                }
            }
            return values[0];
        } else {
            return defaultValueGetter.get();
        }
    }
}