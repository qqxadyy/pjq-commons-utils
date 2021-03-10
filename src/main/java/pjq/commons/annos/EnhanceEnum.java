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
package pjq.commons.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.utils.CheckUtils;

/**
 * <p>
 * 用于给自定义的枚举类增加value、desc方法<br>
 * 可以用{@link EnhanceEnumFieldDefine}指定获取value、desc值的属性名
 * <p>
 * Create at 2018年11月17日
 * 
 * @author pengjianqiang
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
    public @interface EnhanceEnumFieldDefine {
        /**
         * 获取枚举值的属性名，默认为value
         * 
         * @return
         */
        String valueField() default "value";

        /**
         * 获取枚举描述的属性名，默认为desc
         * 
         * @return
         */
        String descField() default "desc";
    }

    /**
     * 1.如果枚举类有value属性，则value属性值作为枚举值<br>
     * 2.否则使用ordinal值作为枚举值<br>
     * 3.或枚举类重写value方法，用其返回值作为枚举值
     * 
     * @return
     */
    default String value() {
        Class<?> thisClass = getClass();
        CheckUtils.checkNotFalse(thisClass.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

        String enumValue = null;
        try {
            // 如果枚举类有对应属性，则使用该属性值作为枚举的值
            EnhanceEnumFieldDefine anno = thisClass.getAnnotation(EnhanceEnumFieldDefine.class);
            String valueFieldName = CheckUtils.isNotNull(anno) ? anno.valueField() : "value";
            Field valueField = thisClass.getDeclaredField(valueFieldName);
            valueField.setAccessible(true);
            if (CheckUtils.isNotNull(valueField)) {
                enumValue = String.valueOf(valueField.get(this));
            }
        } catch (Exception e) {
        }

        if (CheckUtils.isEmpty(enumValue)) {
            // 没有value属性或获取属性值失败，则用ordinal方法
            enumValue = String.valueOf(((Enum<?>)this).ordinal());
        }
        return enumValue;
    }

    /**
     * 返回int类型的value值，value值不是int类型时返回-1
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
     * 1.如果枚举类有desc属性，则desc属性值作为枚举描述<br>
     * 2.否则使用name()作为枚举描述<br>
     * 3.或枚举类重写desc方法，用其返回值作为枚举描述
     * 
     * @return
     */
    default String desc() {
        Class<?> thisClass = getClass();
        CheckUtils.checkNotFalse(thisClass.isEnum(), "只有枚举类才能实现" + EnhanceEnum.class.getSimpleName() + "接口");

        String enumDesc = null;
        try {
            // 如果枚举类有对应属性，则使用该属性值作为枚举的描述
            EnhanceEnumFieldDefine anno = thisClass.getAnnotation(EnhanceEnumFieldDefine.class);
            String descFieldName = CheckUtils.isNotNull(anno) ? anno.descField() : "desc";
            Field descField = thisClass.getDeclaredField(descFieldName);
            descField.setAccessible(true);
            if (CheckUtils.isNotNull(descField)) {
                enumDesc = String.valueOf(descField.get(this));
            }
        } catch (Exception e) {
        }

        if (CheckUtils.isEmpty(enumDesc)) {
            // 没有desc属性或获取属性值失败，则用name方法
            enumDesc = String.valueOf(((Enum<?>)this).name());
        }
        return enumDesc;
    }

    /**
     * 默认实现ValuableEnum的类，用于根据枚举值查找枚举类型
     * 
     * @author pengjianqiang
     * @date 2021年2月7日
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DefualtEnhanceEnum implements EnhanceEnum {
        /**
         * 根据枚举值/名查找枚举类型
         * 
         * @param enumTypeName
         *            枚举类的名
         * @param valueOrName
         *            枚举类的值/名
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends Enum<T>> T valueOrNameOf(String enumTypeName, String valueOrName) {
            if (CheckUtils.isEmpty(enumTypeName)) {
                return null;
            }

            try {
                return valueOrNameOf((Class<T>)Class.forName(enumTypeName), valueOrName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * 根据枚举值/名查找枚举类型
         * 
         * @param enumType
         *            枚举类的class
         * @param valueOrName
         *            枚举类的值/名
         * @return
         */
        public static <T extends Enum<T>> T valueOrNameOf(Class<T> enumType, String valueOrName) {
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
                        if (valueOrName.equals(((EnhanceEnum)tempEnum).value())) {
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
         * @param enumType
         * @param valueOrName
         * @return
         * @throws Exception
         * @creator pengjianqiang@2021年3月10日
         */
        public static <T extends Enum<T>> boolean isEnumOf(Class<T> enumType, String valueOrName) throws Exception {
            T eunmObj = valueOrNameOf(enumType, valueOrName);
            return CheckUtils.isNotNull(eunmObj);
        }
    }
}