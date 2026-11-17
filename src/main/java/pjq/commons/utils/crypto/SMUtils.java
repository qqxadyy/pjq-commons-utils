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
package pjq.commons.utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.StringUtils;

/**
 * 国密SM2、SM3、SM4算法工具类<br>
 * 提供常用场景的相关方法，复杂场景可以直接参考{@link SmUtil}中的方法<br>
 * <ul>
 *     <li>SM2：非对称加密算法(分组加密)，使用场景参考RSA</li>
 *     <li>SM3：密码杂凑算法，使用场景参考MD5/SHA-1/SHA-2</li>
 *     <li>SM4：对称加密算法，使用场景参考DES/AES；默认使用带完整性校验的GCM模式，另提供CTR模式</li>
 * </ul>
 * 加解密和签名验签大致区别：<br>
 * <ul>
 *     <li>SM2加解密：A接收消息，B公钥加密，A私钥解密</li>
 *     <li>SM2签名验签：A发送消息，A私钥签名，B公钥验签</li>
 * </ul>
 *
 * @author pengjianqiang
 * @date 2023-08-11
 * @see SmUtil
 */
public class SMUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * SM4-GCM模式的IV长度(字节)
     */
    private static final int SM4_GCM_IV_LENGTH = 12;

    /**
     * SM4-GCM模式的tag长度(bit)
     */
    private static final int SM4_GCM_TAG_LENGTH_BITS = 128;

    /**
     * SM4-GCM模式的IV中实例前缀长度(字节)
     */
    private static final int SM4_GCM_IV_PREFIX_LENGTH = 6;

    /**
     * SM4-GCM模式IV的48位计数器掩码
     */
    private static final long SM4_GCM_COUNTER_MASK = 0xFFFFFFFFFFFFL;

    /**
     * SM4-CTR模式的IV长度(字节)
     */
    private static final int SM4_CTR_IV_LENGTH = 16;

    /**
     * SM4-CTR模式的IV中实例前缀长度(字节)
     */
    private static final int SM4_CTR_IV_PREFIX_LENGTH = 8;

    /**
     * 当前JVM实例的GCM-IV随机前缀，用于多节点部署时区分不同实例<br>
     * 同一实例内通过自增计数器保证IV不重复，不同实例间依靠随机前缀区分
     */
    private static final byte[] SM4_GCM_IV_PREFIX = randomBytes(SM4_GCM_IV_PREFIX_LENGTH);

    /**
     * 当前JVM实例的CTR-IV随机前缀，作用同GCM-IV前缀
     */
    private static final byte[] SM4_CTR_IV_PREFIX = randomBytes(SM4_CTR_IV_PREFIX_LENGTH);

    /**
     * GCM-IV的48位自增计数器，初值随机，避免应用重启后计数区间与旧进程重叠
     */
    private static final AtomicLong SM4_GCM_IV_COUNTER = new AtomicLong(
            SECURE_RANDOM.nextLong() & SM4_GCM_COUNTER_MASK);

    /**
     * CTR-IV的64位自增计数器，初值随机，作用同GCM-IV计数器
     */
    private static final AtomicLong SM4_CTR_IV_COUNTER = new AtomicLong(SECURE_RANDOM.nextLong());

    /**
     * 生成SM2加密算法的公私钥对
     *
     * @return
     */
    public static SM2KeyPair sm2GenKeyPair() {
        SM2 sm2 = SmUtil.sm2();
        return SM2KeyPair.builder()
                .keyPair(new KeyPair(sm2.getPublicKey(), sm2.getPrivateKey()))
                .privateKey(sm2.getDHex())
                .publicKey(HexUtil.encodeHexStr(sm2.getQ(false)))
                .compressedPublicKey(HexUtil.encodeHexStr(sm2.getQ(true)))
                .build();
    }

    /**
     * SM2加密
     *
     * @param data
     *         待加密字符串
     * @param publicKey
     *         SM2公钥
     * @return 16进制密文。BC库加密出的SM2密文串会以"04"开头，解密时如果不是使用BC库，则需要根据实际情况判断是否需要去掉开头的"04"部分
     */
    public static String sm2Encrypt(String data, String publicKey) {
        return sm2Encrypt(data, publicKey, 0);
    }

    /**
     * SM2解密
     *
     * @param sm2EncryptedData
     *         16进制密文
     * @param privateKey
     *         SM2私钥
     * @return 明文
     */
    public static String sm2Decrypt(String sm2EncryptedData, String privateKey) {
        return sm2Decrypt(sm2EncryptedData, privateKey, 0);
    }

    /**
     * SM2加密<br>
     * 返回Base64编码的密文
     *
     * @param data
     *         待加密字符串
     * @param publicKey
     *         SM2公钥
     * @return Base64编码密文。BC库加密出的SM2密文串会以"04"开头，解密时如果不是使用BC库，则需要根据实际情况判断是否需要去掉开头的"04"部分<br>
     * 可先Base64解码，再转成16进制密文，即可判断密文是否以"04"开头
     */
    public static String sm2EncryptToBase64(String data, String publicKey) {
        return sm2Encrypt(data, publicKey, 1);
    }

    /**
     * SM2解密
     *
     * @param sm2EncryptedData
     *         Base64编码的密文
     * @param privateKey
     *         SM2私钥
     * @return 明文
     */
    public static String sm2DecryptFromBase64(String sm2EncryptedData, String privateKey) {
        return sm2Decrypt(sm2EncryptedData, privateKey, 1);
    }

    private static String sm2Encrypt(String data, String publicKey, int ciphertextEncoding) {
        checkSrcData(data, 0);
        publicKey = sm2CheckKey(publicKey, KeyType.PublicKey);
        if (ciphertextEncoding == 0) {
            //加密并返回16进制密文
            return SmUtil.sm2(null, publicKey).encryptHex(data, KeyType.PublicKey);
        } else {
            //加密并返回Base64编码的密文
            return SmUtil.sm2(null, publicKey).encryptBase64(data, KeyType.PublicKey);
        }
    }

    private static String sm2Decrypt(String sm2EncryptedData, String privateKey, int ciphertextEncoding) {
        checkSrcData(sm2EncryptedData, 1);
        sm2CheckKey(privateKey, KeyType.PrivateKey);

        if (ciphertextEncoding != 0) {
            //如果是Base64编码的密文，则先转成16进制密文
            sm2EncryptedData = HexUtil.encodeHexStr(Base64.decode(sm2EncryptedData));
        }
        if (!sm2EncryptedData.startsWith("04")) {
            //BouncyCastle(BC库)解密时需要密文的第一位为0x04，而一些非BC库加密出的密文第一位可能不是0x04，所以需要判断是否需要补上
            sm2EncryptedData = "04".concat(sm2EncryptedData);
        }

        //解密16进制密文
        return SmUtil.sm2(privateKey, null).decryptStr(sm2EncryptedData, KeyType.PrivateKey);
    }

    /**
     * SM2签名
     *
     * @param data
     *         待签名字符串
     * @param privateKey
     *         SM2私钥
     * @return 16进制签名串
     */
    public static String sm2Sign(String data, String privateKey) {
        checkSrcData(data, 2);
        sm2CheckKey(privateKey, KeyType.PrivateKey);
        return SmUtil.sm2(privateKey, null).signHexFromHex(HexUtil.encodeHexStr(data));
    }

    /**
     * SM2验签
     *
     * @param data
     *         待签名字符串
     * @param publicKey
     *         SM2公钥
     * @param sign
     *         16进制签名串
     * @return
     */
    public static boolean sm2VerifySign(String data, String publicKey, String sign) {
        checkSrcData(data, 2);
        publicKey = sm2CheckKey(publicKey, KeyType.PublicKey);
        if (CheckUtils.isEmpty(sign)) {
            throw new RuntimeException("SM2签名不能为空");
        }

        //verifyHex方法第一个参数的注释有歧义，实际是需要传入待签名的源数据
        return SmUtil.sm2(null, publicKey).verifyHex(HexUtil.encodeHexStr(data), sign);
    }

    /**
     * 生成SM3摘要字符串
     *
     * @param data
     *         源字符串
     * @return
     */
    public static String sm3(String data) {
        return SmUtil.sm3(data);
    }

    /**
     * 验证SM3摘要字符串是否匹配
     *
     * @param data
     *         待验证源字符串
     * @param sm3DigestString
     *         SM3摘要字符串
     * @return
     */
    public static boolean sm3Verify(String data, String sm3DigestString) {
        if (CheckUtils.isEmpty(sm3DigestString)) {
            return false;
        }
        return sm3DigestString.equalsIgnoreCase(sm3(data));
    }

    /**
     * 生成SM3摘要字符串
     *
     * @param data
     *         源字符串
     * @param salt
     *         加密盐(随机串)
     * @return
     */
    public static String sm3WithSalt(String data, String salt) {
        if (CheckUtils.isEmpty(salt)) {
            return sm3(data);
        } else {
            return SmUtil.sm3WithSalt(strToBytes(salt)).digestHex(data);
        }
    }

    /**
     * 验证SM3摘要字符串是否匹配
     *
     * @param data
     *         待验证源字符串
     * @param salt
     *         加密盐(随机串)
     * @param sm3DigestString
     *         SM3摘要字符串
     * @return
     */
    public static boolean sm3VerifyWithSalt(String data, String salt, String sm3DigestString) {
        if (CheckUtils.isEmpty(salt)) {
            return sm3Verify(data, sm3DigestString);
        } else {
            if (CheckUtils.isEmpty(sm3DigestString)) {
                return false;
            }
            return sm3DigestString.equalsIgnoreCase(sm3WithSalt(data, salt));
        }
    }

    /**
     * SM4加密(使用GCM模式，带完整性校验，推荐使用)<br>
     * 密文格式：Base64(IV(12字节) + 密文 + tag(16字节))<br>
     * 每次加密都会重新生成IV，所以相同明文的密文每次都不同；IV由“实例随机前缀 + 自增计数器”组成，
     * 多节点部署时也可尽量避免IV重复
     *
     * @param data
     *         源字符串
     * @param key
     *         16个非中文字符组成的密钥
     * @return Base64编码的密文
     */
    public static String sm4Encrypt(String data, String key) {
        checkSrcData(data, 0);
        byte[] keyByte = sm4CheckKey(key);
        byte[] plainBytes = strToBytes(data);
        byte[] iv = nextGcmIv();

        //使用BouncyCastle原生API实现SM4-GCM，tag长度为128bit
        GCMBlockCipher cipher = new GCMBlockCipher(new SM4Engine());
        cipher.init(true, new AEADParameters(new KeyParameter(keyByte), SM4_GCM_TAG_LENGTH_BITS, iv));
        byte[] cipherBytes = new byte[cipher.getOutputSize(plainBytes.length)];
        int len = cipher.processBytes(plainBytes, 0, plainBytes.length, cipherBytes, 0);
        try {
            len += cipher.doFinal(cipherBytes, len);
        } catch (InvalidCipherTextException e) {
            //加密过程正常不会出现该异常，兜底处理
            throw new RuntimeException("SM4加密失败", e);
        }

        //密文 = IV + 密文 + tag
        byte[] result = new byte[iv.length + len];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(cipherBytes, 0, result, iv.length, len);
        return Base64.encode(result);
    }

    /**
     * SM4解密(GCM模式)<br>
     * 密文格式：Base64(IV(12字节) + 密文 + tag(16字节))
     *
     * @param sm4EncryptedData
     *         Base64编码的密文
     * @param key
     *         16个非中文字符组成的密钥
     * @return 明文
     */
    public static String sm4Decrypt(String sm4EncryptedData, String key) {
        checkSrcData(sm4EncryptedData, 1);
        byte[] keyByte = sm4CheckKey(key);
        byte[] allBytes = Base64.decode(sm4EncryptedData);
        int minLength = SM4_GCM_IV_LENGTH + SM4_GCM_TAG_LENGTH_BITS / 8;
        if (allBytes.length <= minLength) {
            throw new RuntimeException("SM4密文长度不足，解密失败");
        }

        byte[] iv = Arrays.copyOfRange(allBytes, 0, SM4_GCM_IV_LENGTH);
        int cipherLength = allBytes.length - SM4_GCM_IV_LENGTH;
        GCMBlockCipher cipher = new GCMBlockCipher(new SM4Engine());
        cipher.init(false, new AEADParameters(new KeyParameter(keyByte), SM4_GCM_TAG_LENGTH_BITS, iv));
        byte[] plainBytes = new byte[cipher.getOutputSize(cipherLength)];
        int len = cipher.processBytes(allBytes, SM4_GCM_IV_LENGTH, cipherLength, plainBytes, 0);
        try {
            len += cipher.doFinal(plainBytes, len);
        } catch (InvalidCipherTextException e) {
            //GCM会在doFinal时校验tag，密文被篡改或密钥错误都会进入该分支
            throw new RuntimeException("SM4解密失败，密文可能已损坏或密钥不正确", e);
        }
        return new String(plainBytes, 0, len, StandardCharsets.UTF_8);
    }

    /**
     * SM4加密(使用CTR模式，无完整性校验，只适用于不需要防篡改的场景)<br>
     * 密文格式：Base64(IV(16字节) + 密文)<br>
     * 每次加密都会重新生成IV，所以相同明文的密文每次都不同；IV由“实例随机前缀 + 自增计数器”组成，
     * 多节点部署时也可尽量避免IV重复
     *
     * @param data
     *         源字符串
     * @param key
     *         16个非中文字符组成的密钥
     * @return Base64编码的密文
     */
    public static String sm4EncryptByCTR(String data, String key) {
        checkSrcData(data, 0);
        return sm4ByCTR(data, sm4CheckKey(key), true);
    }

    /**
     * SM4解密(CTR模式)<br>
     * 密文格式：Base64(IV(16字节) + 密文)
     *
     * @param sm4EncryptedData
     *         Base64编码的密文
     * @param key
     *         16个非中文字符组成的密钥
     * @return 明文
     */
    public static String sm4DecryptByCTR(String sm4EncryptedData, String key) {
        checkSrcData(sm4EncryptedData, 1);
        return sm4ByCTR(sm4EncryptedData, sm4CheckKey(key), false);
    }

    /**
     * CTR模式的加解密处理(CTR加解密逻辑相同，仅init的加密标识不同)
     *
     * @param data
     *         源字符串或Base64编码的密文
     * @param keyByte
     *         密钥字节数组
     * @param encrypt
     *         是否加密
     * @return Base64编码的密文或明文
     */
    private static String sm4ByCTR(String data, byte[] keyByte, boolean encrypt) {
        byte[] srcBytes;
        byte[] iv;
        if (encrypt) {
            iv = nextCtrIv();
            srcBytes = strToBytes(data);
        } else {
            byte[] allBytes = Base64.decode(data);
            if (allBytes.length <= SM4_CTR_IV_LENGTH) {
                throw new RuntimeException("SM4密文长度不足，解密失败");
            }
            iv = Arrays.copyOfRange(allBytes, 0, SM4_CTR_IV_LENGTH);
            srcBytes = Arrays.copyOfRange(allBytes, SM4_CTR_IV_LENGTH, allBytes.length);
        }

        //使用BouncyCastle原生API实现SM4-CTR(CTR为流式加密，输出长度与输入长度一致)
        SICBlockCipher cipher = new SICBlockCipher(new SM4Engine());
        cipher.init(encrypt, new ParametersWithIV(new KeyParameter(keyByte), iv));
        byte[] outBytes = new byte[srcBytes.length];
        int len = cipher.processBytes(srcBytes, 0, srcBytes.length, outBytes, 0);

        if (encrypt) {
            //加密时把IV拼在密文前面
            byte[] result = new byte[iv.length + len];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(outBytes, 0, result, iv.length, len);
            return Base64.encode(result);
        } else {
            return new String(outBytes, 0, len, StandardCharsets.UTF_8);
        }
    }

    /**
     * 生成GCM模式使用的IV<br>
     * IV = 6字节实例随机前缀 + 6字节自增计数器，同一实例内严格不重复，多实例间依靠随机前缀区分
     *
     * @return 12字节IV
     */
    private static byte[] nextGcmIv() {
        return nextIv(SM4_GCM_IV_PREFIX, SM4_GCM_IV_PREFIX_LENGTH, SM4_GCM_IV_LENGTH, SM4_GCM_IV_COUNTER,
                SM4_GCM_COUNTER_MASK);
    }

    /**
     * 生成CTR模式使用的IV<br>
     * IV = 8字节实例随机前缀 + 8字节自增计数器，同一实例内严格不重复，多实例间依靠随机前缀区分
     *
     * @return 16字节IV
     */
    private static byte[] nextCtrIv() {
        return nextIv(SM4_CTR_IV_PREFIX, SM4_CTR_IV_PREFIX_LENGTH, SM4_CTR_IV_LENGTH, SM4_CTR_IV_COUNTER,
                -1L); // CTR 不需要掩码
    }

    /**
     * 生成带前缀的IV<br>
     * IV = 随机前缀 + 自增计数器，同一实例内严格不重复，多实例间依靠随机前缀区分
     *
     * @param prefix
     *         随机前缀（实例级）
     * @param prefixLength
     *         随机前缀长度
     * @param ivLength
     *         IV总长度
     * @param counter
     *         自增计数器（AtomicLong）
     * @param counterMask
     *         计数器掩码（用于截断，如无掩码可传 -1L 表示不截断）
     * @return 指定长度的IV
     */
    private static byte[] nextIv(byte[] prefix, int prefixLength, int ivLength, AtomicLong counter, long counterMask) {
        byte[] iv = new byte[ivLength];
        System.arraycopy(prefix, 0, iv, 0, prefixLength);
        long counterValue = counter.incrementAndGet();
        if (counterMask != -1L) {
            counterValue &= counterMask;
        }
        putLong(iv, prefixLength, ivLength - prefixLength, counterValue);
        return iv;
    }

    /**
     * 按大端方式把long值写入byte数组的指定位置
     *
     * @param target
     *         目标数组
     * @param offset
     *         起始位置
     * @param length
     *         占用字节数
     * @param value
     *         待写入的值
     */
    private static void putLong(byte[] target, int offset, int length, long value) {
        for (int i = length - 1; i >= 0; i--) {
            target[offset + i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
    }

    /**
     * 生成指定长度的安全随机字节数组
     *
     * @param length
     *         长度
     * @return
     */
    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte[] strToBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static void checkSrcData(String data, int type) {
        if (CheckUtils.isEmpty(data)) {
            throw new RuntimeException("待" + (0 == type ? "加密" : (1 == type ? "解密" : "签名")) + "字符串不能为空");
        }
    }

    /**
     * 检查SM2公私钥是否为空
     *
     * @param key
     * @param keyType
     * @return 可能经过处理的公钥串
     */
    private static String sm2CheckKey(String key, KeyType keyType) {
        if (CheckUtils.isEmpty(key)) {
            throw new RuntimeException("SM2" + (keyType.equals(KeyType.PublicKey) ? "公钥" : "私钥") + "不能为空");
        }

        if (keyType.equals(KeyType.PublicKey)) {
            //SM2的未压缩公钥为64位，但是BouncyCastle(BC库)会在前面带一位0x04再进行处理(生成的公钥也是这样)，所以先判断是否需要补充前面一位
            byte[] keyBytes = HexUtil.decodeHex(key);
            int byteNum = keyBytes.length;
            if (byteNum == 64) {
                byte[] newKeyBytes = new byte[byteNum + 1];
                newKeyBytes[0] = 4; //最前面补0x04
                System.arraycopy(keyBytes, 0, newKeyBytes, 1, byteNum); //把源byte[]复制到新的byte[]中
                return HexUtil.encodeHexStr(newKeyBytes);
            }
        }
        return key;
    }

    /**
     * 检查SM4公私钥是否合法，合法则返回其对应的byte数组
     *
     * @param key
     * @return
     */
    private static byte[] sm4CheckKey(String key) {
        if (CheckUtils.isEmpty(key)) {
            throw new RuntimeException("SM4密钥不能为空");
        }
        byte[] keyBytes = strToBytes(key);
        if (keyBytes.length != 16 || StringUtils.containChinese(key)) {
            //1byte=8bit。长度不符合的话会报错SM4 requires a 128 bit key
            throw new RuntimeException("SM4密钥必须为16个非中文字符组成的字符串");
        }
        return keyBytes;
    }
}