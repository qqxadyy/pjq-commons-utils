/*
 * Copyright © 2021 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-commons-utils
 * 项目描述：pjq-commons-utils
 * 项目地址：https://github.com/qqxadyy/pjq-commons-utils
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2021 pengjianqiang
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
package pjq.commons.utils.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pjq.commons.utils.CheckUtils;

/**
 * 集合、map、数组工具类
 *
 * @author pengjianqiang
 * @date 2019年1月17日
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtils {
    /**
     * 遍历集合，可以在action方法中抛出{@link Break}或{@link Continue}异常进行控制<br>
     * 如果action里面本身有必须显式捕捉的异常，则可以在catch中根据情况抛出Break或Continue异常<br>
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @param action
     *            遍历操作，<code>e->{}</code>
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> void forEach(Iterable<T> iterable, Consumer<T> action) {
        forEachCommon(iterable, action);
    }

    /**
     * 遍历集合，可以在action方法中抛出{@link Break}或{@link Continue}异常进行控制<br>
     * 如果action里面本身有必须显式捕捉的异常，则可以在catch中根据情况抛出Break或Continue异常<br>
     * 当集合非有序时，action的下标不一定符合预期，根据实际情况判断
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @param action
     *            遍历操作，<code>(e,index)->{}</code>
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> void forEach(Iterable<T> iterable, BiConsumer<T, Integer> action) {
        forEachCommon(iterable, action);
    }

    public static <K, V> void forEach(Map<K, V> map, Consumer<Entry<K, V>> action) {
        forEachCommon(map.entrySet(), action);
    }

    /**
     * 遍历map，可以在action方法中抛出{@link Break}或{@link Continue}异常进行控制<br>
     * 如果action里面本身有必须显式捕捉的异常，则可以在catch中根据情况抛出Break或Continue异常<br>
     * 当map非有序时，action的下标不一定符合预期，根据实际情况判断
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            map集合
     * @param action
     *            遍历操作，<code>(entry,index)->{}</code>
     * @creator pengjianqiang@2021年4月20日
     */
    public static <K, V> void forEach(Map<K, V> map, BiConsumer<Entry<K, V>, Integer> action) {
        forEachCommon(map.entrySet(), action);
    }

    public static <T> void forEach(T[] array, Consumer<T> action) {
        if (CheckUtils.isEmpty(array)) {
            return;
        }
        forEach(Arrays.stream(array), action);
    }

    public static <T> void forEach(T[] array, BiConsumer<T, Integer> action) {
        if (CheckUtils.isEmpty(array)) {
            return;
        }
        forEach(Arrays.stream(array), action);
    }

    public static <T> void forEach(Stream<T> stream, Consumer<T> action) {
        if (CheckUtils.isNull(stream)) {
            return;
        }
        forEach(stream.collect(Collectors.toList()), action);
    }

    public static <T> void forEach(Stream<T> stream, BiConsumer<T, Integer> action) {
        if (CheckUtils.isNull(stream)) {
            return;
        }
        forEach(stream.collect(Collectors.toList()), action);
    }

    @SuppressWarnings("unchecked")
    private static <T> void forEachCommon(Iterable<T> iterable, Object actionObj) {
        if (CheckUtils.isNull(iterable) || CheckUtils.isNull(actionObj)) {
            return;
        }

        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return;
        }

        boolean isIndexAction = (actionObj instanceof BiConsumer);
        Consumer<T> action = !isIndexAction ? (Consumer<T>)actionObj : null;
        BiConsumer<T, Integer> indexAction = isIndexAction ? (BiConsumer<T, Integer>)actionObj : null;

        int index = 0;
        while (iterator.hasNext()) {
            try {
                T element = iterator.next();
                if (!isIndexAction) {
                    action.accept(element);
                } else {
                    indexAction.accept(element, index++);
                }
            } catch (Break | Continue e) {
                String errMsg = e.getMessage();
                if (CheckUtils.isEmpty(errMsg)) {
                    Throwable cause = e.getCause();
                    if (CheckUtils.isNotNull(cause)) {
                        errMsg = ExceptionUtils.getRootCauseMessage(cause);
                    }
                }
                if (CheckUtils.isNotEmpty(errMsg)) {
                    log.warn("(一般可忽略)集合遍历出现".concat(e.getClass().getSimpleName()).concat("，原因如下===={}"), errMsg,
                        ExceptionUtils.getRootCause(e));
                }

                if (e instanceof Break) {
                    break;
                } else {
                    continue;
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * 保留集合中符合条件的对象
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return 过滤后的list，没有符合条件则返回空list
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> List<T> filter(Iterable<T> iterable, Predicate<T> predicate) {
        if (CheckUtils.isNull(iterable)) {
            return new ArrayList<>();
        }
        return filterStream(IterableUtils.toList(iterable).stream(), predicate).collect(Collectors.toList());
    }

    /**
     * 保留map中符合条件的对象
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            map集合
     * @param predicate
     *            过滤条件，<code>entry->{}</code>，为空时相当于不过滤
     * @return 过滤后的map，没有符合条件则返回空map
     * @creator pengjianqiang@2021年4月20日
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Predicate<Entry<K, V>> predicate) {
        if (CheckUtils.isEmpty(map)) {
            return new HashMap<>();
        }
        Map<K, V> filteredMap = filterStream(map.entrySet().stream(), predicate)
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        return filteredMap;
    }

    /**
     * 保留数组中符合条件的对象
     * 
     * @param <T>
     *            数组元素类型
     * @param array
     *            数组
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return 过滤后的数组，没有符合条件则返回空数组(注：array==null时返回null)
     * @creator pengjianqiang@2021年4月20日
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] filter(T[] array, Predicate<? super T> predicate) {
        if (CheckUtils.isEmpty(array)) {
            return array;
        }
        List<T> filteredList = filterStream(Arrays.stream(array), predicate).collect(Collectors.toList());
        return filteredList.stream()
            .toArray(t -> (T[])Array.newInstance(array.getClass().getComponentType(), filteredList.size()));
    }

    /**
     * Stream的filter不是结束操作，不开放给外部调用，避免外部获取没结束的Stream
     * 
     * @param <T>
     *            stream的元素类类型
     * @param stream
     *            stream
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return
     * @creator pengjianqiang@2021年4月20日
     */
    private static <T> Stream<T> filterStream(Stream<T> stream, Predicate<? super T> predicate) {
        return stream.filter(mergeNotNullPredicate(predicate));
    }

    /**
     * 把一个过滤非空元素的过滤条件和参数的过滤条件进行and合并
     * 
     * @param <T>
     *            过滤条件中的元素类型
     * @param predicate
     *            过滤元素用的过滤条件
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    @SafeVarargs
    private static <T> Predicate<? super T> mergeNotNullPredicate(Predicate<? super T>... predicate) {
        // 默认过滤不为空的元素，然后再用参数中的过滤条件(参数中的过滤条件为空时and一个返回true的新过滤条件即可)
        Predicate<T> notNullPredicate = CheckUtils::isNotNull;
        return notNullPredicate
            .and(CheckUtils.isEmpty(predicate) || CheckUtils.isNull(predicate[0]) ? (t -> true) : predicate[0]);
    }

    /**
     * 保留集合中符合条件的对象，并返回第一个符合条件的对象<br>
     * 当集合非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return 过滤后的第一个符合条件的对象，没有符合条件则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> T filterOne(Iterable<T> iterable, Predicate<T> predicate) {
        return first(filter(iterable, predicate));
    }

    /**
     * 保留map中符合条件的对象，并返回第一个符合条件的对象<br>
     * 当map非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            集合
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return 过滤后的第一个符合条件的对象，没有符合条件则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <K, V> Map<K, V> filterOne(Map<K, V> map, Predicate<Entry<K, V>> predicate) {
        return first(filter(map, predicate));
    }

    /**
     * 保留数组中符合条件的对象，并返回第一个符合条件的对象
     *
     * @param <T>
     *            数组元素类型
     * @param array
     *            集合
     * @param predicate
     *            过滤条件，<code>t->{}</code>，为空时相当于不过滤
     * @return 过滤后的第一个符合条件的对象，没有符合条件则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> T filterOne(T[] array, Predicate<T> predicate) {
        return first(filter(array, predicate));
    }

    /**
     * 获取集合的第一个对象<br>
     * 当集合非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @return 集合的第一个对象，集合为空则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> T first(Iterable<T> iterable) {
        if (CheckUtils.isNull(iterable)) {
            return null;
        }
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * 获取集合的最后一个对象<br>
     * 当集合非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <T>
     *            集合元素类型
     * @param iterable
     *            集合
     * @return 集合的最后一个对象，集合为空则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <T> T last(Iterable<T> iterable) {
        if (CheckUtils.isNull(iterable)) {
            return null;
        }
        T last = null;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
    }

    /**
     * 获取map的第一个元素新构成的map<br>
     * 当map非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            集合
     * @return 集合的第一个对象，集合为空则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <K, V> Map<K, V> first(Map<K, V> map) {
        if (CheckUtils.isEmpty(map)) {
            return null;
        }
        Map<K, V> newMap = new HashMap<>();
        Entry<K, V> entry = map.entrySet().iterator().next();
        newMap.put(entry.getKey(), entry.getValue());
        return newMap;
    }

    /**
     * 获取map的最后一个元素新构成的map<br>
     * 当map非有序时，返回结果不一定符合预期，根据实际情况判断
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            集合
     * @return 集合的最后一个对象，集合为空则返回null
     * @creator pengjianqiang@2021年4月20日
     */
    public static <K, V> Map<K, V> last(Map<K, V> map) {
        if (CheckUtils.isEmpty(map)) {
            return null;
        }
        Map<K, V> newMap = new HashMap<>();
        Entry<K, V> entry = null;
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
        }
        newMap.put(entry.getKey(), entry.getValue());
        return newMap;
    }

    public static <T> T first(T[] array) {
        return CheckUtils.isEmpty(array) ? null : array[0];
    }

    public static <T> T last(T[] array) {
        return CheckUtils.isEmpty(array) ? null : array[array.length - 1];
    }

    /**
     * 根据mapper的处理转成目标list
     * 
     * @param <S>
     *            源集合元素类型
     * @param <T>
     *            目标list元素类型
     * @param iterable
     *            源集合
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空list；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标list
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, T> List<T> transformToList(Iterable<S> iterable, Function<S, T> mapper,
        Predicate<S>... predicate) {
        List<S> list = IterableUtils.toList(iterable);
        if (CheckUtils.isEmpty(list) || CheckUtils.isNull(mapper)) {
            return new ArrayList<>();
        }
        return transformToList(list.stream(), mapper, predicate);
    }

    /**
     * 根据mapper的处理转成目标list
     * 
     * @param <K>
     *            源map的key元素类型
     * @param <V>
     *            源map的value元素类型
     * @param <T>
     *            目标list元素类型
     * @param map
     *            源map
     * @param mapper
     *            转换处理器，<code>entry->{}</code>，为空时返回空list；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标list
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <K, V, T> List<T> transformToList(Map<K, V> map, Function<Entry<K, V>, T> mapper,
        Predicate<Entry<K, V>>... predicate) {
        if (CheckUtils.isEmpty(map) || CheckUtils.isNull(mapper)) {
            return new ArrayList<>();
        }
        return transformToList(map.entrySet(), mapper, predicate);
    }

    /**
     * 根据mapper的处理转成目标list
     * 
     * @param <S>
     *            源数组元素类型
     * @param <T>
     *            目标list元素类型
     * @param array
     *            源数组
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空list；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标list
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, T> List<T> transformToList(S[] array, Function<S, T> mapper, Predicate<S>... predicate) {
        if (CheckUtils.isEmpty(array)) {
            return new ArrayList<>();
        }
        return transformToList(Arrays.stream(array), mapper, predicate);
    }

    /**
     * 根据mapper的处理转成目标list
     * 
     * @param <S>
     *            源stream元素类型
     * @param <T>
     *            目标list元素类型
     * @param stream
     *            源stream
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空list；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标list
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, T> List<T> transformToList(Stream<S> stream, Function<S, T> mapper, Predicate<S>... predicate) {
        if (CheckUtils.isNull(stream) || CheckUtils.isNull(mapper)) {
            return new ArrayList<>();
        }

        // 先根据参数传入的断言过滤数据，然后转换，最后再过滤一次为空的数据
        return filterStream(stream, mergeNotNullPredicate(predicate)).map(mapper).filter(CheckUtils::isNotNull)
            .collect(Collectors.toList());
    }

    /**
     * 根据mapper的处理转成目标set
     * 
     * @param <S>
     *            源集合元素类型
     * @param <T>
     *            目标set元素类型
     * @param iterable
     *            源集合
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空set；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标set
     * @creator pengjianqiang@2022年6月28日
     */
    @SafeVarargs
    public static <S, T> Set<T> transformToSet(Iterable<S> iterable, Function<S, T> mapper, Predicate<S>... predicate) {
        return new HashSet<>(transformToList(iterable, mapper, predicate));
    }

    /**
     * 根据mapper的处理转成目标set
     * 
     * @param <K>
     *            源map的key元素类型
     * @param <V>
     *            源map的value元素类型
     * @param <T>
     *            目标set元素类型
     * @param map
     *            源map
     * @param mapper
     *            转换处理器，<code>entry->{}</code>，为空时返回空set；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标set
     * @creator pengjianqiang@2022年6月28日
     */
    @SafeVarargs
    public static <K, V, T> Set<T> transformToSet(Map<K, V> map, Function<Entry<K, V>, T> mapper,
        Predicate<Entry<K, V>>... predicate) {
        return new HashSet<>(transformToList(map, mapper, predicate));
    }

    /**
     * 根据mapper的处理转成目标set
     * 
     * @param <S>
     *            源数组元素类型
     * @param <T>
     *            目标set元素类型
     * @param array
     *            源数组
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空set；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标set
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, T> Set<T> transformToSet(S[] array, Function<S, T> mapper, Predicate<S>... predicate) {
        return new HashSet<>(transformToList(array, mapper, predicate));
    }

    /**
     * 根据mapper的处理转成目标set
     * 
     * @param <S>
     *            源stream元素类型
     * @param <T>
     *            目标set元素类型
     * @param stream
     *            源stream
     * @param mapper
     *            转换处理器，<code>e->{}</code>，为空时返回空set；mapper中返回Null时的元素会被最终过滤
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标set
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, T> Set<T> transformToSet(Stream<S> stream, Function<S, T> mapper, Predicate<S>... predicate) {
        return new HashSet<>(transformToList(stream, mapper, predicate));
    }

    /**
     * 根据mapper的处理转成目标map<br>
     * 如果转换后有相同key，则后面的value值覆盖前面的
     * 
     * @param <S>
     *            源集合元素类型
     * @param <K>
     *            目标map的key元素类型
     * @param <V>
     *            目标map的value元素类型
     * @param iterable
     *            源集合
     * @param keyMapper
     *            key转换处理器，<code>e->{}</code>，为空时返回空map
     * @param valueMapper
     *            value转换处理器，<code>e->{}</code>，为空时返回空map
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标map
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, K, V> Map<K, V> transformToMap(Iterable<S> iterable, Function<S, K> keyMapper,
        Function<S, V> valueMapper, Predicate<S>... predicate) {
        List<S> list = IterableUtils.toList(iterable);
        if (CheckUtils.isEmpty(list) || CheckUtils.isNull(keyMapper) || CheckUtils.isNull(valueMapper)) {
            return new HashMap<>();
        }
        return transformToMap(list.stream(), keyMapper, valueMapper, predicate);
    }

    /**
     * 根据mapper的处理转成目标map<br>
     * 如果转换后有相同key，则后面的value值覆盖前面的
     * 
     * @param <SK>
     *            源map的key元素类型
     * @param <SV>
     *            源map的value元素类型
     * @param <K>
     *            目标map的key元素类型
     * @param <V>
     *            目标map的value元素类型
     * @param map
     *            源map
     * @param keyMapper
     *            key转换处理器，<code>entry->{}</code>，为空时返回空map
     * @param valueMapper
     *            value转换处理器，<code>entry->{}</code>，为空时返回空map
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标map
     * @creator pengjianqiang@2021年5月11日
     */
    @SafeVarargs
    public static <SK, SV, K, V> Map<K, V> transformToMap(Map<SK, SV> map, Function<Entry<SK, SV>, K> keyMapper,
        Function<Entry<SK, SV>, V> valueMapper, Predicate<Entry<SK, SV>>... predicate) {
        if (CheckUtils.isEmpty(map) || CheckUtils.isNull(keyMapper) || CheckUtils.isNull(valueMapper)) {
            return new HashMap<>();
        }

        Stream<Entry<SK, SV>> stream = map.entrySet().stream();
        if (map instanceof LinkedHashMap) {
            // 如果map本身有序，则返回一个有序map
            // 先根据参数传入的断言过滤数据，然后再转换
            return filterStream(stream, mergeNotNullPredicate(predicate))
                .collect(Collectors.toMap(keyMapper, valueMapper, (value1, value2) -> value2, LinkedHashMap::new));
        } else {
            return transformToMap(stream, keyMapper, valueMapper, predicate);
        }
    }

    /**
     * 根据mapper的处理转成目标map<br>
     * 如果转换后有相同key，则后面的value值覆盖前面的
     * 
     * @param <S>
     *            源数组元素类型
     * @param <K>
     *            目标map的key元素类型
     * @param <V>
     *            目标map的value元素类型
     * @param array
     *            源数组
     * @param keyMapper
     *            key转换处理器，<code>e->{}</code>，为空时返回空map
     * @param valueMapper
     *            value转换处理器，<code>e->{}</code>，为空时返回空map
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标map
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, K, V> Map<K, V> transformToMap(S[] array, Function<S, K> keyMapper, Function<S, V> valueMapper,
        Predicate<S>... predicate) {
        if (CheckUtils.isEmpty(array) || CheckUtils.isNull(keyMapper) || CheckUtils.isNull(valueMapper)) {
            return new HashMap<>();
        }
        return transformToMap(Arrays.stream(array), keyMapper, valueMapper, predicate);
    }

    /**
     * 根据mapper的处理转成目标map<br>
     * 如果转换后有相同key，则后面的value值覆盖前面的
     * 
     * @param <S>
     *            源stream元素类型
     * @param <K>
     *            目标map的key元素类型
     * @param <V>
     *            目标map的value元素类型
     * @param stream
     *            源stream
     * @param keyMapper
     *            key转换处理器，<code>e->{}</code>，为空时返回空map
     * @param valueMapper
     *            value转换处理器，<code>e->{}</code>，为空时返回空map
     * @param predicate
     *            过滤条件，满足该条件的元素才会被转换
     * @return 目标map
     * @creator pengjianqiang@2021年4月20日
     */
    @SafeVarargs
    public static <S, K, V> Map<K, V> transformToMap(Stream<S> stream, Function<S, K> keyMapper,
        Function<S, V> valueMapper, Predicate<S>... predicate) {
        if (CheckUtils.isNull(stream) || CheckUtils.isNull(keyMapper) || CheckUtils.isNull(valueMapper)) {
            return new HashMap<>();
        }

        // 先根据参数传入的断言过滤数据，然后再转换
        return filterStream(stream, mergeNotNullPredicate(predicate))
            .collect(Collectors.toMap(keyMapper, valueMapper, (value1, value2) -> value2));
    }

    /**
     * 移除集合中的Null元素
     * 
     * @param <S>
     *            源集合元素类型
     * @param collection
     *            要移除Null的集合
     * @creator pengjianqiang@2022年6月28日
     */
    public static <S> void removeNull(Collection<S> collection) {
        collection.removeIf(CheckUtils::isNull);
    }

    /**
     * 移除Map中key和value都Null的元素
     * 
     * @param <K>
     *            map的key元素类型
     * @param <V>
     *            map的value元素类型
     * @param map
     *            要移除Null的map
     * @creator pengjianqiang@2022年6月28日
     */
    public static <K, V> void removeNull(Map<K, V> map) {
        map.remove(null, null);
    }

    /**
     * 查找对象在数组中的下标
     * 
     * @param array
     *            数组
     * @param objectToFind
     *            目标对象
     * @return 下标值，找不到时返回-1
     * @creator pengjianqiang@2021年4月27日
     */
    public static int indexOf(Object[] array, Object objectToFind) {
        return ArrayUtils.indexOf(array, objectToFind);
    }

    @NoArgsConstructor
    @SuppressWarnings("serial")
    public static class Break extends RuntimeException {
        public Break(String msg) {
            super(msg);
        }

        public Break(Throwable cause) {
            super(cause);
        }
    }

    @NoArgsConstructor
    @SuppressWarnings("serial")
    public static class Continue extends RuntimeException {
        public Continue(String msg) {
            super(msg);
        }

        public Continue(Throwable cause) {
            super(cause);
        }
    }
}