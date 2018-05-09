package com.citic.helper;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AES加解密算法 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符 此处使用AES-128-CBC加密模式，key需要为16位.
 *
 * @author zhangfeng
 */
public class AesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AesUtil.class);
    private static final String SKEY = "tospurexmindcomp";
    private static final int SKEY_MUST_LENGTH = 16;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        System.out.println(AesUtil.encForTd("123456"));
        System.out.println(AesUtil.encForTd("root"));
        System.out.println(AesUtil.encForTd("hdfs"));
        System.out.println(AesUtil.encForTd("hive"));
        System.out.println(AesUtil.decForTd("Co9ME/Agq664+ZimAcnckA=="));
    }

    /**
     * 平台统一的加密方法.
     */
    public static String encForTd(String ssrc) {
        return encrypt(ssrc, SKEY);
    }

    /**
     * 平台统一的解密方法.
     */
    public static String decForTd(String ssrc) {
        return decrypt(ssrc, SKEY);
    }

    /**
     * 加密，key与iv设为一致.
     */
    private static String encrypt(String ssrc, String skey) {
        if (ssrc == null || skey == null) {
            return null;
        }

        // 判断Key是否为16位    
        if (skey.length() != SKEY_MUST_LENGTH) {
            return null;
        }

        byte[] raw = skey.getBytes(UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

        try {
            //"算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            //使用CBC模式，需要一个向量iv，可增加加密算法的强度
            IvParameterSpec iv = new IvParameterSpec(skey.getBytes(UTF_8));

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(ssrc.getBytes(UTF_8));

            //此处使用BAES64做转码功能，同时能起到2次加密的作用。
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 解密,key与iv设为一致.
     */
    public static String decrypt(String ssrc, String skey) {
        // 判断Key是否正确    
        if (ssrc == null || skey == null) {
            return null;
        }

        // 判断Key是否为16位    
        if (skey.length() != SKEY_MUST_LENGTH) {
            return null;
        }

        try {
            byte[] raw = skey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(skey.getBytes(UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            //先用bAES64解密
            byte[] encrypted1 = Base64.decodeBase64(ssrc);
            byte[] original = cipher.doFinal(encrypted1);

            return new String(original, UTF_8);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 生成随机密码.
     */
    public static String generateRandomCharAndNumber(Integer len) {
        StringBuilder sb = new StringBuilder();

        for (Integer i = 0; i < len; i++) {
            int intRand = new Random().nextInt(52);
            int numValue = new Random().nextInt(10);
            char base = (intRand < 26) ? 'A' : 'a';
            char c = (char) (base + intRand % 26);
            if (numValue % 2 == 0) {
                sb.append(c);
            } else {
                sb.append(numValue);
            }
        }

        return sb.toString().toLowerCase();
    }

    /**
     * 转换日期方法.
     */
    public static String tranDate(String format) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());

        // 将处理后的时间转换为指定的日期格式返回
        return new SimpleDateFormat(format).format(calendar.getTime());
    }
}