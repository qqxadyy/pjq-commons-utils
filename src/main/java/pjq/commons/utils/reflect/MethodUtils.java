/*
 * Copyright © 2024 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-commons-utils
 * 项目描述：个人整理的工具类
 * 项目地址：https://github.com/qqxadyy/pjq-commons-utils
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2024 pengjianqiang
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
package pjq.commons.utils.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * {@link Method}工具类<br>
 *
 * @author pengjianqiang
 * @date 2018年12月17日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodUtils {
    /**
     * 调用类/接口的静态或默认方法(只适用于无参方法，有参方法需要使用另一个{@link #invokeStaticOrDefault(Class, Method, Object...)}方法)
     *
     * @param clazz
     * @param noArgsMethodName
     * @return
     * @throws Throwable
     */
    public static Object invokeStaticOrDefault(Class<?> clazz, String noArgsMethodName) throws Throwable {
        Method method = clazz.getDeclaredMethod(noArgsMethodName);
        return invokeStaticOrDefault(clazz, method, (Object[]) null);
    }

    /**
     * 调用类/接口的静态或默认方法(有参或无参方法都适用)
     *
     * @param clazz
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public static Object invokeStaticOrDefault(Class<?> clazz, Method method, Object... args) throws Throwable {
        if (method.isDefault()) {
            // 默认方法不能直接method.invoke
            InvocationHandler handler = (proxy, methodInProxy, argsInProxy) -> null; // 不用实际实现invoke方法
            Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{ clazz }, handler);
            Constructor<MethodHandles.Lookup> constructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                    | MethodHandles.Lookup.PACKAGE;

            return constructor.newInstance(clazz, allModes).unreflectSpecial(method, clazz).bindTo(proxy)
                    .invokeWithArguments(args);
        } else {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(null, args);
        }
    }
}