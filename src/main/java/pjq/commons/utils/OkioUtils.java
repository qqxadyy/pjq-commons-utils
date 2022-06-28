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
package pjq.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * okio工具类
 * 
 * @author pengjianqiang
 * @date 2022年6月28日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OkioUtils {
    private static final String NOT_NULL_MSG = "不能为空";

    /**
     * 读文件并返回每行字符串
     * 
     * @param filePath
     *            文件路径
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    public static List<String> readLines(String filePath) {
        CheckUtils.checkNotNull(filePath, "文件路径" + NOT_NULL_MSG);
        return readLines(new File(filePath));
    }

    /**
     * 读文件并返回每行字符串
     * 
     * @param file
     *            文件对象
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    public static List<String> readLines(File file) {
        CheckUtils.checkNotNull(file, "文件对象" + NOT_NULL_MSG);
        try {
            return readLines(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读输入流并返回每行字符串
     * 
     * @param inputStream
     *            输入流对象
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    public static List<String> readLines(InputStream inputStream) {
        CheckUtils.checkNotNull(inputStream, "文件流对象" + NOT_NULL_MSG);
        List<String> lines = new ArrayList<>();
        try (BufferedSource bufferedSource = Okio.buffer(Okio.source(inputStream))) {
            for (String line; null != (line = bufferedSource.readUtf8Line());) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读文件并返回byteString对象
     * 
     * @param filePath
     *            文件路径
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    public static ByteString readByteString(String filePath) {
        CheckUtils.checkNotNull(filePath, "文件路径" + NOT_NULL_MSG);
        return readByteString(new File(filePath));
    }

    /**
     * 读文件并返回byteString对象
     * 
     * @param file
     *            文件对象
     * @return
     * @creator pengjianqiang@2022年6月28日
     */
    public static ByteString readByteString(File file) {
        CheckUtils.checkNotNull(file, "文件对象" + NOT_NULL_MSG);
        try {
            return readByteString(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读输入流并返回byteString对象
     * 
     * @param inputStream
     *            输入流对象
     * @return
     * @creator pengjianqiang@2022年6月29日
     */
    public static ByteString readByteString(InputStream inputStream) {
        CheckUtils.checkNotNull(inputStream, "文件流对象" + NOT_NULL_MSG);
        try (BufferedSource bufferedSource = Okio.buffer(Okio.source(inputStream))) {
            return bufferedSource.readByteString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝文件到目标文件(强制覆盖)
     * 
     * @param srcFile
     *            源文件对象
     * @param destFile
     *            目标文件对象
     * @creator pengjianqiang@2022年6月28日
     */
    public static void copyFile(File srcFile, File destFile) {
        copyFile(srcFile, null, destFile, null);
    }

    /**
     * 拷贝文件到目标文件(强制覆盖)
     * 
     * @param srcFilePath
     *            源文件路径
     * @param descFilePath
     *            目标文件路径
     * @creator pengjianqiang@2022年6月28日
     */
    public static void copyFile(String srcFilePath, String descFilePath) {
        copyFile(null, srcFilePath, null, descFilePath);
    }

    /**
     * 拷贝文件到目标文件(强制覆盖)<br>
     * xxxFile为空时，从xxxFilePath获取文件对象<br>
     * 
     * @param srcFile
     *            源文件对象
     * @param srcFilePath
     *            源文件路径
     * @param destFile
     *            目标文件对象
     * @param descFilePath
     *            目标文件路径
     * @creator pengjianqiang@2022年6月28日
     */
    public static void copyFile(File srcFile, String srcFilePath, File destFile, String descFilePath) {
        if (CheckUtils.isNull(srcFile)) {
            CheckUtils.checkNotEmpty(srcFilePath, "源文件对象" + NOT_NULL_MSG);
            srcFile = new File(srcFilePath);
        }
        if (CheckUtils.isNull(destFile)) {
            CheckUtils.checkNotEmpty(descFilePath, "目标文件对象" + NOT_NULL_MSG);
            destFile = new File(descFilePath);
        }

        destFile.getParentFile().mkdirs();

        try {
            copy(Okio.source(srcFile), Okio.sink(destFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝文件到输出流
     * 
     * @param srcFile
     *            源文件对象
     * @param srcFilePath
     *            源文件路径
     * @param outputStream
     *            输出流对象
     * @creator pengjianqiang@2022年6月28日
     */
    public static void copyFile(File srcFile, String srcFilePath, OutputStream outputStream) {
        if (CheckUtils.isNull(srcFile)) {
            CheckUtils.checkNotEmpty(srcFilePath, "源文件对象" + NOT_NULL_MSG);
            srcFile = new File(srcFilePath);
        }
        CheckUtils.checkNotNull(outputStream, "输出流对象" + NOT_NULL_MSG);
        try {
            copy(Okio.source(srcFile), Okio.sink(outputStream));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝流
     * 
     * @param inputStream
     *            输入流对象
     * @param outputStream
     *            输出流对象
     * @creator pengjianqiang@2022年6月28日
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStream) {
        CheckUtils.checkNotNull(inputStream, "输入流对象" + NOT_NULL_MSG);
        CheckUtils.checkNotNull(outputStream, "输出流对象" + NOT_NULL_MSG);
        copy(Okio.source(inputStream), Okio.sink(outputStream));
    }

    /**
     * 把byteString输出到sink
     * 
     * @param byteString
     * @param sink
     * @creator pengjianqiang@2022年6月29日
     */
    public static void copy(ByteString byteString, Sink sink) {
        try (BufferedSink bufferedSink = Okio.buffer(sink)) {
            bufferedSink.write(byteString.asByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 直接把source输出到sink
     * 
     * @param source
     * @param sink
     * @creator pengjianqiang@2022年6月29日
     */
    public static void copy(Source source, Sink sink) {
        try (BufferedSink bufferedSink = Okio.buffer(sink); BufferedSource bufferedSource = Okio.buffer(source)) {
            bufferedSink.writeAll(bufferedSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}