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
package pjq.commons.utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

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
 *     <li>SM4：对称加密算法，使用场景参考DES/AES</li>
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
        return SmUtil.sm2(privateKey, null).signHex(HexUtil.encodeHexStr(data));
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
     * SM4加密
     *
     * @param data
     *         源字符串
     * @param key
     *         密钥
     * @return Base64编码的密文
     */
    public static String sm4Encrypt(String data, String key) {
        checkSrcData(data, 0);
        byte[] keyByte = sm4CheckKey(key);
        return SmUtil.sm4(keyByte).encryptBase64(data);
    }

    /**
     * SM4解密
     *
     * @param sm4EncryptedData
     *         Base64编码的密文
     * @param key
     *         SM4密钥
     * @return 明文
     */
    public static String sm4Decrypt(String sm4EncryptedData, String key) {
        checkSrcData(sm4EncryptedData, 1);
        byte[] keyByte = sm4CheckKey(key);
        return SmUtil.sm4(keyByte).decryptStr(sm4EncryptedData);
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