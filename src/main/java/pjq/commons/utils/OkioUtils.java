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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    private static void checkFilePath(String filePath, String nameDesc) {
        CheckUtils.checkNotEmpty(filePath, nameDesc + NOT_NULL_MSG);
    }

    private static void checkFile(File file, String nameDesc) {
        CheckUtils.checkNotNull(file, nameDesc + NOT_NULL_MSG);
        CheckUtils.checkNotTrue(file.isDirectory(), nameDesc + "不能是目录");
    }

    private static void checkObject(Object stream, String nameDesc) {
        CheckUtils.checkNotNull(stream, nameDesc + NOT_NULL_MSG);
    }

    private static File[] checkSrcAndDestFile(File srcFile, String srcFilePath, File destFile, String descFilePath) {
        if (CheckUtils.isNull(srcFile)) {
            checkFilePath(srcFilePath, "源文件路径");
            srcFile = new File(srcFilePath);
        }
        if (CheckUtils.isNull(destFile)) {
            checkFilePath(descFilePath, "目标文件路径");
            destFile = new File(descFilePath);
        }
        checkFile(srcFile, "源文件对象");
        checkFile(destFile, "目标文件对象");

        destFile.getParentFile().mkdirs(); //保证目标文件的路径存在

        return new File[]{ srcFile, destFile };
    }

    /**
     * 读文件并返回每行字符串
     *
     * @param filePath
     *         文件路径
     * @return
     */
    public static List<String> readLines(String filePath) {
        checkFilePath(filePath, "文件路径");
        return readLines(new File(filePath));
    }

    /**
     * 读文件并返回每行字符串
     *
     * @param file
     *         文件对象
     * @return
     */
    public static List<String> readLines(File file) {
        checkFile(file, "文件对象");
        try {
            return readLines(Files.newInputStream(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读输入流并返回每行字符串
     *
     * @param inputStream
     *         输入流对象
     * @return
     */
    public static List<String> readLines(InputStream inputStream) {
        checkObject(inputStream, "文件流对象");
        List<String> lines = new ArrayList<>();
        try (BufferedSource bufferedSource = Okio.buffer(Okio.source(inputStream))) {
            for (String line; null != (line = bufferedSource.readUtf8Line()); ) {
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
     *         文件路径
     * @return
     */
    public static ByteString readByteString(String filePath) {
        checkFilePath(filePath, "文件路径");
        return readByteString(new File(filePath));
    }

    /**
     * 读文件并返回byteString对象
     *
     * @param file
     *         文件对象
     * @return
     */
    public static ByteString readByteString(File file) {
        checkFile(file, "文件对象");
        try {
            return readByteString(Files.newInputStream(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读输入流并返回byteString对象
     *
     * @param inputStream
     *         输入流对象
     * @return
     */
    public static ByteString readByteString(InputStream inputStream) {
        checkObject(inputStream, "文件流对象");
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
     *         源文件对象
     * @param destFile
     *         目标文件对象
     */
    public static void copyFile(File srcFile, File destFile) {
        copyFile(srcFile, null, destFile, null);
    }

    /**
     * 拷贝文件到目标文件(强制覆盖)
     *
     * @param srcFilePath
     *         源文件路径
     * @param descFilePath
     *         目标文件路径
     */
    public static void copyFile(String srcFilePath, String descFilePath) {
        copyFile(null, srcFilePath, null, descFilePath);
    }

    /**
     * 拷贝文件到目标文件(强制覆盖)<br>
     * xxxFile为空时，从xxxFilePath获取文件对象<br>
     *
     * @param srcFile
     *         源文件对象
     * @param srcFilePath
     *         源文件路径
     * @param destFile
     *         目标文件对象
     * @param descFilePath
     *         目标文件路径
     */
    public static void copyFile(File srcFile, String srcFilePath, File destFile, String descFilePath) {
        File[] files = checkSrcAndDestFile(srcFile, srcFilePath, destFile, descFilePath);
        try {
            copy(Okio.source(files[0]), Okio.sink(files[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝文件到输出流
     *
     * @param srcFile
     *         源文件对象
     * @param srcFilePath
     *         源文件路径
     * @param outputStream
     *         输出流对象
     */
    public static void copyFile(File srcFile, String srcFilePath, OutputStream outputStream) {
        if (CheckUtils.isNull(srcFile)) {
            checkFilePath(srcFilePath, "源文件路径");
            srcFile = new File(srcFilePath);
        }
        checkFile(srcFile, "源文件对象");
        checkObject(outputStream, "输出流对象");
        try {
            copy(Okio.source(srcFile), Okio.sink(outputStream));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 移动文件(强制覆盖)
     *
     * @param srcFile
     *         源文件对象
     * @param destFile
     *         目标文件对象
     */
    public static void moveFile(File srcFile, File destFile) {
        moveFile(srcFile, null, destFile, null);
    }

    /**
     * 移动文件(强制覆盖)
     *
     * @param srcFilePath
     *         源文件路径
     * @param descFilePath
     *         目标文件路径
     */
    public static void moveFile(String srcFilePath, String descFilePath) {
        moveFile(null, srcFilePath, null, descFilePath);
    }

    /**
     * 移动文件(强制覆盖)<br>
     * xxxFile为空时，从xxxFilePath获取文件对象<br>
     *
     * @param srcFile
     *         源文件对象
     * @param srcFilePath
     *         源文件路径
     * @param destFile
     *         目标文件对象
     * @param descFilePath
     *         目标文件路径
     */
    public static void moveFile(File srcFile, String srcFilePath, File destFile, String descFilePath) {
        File[] files = checkSrcAndDestFile(srcFile, srcFilePath, destFile, descFilePath);
        try {
            Files.move(files[0].toPath(), files[1].toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝流
     *
     * @param inputStream
     *         输入流对象
     * @param outputStream
     *         输出流对象
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStream) {
        checkObject(inputStream, "输入流对象");
        checkObject(outputStream, "输出流对象");
        copy(Okio.source(inputStream), Okio.sink(outputStream));
    }

    /**
     * 把byteString输出到sink
     *
     * @param byteString
     *         源输入对象
     * @param sink
     *         目标输出对象
     */
    public static void copy(ByteString byteString, Sink sink) {
        checkObject(byteString, "源输入对象");
        checkObject(sink, "目标输出对象");
        try (BufferedSink bufferedSink = Okio.buffer(sink)) {
            bufferedSink.write(byteString.asByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 把byteString输出到文件
     *
     * @param byteString
     *         源输入对象
     * @param filePath
     *         目标文件路径
     * @return
     */
    public static void copy(ByteString byteString, String filePath) {
        checkFilePath(filePath, "目标文件路径");
        copy(byteString, new File(filePath));
    }

    /**
     * 把byteString输出到文件
     *
     * @param byteString
     *         源输入对象
     * @param file
     *         目标文件对象
     */
    public static void copy(ByteString byteString, File file) {
        checkObject(byteString, "源输入对象");
        checkFile(file, "目标文件对象");
        try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(file))) {
            bufferedSink.write(byteString.asByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 直接把source输出到sink
     *
     * @param source
     *         源输入对象
     * @param sink
     *         目标输出对象
     */
    public static void copy(Source source, Sink sink) {
        checkObject(source, "源输入对象");
        checkObject(sink, "目标输出对象");
        try (BufferedSink bufferedSink = Okio.buffer(sink); BufferedSource bufferedSource = Okio.buffer(source)) {
            bufferedSink.writeAll(bufferedSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}