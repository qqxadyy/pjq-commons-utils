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
import java.util.Optional;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.support.NoParamConsumer;

/**
 * 增强的{@link CheckUtils}工具类，用链式写法实现，且提供了简单的当true/false时做不同操作的方法<br>
 * 一般是想要类似三元操作符的效果，但是又不想要先用变量指定对应结果值时使用。类似{@link Optional#orElseGet(Supplier)}的用途<br>
 * <br>
 * 注意：<br>
 * 1.组合条件时提供的方法都是and连接，需要or时直接使用{@link #or(boolean)}方法，混合时用时用回常规的if...else写法<br>
 * 2.复杂的if...else if...else用回常规写法
 *
 * @author pengjianqiang
 * @date 2022年6月27日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnhanceCheckUtils {
    private Boolean boolVal;

    public static EnhanceCheckUtils builder() {
        return new EnhanceCheckUtils();
    }

    /**
     * 返回组合条件的判断结果
     *
     * @return
     * @author pengjianqiang@2022年6月27日
     */
    public boolean test() {
        return DefaultValueGetter.getValue(false, this.boolVal);
    }

    /**
     * 当组合条件判断为true时执行对应逻辑，为false时不执行
     *
     * @param doIfTrue
     * @author pengjianqiang@2022年6月27日
     */
    public void exec(NoParamConsumer doIfTrue) {
        exec(doIfTrue, null);
    }

    /**
     * 当组合条件判断为true/false执行对应逻辑
     *
     * @param doIfTrue
     * @param doIfFalse
     * @author pengjianqiang@2022年6月27日
     */
    public void exec(NoParamConsumer doIfTrue, NoParamConsumer doIfFalse) {
        DefaultValueGetter.get(() -> new NoParamConsumer() {
            @Override
            public void accept() {
            }
        }, (test() ? doIfTrue : doIfFalse)).accept();
    }

    /**
     * 当组合条件判断为true时返回对应的值，为false时返回null
     *
     * @param <T>
     * @param objIfTrue
     * @return
     * @author pengjianqiang@2022年6月27日
     */
    public <T> T get(Supplier<T> objIfTrue) {
        return get(objIfTrue, null);
    }

    /**
     * 当组合条件判断为true/false时返回对应的值
     *
     * @param <T>
     * @param objIfTrue
     * @param objIfFalse
     * @return
     * @author pengjianqiang@2022年6月27日
     */
    public <T> T get(Supplier<T> objIfTrue, Supplier<T> objIfFalse) {
        return DefaultValueGetter.get(() -> new Supplier<T>() {
            @Override
            public T get() {
                return null;
            }
        }, (test() ? objIfTrue : objIfFalse)).get();
    }

    public EnhanceCheckUtils and(boolean boolVal) {
        if (null == this.boolVal) {
            this.boolVal = boolVal;
        } else {
            this.boolVal = this.boolVal && boolVal;
        }
        return this;
    }

    public EnhanceCheckUtils or(boolean boolVal) {
        if (null == this.boolVal) {
            this.boolVal = boolVal;
        } else {
            this.boolVal = this.boolVal || boolVal;
        }
        return this;
    }

    public EnhanceCheckUtils isEmpty(String str) {
        return and(CheckUtils.isEmpty(str));
    }

    public EnhanceCheckUtils isNotEmpty(String str) {
        return and(CheckUtils.isNotEmpty(str));
    }

    public EnhanceCheckUtils isEmpty(Collection<?> collection) {
        return and(CheckUtils.isEmpty(collection));
    }

    public EnhanceCheckUtils isNotEmpty(Collection<?> collection) {
        return and(CheckUtils.isNotEmpty(collection));
    }

    public EnhanceCheckUtils isEmpty(Map<?, ?> map) {
        return and(CheckUtils.isEmpty(map));
    }

    public EnhanceCheckUtils isNotEmpty(Map<?, ?> map) {
        return and(CheckUtils.isNotEmpty(map));
    }

    public EnhanceCheckUtils isEmpty(Object[] array) {
        return and(CheckUtils.isEmpty(array));
    }

    public EnhanceCheckUtils isNotEmpty(Object[] array) {
        return and(CheckUtils.isNotEmpty(array));
    }

    public EnhanceCheckUtils isNull(Object obj) {
        return and(CheckUtils.isNull(obj));
    }

    public EnhanceCheckUtils isNotNull(Object obj) {
        return and(CheckUtils.isNotNull(obj));
    }

    public EnhanceCheckUtils areEmpty(String... strArray) {
        return and(CheckUtils.areEmpty(strArray));
    }

    public EnhanceCheckUtils areNotEmpty(String... strArray) {
        return and(CheckUtils.areNotEmpty(strArray));
    }

    public EnhanceCheckUtils areNull(Object... objArray) {
        return and(CheckUtils.areNull(objArray));
    }

    public EnhanceCheckUtils areNotNull(Object... objArray) {
        return and(CheckUtils.areNotNull(objArray));
    }
}