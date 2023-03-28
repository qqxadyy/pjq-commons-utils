/*
 * Copyright © 2023 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-commons-utils
 * 项目描述：pjq-commons-utils
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

import java.nio.charset.Charset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 字符集工具类
 *
 * @author pengjianqiang
 * @date 2019年1月15日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CharsetUtils {
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final String GB2312 = "GB2312";
    public static final String GB18030 = "GB18030";
    public static final String ISO_8859_1 = "ISO-8859-1";

    public static Charset utf8() {
        return forName(UTF_8);
    }

    public static Charset gbk() {
        return forName(GBK);
    }

    public static Charset gb2312() {
        return forName(GB2312);
    }

    public static Charset gb18030() {
        return forName(GB18030);
    }

    public static Charset iso8859_1() {
        return forName(ISO_8859_1);
    }

    public static Charset forName(String charsetName) {
        return Charset.forName(charsetName);
    }
}