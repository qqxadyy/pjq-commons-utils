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
        return new SM2KeyPair(new KeyPair(sm2.getPublicKey(), sm2.getPrivateKey()), sm2.getPublicKeyBase64(),
                sm2.getPrivateKeyBase64());
    }

    /**
     * SM2加密
     *
     * @param data
     *         待加密字符串
     * @param publicKey
     *         SM2公钥
     * @return
     */
    public static String sm2Encrypt(String data, String publicKey) {
        checkSrcData(data, 0);
        sm2CheckKey(publicKey, KeyType.PublicKey);
        return SmUtil.sm2(null, publicKey).encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * SM2解密
     *
     * @param sm2EncryptedData
     *         SM2加密字符串
     * @param privateKey
     *         SM2私钥
     * @return
     */
    public static String sm2Decrypt(String sm2EncryptedData, String privateKey) {
        checkSrcData(sm2EncryptedData, 1);
        sm2CheckKey(privateKey, KeyType.PrivateKey);
        return SmUtil.sm2(privateKey, null).decryptStr(sm2EncryptedData, KeyType.PrivateKey);
    }

    /**
     * SM2签名
     *
     * @param data
     *         待签名字符串
     * @param privateKey
     *         SM2私钥
     * @return
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
     *         SM2签名
     * @return
     */
    public static boolean sm2VerifySign(String data, String publicKey, String sign) {
        checkSrcData(data, 2);
        sm2CheckKey(publicKey, KeyType.PublicKey);
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
     * @return
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
     *         SM4加密字符串
     * @param key
     *         SM4密钥
     * @return
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
     */
    private static void sm2CheckKey(String key, KeyType keyType) {
        if (CheckUtils.isEmpty(key)) {
            throw new RuntimeException("SM2" + (keyType.equals(KeyType.PublicKey) ? "公钥" : "私钥") + "不能为空");
        }
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
        byte[] keyByte = strToBytes(key);
        if (keyByte.length != 16 || StringUtils.containChinese(key)) {
            //1byte=8bit。长度不符合的话会报错SM4 requires a 128 bit key
            throw new RuntimeException("SM4密钥必须为16个非中文字符组成的字符串");
        }
        return keyByte;
    }
}