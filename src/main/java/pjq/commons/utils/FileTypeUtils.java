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
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import cn.hutool.core.io.FileUtil;

import pjq.commons.utils.collection.CollectionUtils;

/**
 * 文件类型相关工具类<br>
 * 读取文件的前几个字节，文件后缀不存在也可以解析
 *
 * @author pengjianqiang
 * @date 2023年11月6日
 */
public final class FileTypeUtils {
    private FileTypeUtils() {
    }

    private static final Tika DEFAULT_TIKA = new Tika();
    private static final MimeTypes DEFAULT_MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    private static final String EMPTY_EXTENSION = "";

    private static final String[] EXECUTABLE_OR_SYS_EXTENSIONS = new String[]{
            "exe", "msi", "msp", "mst", "sys", "dll", "com", "winmd", "drv", "pif", "bat", "cmd", "reg", /*windows*/
            "vbs", "vba", /*windows*/
            "so", "sh", "bin", "rpm", "py", "deb", /*linux*/
            "pkg", "dmg", /*mac os*/
            "dex", /*安卓平台的虚拟机*/
            "jar", "class", /*java*/
            "apk", "ipa", "hap" /*手机系统安装文件(安卓、ios、鸿蒙)*/
    };
    private static final String[] EXECUTABLE_OR_SYS_MIMETYPES = new String[]{
            "application/x-dosexec", "application/x-msdownload", "application/x-ms-installer", /*windows*/
            "application/x-bat", "text/x-vbscript", /*windows*/
            "application/x-rpm", "application/x-executable", "application/x-sharedlib", /*linux*/
            "application/x-sh", "text/x-python", /*linux*/
            "application/x-debian-package", /*debian*/
            "application/x-apple-diskimage", /*mac os*/
            "application/x-dex", /*安卓平台的虚拟机*/
            "application/java-archive", "application/java-vm", /*java*/
            "application/vnd.android.package-archive", "application/x-itunes-ipa" /*手机系统安装文件(安卓、ios)*/
    };

    static {
        try {
            //以下额外绑定的mimeType和文件后缀类型关系可能都绑定不全，需要注意
            //exe等部分文件解析出来的mimeType可能为application/x-msdownload
            MimeType windowsExeOrSysFileType1 = DEFAULT_MIME_TYPES.getRegisteredMimeType("application/x-msdownload");
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType1, "*.exe");
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType1, "*.sys"); //系统文件
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType1, "*.winmd"); //系统元数据文件
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType1, "*.drv"); //系统驱动文件
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType1, "*.pif"); //可能是病毒文件

            //lnk文件文件解析出来的mimeType可能为application/octet-stream
            MimeType windowsExeOrSysFileType2 = DEFAULT_MIME_TYPES.getRegisteredMimeType("application/octet-stream");
            DEFAULT_MIME_TYPES.addPattern(windowsExeOrSysFileType2, "*.lnk");

            //bin文件解析出来的mimeType可能为application/x-shn
            MimeType linuxExeOrSysFileType1 = DEFAULT_MIME_TYPES.getRegisteredMimeType("application/x-sh");
            DEFAULT_MIME_TYPES.addPattern(linuxExeOrSysFileType1, "*.bin");

            //so文件解析出来的mimeType可能为application/x-sharedlib
            MimeType linuxExeOrSysFileType2 = DEFAULT_MIME_TYPES.getRegisteredMimeType("application/x-sharedlib");
            DEFAULT_MIME_TYPES.addPattern(linuxExeOrSysFileType2, "*.so"); //动态链接库
        } catch (Exception e) {
            //报错不用处理，而且try中获取的mimeType都不会报错
        }
    }

    /**
     * 获取文件Mime类型
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static String getMimeType(String filePath) {
        return getMimeType(new File(filePath));
    }

    /**
     * 获取文件Mime类型
     *
     * @param file
     *         文件对象
     * @return
     */
    public static String getMimeType(File file) {
        if (CheckUtils.isNull(file)) {
            throw new RuntimeException("文件对象为空");
        }

        String filePath = null;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("文件路径不规范");
        }
        if (!file.exists()) {
            throw new RuntimeException("文件不存在[" + filePath + "]");
        } else if (file.isDirectory()) {
            throw new RuntimeException("路径不是一个文件[" + filePath + "]");
        }

        try {
            return DEFAULT_TIKA.detect(file);
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败[" + filePath + "]");
        }
    }

    private static String getMimeTypeWithParam(File file, String... mimeTypeStr) {
        return DefaultValueGetter.get((Supplier<String>) () -> getMimeType(file), mimeTypeStr);
    }

    /**
     * 获取文件后缀类型<br>
     * 解析文件头进行获取，不是直接从文件名获取
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static String getExtension(String filePath) {
        return getExtension(new File(filePath));
    }

    /**
     * 获取文件后缀类型<br>
     * 解析文件头进行获取，不是直接从文件名获取
     *
     * @param file
     *         文件对象
     * @param mimeTypeStr
     *         文件mimeType值，为空时从file对象中获取，不为空时直接使用(一般使用时不需要传这个参数)
     * @return
     */
    public static String getExtension(File file, String... mimeTypeStr) {
        String mimeType = getMimeTypeWithParam(file, mimeTypeStr);
        try {
            //获取mimeType对应的后缀类型列表
            MimeType mimeTypeObj = DEFAULT_MIME_TYPES.getRegisteredMimeType(mimeType);
            List<String> extensionsWithDot = mimeTypeObj.getExtensions();
            if (CheckUtils.isEmpty(extensionsWithDot) && mimeTypeObj.getType().hasParameters()) {
                //如果列表为空，则可能是以下原因：
                //mimeType为"application/x-rar-compressed;version=5"这种带参数的形式
                //则尝试获取其基础mimeType(例如"application/x-rar-compressed")对应的后缀类型列表
                mimeType = mimeTypeObj.getType().getBaseType().toString();
                extensionsWithDot = DEFAULT_MIME_TYPES.getRegisteredMimeType(mimeType).getExtensions();
            }
            if (CheckUtils.isEmpty(extensionsWithDot)) {
                return EMPTY_EXTENSION;
            }

            //判断文件名中的后缀类型是否和解析出的后缀类型一致
            List<String> extensionsWithoutDot = CollectionUtils.transformToList(extensionsWithDot,
                    extension -> extension.substring(1));
            String extensionFromFileName = getLowerCaseExtension(file);
            for (String extension : extensionsWithoutDot) {
                if (extension.equals(extensionFromFileName)) {
                    return extension; //文件名中的后缀匹配则返回
                }
            }

            //处理dmg文件解析出bz后缀类型的情况(不在static块中用addPattern绑定是因为dmg本身有对应的mimeType，不能绑定另一个mimeType)
            if ("dmg".equals(extensionFromFileName) && "bz".equals(extensionsWithoutDot.get(0))) {
                //当文件名中的后缀为dmg时，解析出来的mimeType可能为"application/x-bzip"之类的情况
                //这时如果按后缀为bz返回的话会和文件名中的不一致，所以需要特殊处理下(tika-mimetypes.xml中可看出原因)
                return "dmg";
            }

            return extensionsWithoutDot.get(0); //文件名中不含后缀或后缀错误，则返回检测到的后缀列表中的第一个
        } catch (Exception e) {
            return EMPTY_EXTENSION; //mimeType不合法则返回空字符串
        }
    }

    private static String getLowerCaseExtension(File file) {
        String extensionFromFileName = StringUtils.lowerCase(FileUtil.getSuffix(file));
        if (StringUtils.lowerCase(file.getName()).contains(".so.")) {
            return "so"; //linux的so文件，有时是XX.so.1的文件名，后面的".1"是文件的版本后，文件实际还是so文件，需要处理下
        } else {
            return extensionFromFileName;
        }
    }

    /**
     * 严格模式判断文件是否为系统可执行文件或关键文件
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static boolean isExecutableOrSysFile(String filePath) {
        return isExecutableOrSysFile(new File(filePath));
    }

    /**
     * 严格模式判断文件是否为系统可执行文件或关键文件
     *
     * @param file
     *         文件对象
     * @return
     */
    public static boolean isExecutableOrSysFile(File file) {
        return isExecutableOrSysFile(file, true);
    }

    /**
     * 判断文件是否为系统可执行文件或关键文件
     *
     * @param filePath
     *         文件绝对路径
     * @param strict
     *         严格模式，为true时只从文件解析出的mimeType进行判断，为false时会先从文件名的后缀进行判断
     * @return
     */
    public static boolean isExecutableOrSysFile(String filePath, boolean strict) {
        return isExecutableOrSysFile(new File(filePath), strict);
    }

    /**
     * 判断文件是否为系统可执行文件或关键文件
     *
     * @param file
     *         文件对象
     * @param strict
     *         严格模式，为true时只从文件解析出的mimeType进行判断，为false时会先从文件名的后缀进行判断
     * @return
     */
    public static boolean isExecutableOrSysFile(File file, boolean strict) {
        String extensionFromFileName = getLowerCaseExtension(file);
        if (!strict && CollectionUtils.indexOf(EXECUTABLE_OR_SYS_EXTENSIONS, extensionFromFileName) > -1) {
            return true; //先文件名的后缀进行判断
        }

        //再从文件解析出的mimeType进行判断
        return CollectionUtils.indexOf(EXECUTABLE_OR_SYS_MIMETYPES, getMimeType(file)) > -1;
    }

    /**
     * 判断文件是否为图片类型
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static boolean isImage(String filePath) {
        return isImage(new File(filePath));
    }

    /**
     * 判断文件是否为图片类型
     *
     * @param file
     *         文件对象
     * @param mimeTypeStr
     *         文件mimeType值，为空时从file对象中获取，不为空时直接使用(一般使用时不需要传这个参数)
     * @return
     */
    public static boolean isImage(File file, String... mimeTypeStr) {
        String mimeType = getMimeTypeWithParam(file, mimeTypeStr);
        return mimeType.startsWith("image/") || "application/coreldraw".equalsIgnoreCase(mimeType); //coreldraw是cdr文件
    }

    /**
     * 判断文件是否为常用的office文档(doc/docx、xls/xlsx、ppt/pptx等)
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static boolean isCommonOfficeFile(String filePath) {
        return isCommonOfficeFile(new File(filePath));
    }

    /**
     * 判断文件是否为常用的office文档(doc/docx、xls/xlsx、ppt/pptx等)
     *
     * @param file
     *         文件对象
     * @param mimeTypeStr
     *         文件mimeType值，为空时从file对象中获取，不为空时直接使用(一般使用时不需要传这个参数)
     * @return
     */
    public static boolean isCommonOfficeFile(File file, String... mimeTypeStr) {
        String macroEnabledSuffix = "macroenabled.12";
        String mimeType = getMimeTypeWithParam(file, mimeTypeStr);
        boolean isCommonDoc = "application/msword".equals(mimeType)
                || mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")
                || (mimeType.startsWith("application/vnd.ms-word") && mimeType.endsWith(macroEnabledSuffix));
        boolean isCommonExcel = "application/vnd.ms-excel".equals(mimeType)
                || mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")
                || (mimeType.startsWith("application/vnd.ms-excel") && mimeType.endsWith(macroEnabledSuffix));
        boolean isCommonPpt = "application/vnd.ms-powerpoint".equals(mimeType)
                || mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml")
                || (mimeType.startsWith("application/vnd.ms-powerpoint") && mimeType.endsWith(macroEnabledSuffix));
        return isCommonDoc || isCommonExcel || isCommonPpt;
    }

    /**
     * 判断文件名的后缀类型是否和文件内容一致(即判断有没有修改过文件名的后缀)
     *
     * @param filePath
     *         文件绝对路径
     * @return
     */
    public static boolean isExtensionMatch(String filePath) {
        return isExtensionMatch(new File(filePath));
    }

    /**
     * 判断文件名的后缀类型是否和文件内容一致(即判断有没有修改过文件名的后缀)
     *
     * @param file
     * @return
     */
    public static boolean isExtensionMatch(File file) {
        String extensionFromFileName = getLowerCaseExtension(file);
        String extension = getExtension(file);
        if (CheckUtils.areEmpty(extensionFromFileName, extension)) {
            //extension为空表示tika解析到文件的mimeType，但是没有定义对应的文件后缀类型，这时也按匹配处理
            return true;
        }
        return extension.equalsIgnoreCase(extensionFromFileName);
    }
}