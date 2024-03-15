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
package pjq.commons.utils;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 简单的字符串工具类
 *
 * @author pengjianqiang
 * @date 2022-04-05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {
    private static final Pattern PATTERN_CHINESE = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * 替换字符串中的换行符
     *
     * @param str
     * @param isChinese
     *         如果是英文内容，则把换行符替换成空格
     * @return
     */
    public static String cleanLineSeparator(String str, boolean isChinese) {
        return cleanLineSeparator(str, isChinese ? "" : " ");
    }

    /**
     * 替换字符串中的换行符
     *
     * @param str
     * @return
     */
    public static String cleanLineSeparator(String str, String replacement) {
        if (CheckUtils.isNotEmpty(str)) {
            //换行替换成空字符串
            return str.replaceAll("\r\n", replacement).replaceAll("\n\r", replacement).replaceAll("\r", replacement)
                    .replaceAll("\n", replacement).trim();
        } else {
            return str;
        }
    }

    /**
     * 判断字符串是否包含中文
     *
     * @param str
     * @return
     */
    public static boolean containChinese(String str) {
        return PATTERN_CHINESE.matcher(str).find();
    }
}