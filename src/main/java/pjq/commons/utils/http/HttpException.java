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
package pjq.commons.utils.http;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
public class HttpException extends RuntimeException {
    private @Getter @Setter int statusCode;

    /**
     * 原始报错字符串
     */
    private @Getter @Setter String oriErrMsg;

    /**
     * 
     * @param statusCode
     *            http状态码
     * @param msg
     *            异常信息
     * @param oriErrMsg
     *            请求报错的原始报文串
     * @param msg
     */
    public HttpException(int statusCode, String msg, String oriErrMsg) {
        super(msg);
        this.statusCode = statusCode;
        this.oriErrMsg = oriErrMsg;
    }

    public String getConcatedMsg() {
        return getMessage() + "[" + oriErrMsg + "]";
    }
}