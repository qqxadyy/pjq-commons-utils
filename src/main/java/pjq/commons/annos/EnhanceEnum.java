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
package pjq.commons.annos;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.utils.CheckUtils;

/**
 * 用于给自定义的枚举类增加value、desc、group方法<br>
 * 可以用{@link EnhanceEnumFieldDefine}指定获取value、desc、group值的属性名
 *
 * @author pengjianqiang
 * @date 2018年11月17日
 */
public interface EnhanceEnum {
    /**
     * 用于定义从枚举的哪个属性获取对应值
     *
     * @author pengjianqiang
     * @date 2021年2月7日
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @interface EnhanceEnumFieldDefine {
        /**
         * 获取枚举值的属性名，默认为value
         */
        String valueField() default "value";

        /**
         * 使用枚举名作为枚举值，值不为{@link EnumNameAsValueType#DISABLE}时优先于{@link #valueField()}生效
         */
        EnumNameAsValueType nameAsValue() default EnumNameAsValueType.DISABLE;

        /**
         * 获取枚举描述的属性名，默认为desc
         */
        String descField() default "desc";

        /**
         * 获取枚举分组的属性名，默认为group
         */
        String groupField() default "group";
    }

    /**
     * 枚举名作为value值时的取值方式
     *
     * @author pengjianqiang
     * @date 2021年4月17日
     */
    enum EnumNameAsValueType {
        /**
         * 没有配置
         */
        DISABLE,

        /**
         * 默认，枚举名作为value值
         */
        DEFAULT,

        /**
         * 小写枚举名作为value值
         */
        LOWER_CASE,

        /**
         * 大写枚举名作为value值
         */
        UPPER_CASE;
    }

    /**
     * 1.如果有使用{@link EnhanceEnumFieldDefine}注解，则按该注解的配置获取枚举值<br>
     * 2.否则使用ordinal值作为枚举值<br>
     * 3.或枚举类重写value方法，用其返回值作为枚举值
     *
     * @return
     */
    default String value() {
        Enum<?> thisEnum = ((Enum<?>) this);
        Class<?> thisClass = getClass();
        CheckUtils.checkNotFalse(thisClass.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

        String targetValue = null;
        try {
            // 如果枚举类有对应属性，则使用该属性值作为枚举的值
            EnhanceEnumFieldDefine anno = thisClass.getAnnotation(EnhanceEnumFieldDefine.class);
            EnumNameAsValueType nameAsValueType =
                    CheckUtils.isNotNull(anno) ? anno.nameAsValue() : EnumNameAsValueType.DISABLE;
            if (EnumNameAsValueType.DISABLE.equals(nameAsValueType)) {
                String targetFieldName = CheckUtils.isNotNull(anno) ? anno.valueField() : "value";
                Field targetField = thisClass.getDeclaredField(targetFieldName);
                targetField.setAccessible(true);
                if (CheckUtils.isNotNull(targetField)) {
                    targetValue = String.valueOf(targetField.get(this));
                }
            } else {
                // 有配置nameAsValueType时优先生效
                switch (nameAsValueType) {
                    case LOWER_CASE:
                        targetValue = thisEnum.name().toLowerCase();
                        break;
                    case UPPER_CASE:
                        targetValue = thisEnum.name().toUpperCase();
                        break;
                    default:
                        targetValue = thisEnum.name();
                        break;
                }
            }
        } catch (Exception e) {
        }

        if (CheckUtils.isEmpty(targetValue)) {
            // 没有value属性或获取属性值失败，则用ordinal方法
            targetValue = String.valueOf(thisEnum.ordinal());
        }
        return targetValue;
    }

    /**
     * 返回int类型的value值，value值不是int类型时返回{@link Integer#MIN_VALUE}
     *
     * @return
     */
    default Integer valueOfInt() {
        try {
            return Integer.valueOf(value());
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * 返回byte类型的value值，value值不是byte类型时返回{@link Byte#MIN_VALUE}
     *
     * @return
     */
    default Byte valueOfByte() {
        try {
            return Byte.valueOf(value());
        } catch (Exception e) {
            return Byte.MIN_VALUE;
        }
    }

    /**
     * 1.如果有使用{@link EnhanceEnumFieldDefine}注解，则按该注解的配置枚举描述<br>
     * 2.否则使用name()作为枚举描述<br>
     * 3.或枚举类重写desc方法，用其返回值作为枚举描述
     *
     * @return
     */
    default String desc() {
        Enum<?> thisEnum = ((Enum<?>) this);
        Class<?> thisClass = getClass();
        CheckUtils.checkNotFalse(thisClass.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

        String targetValue = null;
        try {
            // 如果枚举类有对应属性，则使用该属性值作为枚举的描述
            EnhanceEnumFieldDefine anno = thisClass.getAnnotation(EnhanceEnumFieldDefine.class);
            String targetFieldName = CheckUtils.isNotNull(anno) ? anno.descField() : "desc";
            Field targetField = thisClass.getDeclaredField(targetFieldName);
            targetField.setAccessible(true);
            if (CheckUtils.isNotNull(targetField)) {
                targetValue = String.valueOf(targetField.get(this));
            }
        } catch (Exception e) {
        }

        if (CheckUtils.isEmpty(targetValue)) {
            // 没有desc属性或获取属性值失败，则用name方法
            targetValue = String.valueOf(thisEnum.name());
        }
        return targetValue;
    }

    /**
     * 1.如果有使用{@link EnhanceEnumFieldDefine}注解，则按该注解的配置枚举分组<br>
     * 2.否则返回null<br>
     * 3.或枚举类重写group方法，用其返回值作为枚举分组
     *
     * @return
     */
    default String group() {
        Class<?> thisClass = getClass();
        CheckUtils.checkNotFalse(thisClass.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

        String targetValue = null;
        try {
            // 如果枚举类有对应属性，则使用该属性值作为枚举的分组
            EnhanceEnumFieldDefine anno = thisClass.getAnnotation(EnhanceEnumFieldDefine.class);
            String targetFieldName = CheckUtils.isNotNull(anno) ? anno.groupField() : "group";
            Field targetField = thisClass.getDeclaredField(targetFieldName);
            targetField.setAccessible(true);
            if (CheckUtils.isNotNull(targetField)) {
                targetValue = String.valueOf(targetField.get(this));
            }
        } catch (Exception e) {
        }
        return targetValue;
    }

    /**
     * 默认实现{@link EnhanceEnum}的类，用于根据枚举值查找枚举类型
     *
     * @author pengjianqiang
     * @date 2021年2月7日
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DefaultEnhanceEnum implements EnhanceEnum {
        /**
         * 根据枚举值/名查找枚举类型
         *
         * @param <T>
         *         枚举类型
         * @param enumTypeName
         *         枚举类的名
         * @param valueOrName
         *         枚举类的值/名
         * @return 枚举对象
         * @author pengjianqiang@2021年4月20日
         */
        @SuppressWarnings("unchecked")
        public static <T extends Enum<T>> T valueOrNameOf(String enumTypeName, String valueOrName) {
            if (CheckUtils.isEmpty(enumTypeName)) {
                return null;
            }

            try {
                return valueOrNameOf((Class<T>) Class.forName(enumTypeName), valueOrName);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 根据枚举值/名查找枚举类型
         *
         * @param <T>
         *         枚举类型
         * @param enumType
         *         枚举类的class
         * @param valueOrName
         *         枚举类的值/名
         * @return 枚举对象
         * @author pengjianqiang@2021年4月20日
         */
        public static <T extends Enum<T>> T valueOrNameOf(Class<T> enumType, String valueOrName) {
            if (CheckUtils.isNull(enumType) || CheckUtils.isEmpty(valueOrName)) {
                return null;
            }
            CheckUtils.checkNotFalse(enumType.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

            T targetEnum = null;
            try {
                targetEnum = Enum.valueOf(enumType, valueOrName);
            } catch (Exception e) {
                // 根据枚举名没找到，则尝试使用value查找
                targetEnum = null;
                if (EnhanceEnum.class.isAssignableFrom(enumType)) {
                    T[] enums = enumType.getEnumConstants();
                    for (T tempEnum : enums) {
                        if (valueOrName.equals(((EnhanceEnum) tempEnum).value())) {
                            targetEnum = tempEnum;
                        }
                    }
                }
            }
            return targetEnum;
        }

        /**
         * 判断枚举值是否属于枚举对象
         *
         * @param <T>
         *         枚举类型
         * @param enumType
         *         枚举类的class
         * @param valueOrName
         *         枚举类的值/名
         * @return true/false
         * @author pengjianqiang@2021年4月20日
         */
        public static <T extends Enum<T>> boolean isEnumOf(Class<T> enumType, String valueOrName) {
            try {
                T eunmObj = valueOrNameOf(enumType, valueOrName);
                return CheckUtils.isNotNull(eunmObj);
            } catch (Exception e) {
                return false;
            }
        }
    }
}