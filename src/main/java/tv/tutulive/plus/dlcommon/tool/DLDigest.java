package tv.tutulive.plus.dlcommon.tool;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by wangyu on 16/8/28.
 */
public class DLDigest {
    /**
     * compute the sha1 of input string
     * @param input
     * @return
     * sha1 string, or null
     */
    public static String sha1(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input.getBytes());
            byte[] res = md.digest();
            return byte2hex(res);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * compute the the sha1 of input data
     * @param input
     * @return
     * sha1 string, or null
     */
    public static String sha1(byte[] input){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input);
            byte[] res = md.digest();
            return byte2hex(res);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * compute the md5 of input string
     * @param input
     * @return
     * md5 string, or null
     */
    public static String md5(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] res = md.digest();
            return byte2hex(res);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * compute the the md5 of input data
     * @param input
     * @return
     * md5 string, or null
     */
    public static String md5(byte[] input){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input);
            byte[] res = md.digest();
            return byte2hex(res);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * compute the the md5 of input file
     * @param file file
     * @return
     * md5 string, or null
     */
    public static String md5(File file){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 100];
            int numRead = 0;

            while ((numRead = fileStream.read(buffer)) > 0) {
                md.update(buffer, 0, numRead);
            }

            fileStream.close();
            byte[] res = md.digest();
            return byte2hex(res);
        } catch(Exception e){
            return null;
        }
    }

    /**
     * convert byte array @data to hex string
     * @param data: byte array data
     * @return: hex string of byte array @data
     */
    public static String byte2hex(byte[] data){
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<data.length; i++){
            int high = (data[i]>>4)&0x0F;
            int low = data[i]&0x0F;

            sb.append(Integer.toHexString(high));
            sb.append(Integer.toHexString(low));
        }

        return sb.toString();
    }

    /**
     * 对文本进行摘要
     *
     * @param input     要进行摘要的文本
     * @param algorithm 摘要算法 两种值 "MD5"(默认) "SHA-1"
     * @param isSalt    是否加盐
     * @return 摘要后的文本
     */
    public static String digest(@NonNull String input, @Nullable Algorithm algorithm, boolean isSalt) {
        if (!isSalt) {
            return digest(input, algorithm);
        } else {
            Random rand = new Random();
            int count = 16;
            if (algorithm != null) {
                switch (algorithm) {
                    case MD5:
                        count = 16;
                        break;
                    case SHA1:
                        count = 8;
                        break;
                    default:
                        break;
                }
            }
            /*生成盐值*/
            StringBuilder sb = new StringBuilder(count);
            if (count == 8) {
                sb.append(rand.nextInt(9999)).append(rand.nextInt(9999));
            } else {
                sb.append(rand.nextInt(99999999)).append(rand.nextInt(99999999));
            }
            int len = sb.length();
            if (len < count) {
                for (int i = 0; i < count - len; i++) {
                    sb.append("0");
                }
            }
            String salt = sb.toString();
            String md = digest(input + salt, algorithm);
            if (md != null) {
                char[] cs = new char[48];
                int step = 48 / count;
                for (int i = 0; i < 48; i += step) {
                    if (step == 3) {
                        cs[i] = md.charAt(i / 3 * 2);
                        cs[i + 1] = salt.charAt(i / 3);
                        cs[i + 2] = md.charAt(i / 3 * 2 + 1);
                    } else {
                        cs[i] = md.charAt(i / 6 * 5);
                        cs[i + 1] = md.charAt(i / 6 * 5 + 1);
                        cs[i + 2] = md.charAt(i / 6 * 5 + 2);
                        cs[i + 3] = salt.charAt(i / 6);
                        cs[i + 4] = md.charAt(i / 6 * 5 + 3);
                        cs[i + 5] = md.charAt(i / 6 * 5 + 4);
                    }
                }
                return new String(cs);
            } else {
                return null;
            }
        }

    }

    /**
     * 验证文本与已经存储的摘要密文是否一致
     *
     * @param input        带验证的文本
     * @param digestCipher 摘要密文
     * @param algorithm    摘要算法 两种值 "MD5"(默认) "SHA-1"
     * @param isSalt       摘要算法是否加盐
     * @return 是否一致
     */
    public static boolean digestVerify(@NonNull String input, @NonNull String digestCipher, @Nullable Algorithm algorithm, boolean isSalt) {
        if (isSalt) {
            if (digestCipher.length() != 48) {
                return false;
            }
            int i1 = 32;
            int i2 = 16;
            if (algorithm != null) {
                switch (algorithm) {
                    case MD5:
                        i1 = 32;
                        i2 = 16;
                        break;
                    case SHA1:
                        i1 = 40;
                        i2 = 8;
                        break;
                    default:
                        break;
                }
            }
            // 声明字符数组记录去除盐值后的密文
            char[] cs1 = new char[i1];
            // 声明字符数组记录盐值
            char[] cs2 = new char[i2];
            int step = 48 / i2;
            for (int i = 0; i < 48; i += step) {
                if (step == 3) {
                    cs1[i / 3 * 2] = digestCipher.charAt(i);
                    cs1[i / 3 * 2 + 1] = digestCipher.charAt(i + 2);
                    cs2[i / 3] = digestCipher.charAt(i + 1);
                } else {
                    cs1[i / 6 * 5] = digestCipher.charAt(i);
                    cs1[i / 6 * 5 + 1] = digestCipher.charAt(i + 1);
                    cs1[i / 6 * 5 + 2] = digestCipher.charAt(i + 2);
                    cs2[i / 6] = digestCipher.charAt(i + 3);
                    cs1[i / 6 * 5 + 3] = digestCipher.charAt(i + 4);
                    cs1[i / 6 * 5 + 4] = digestCipher.charAt(i + 5);
                }
            }
            String salt = new String(cs2);
            String s = digest(input + salt, algorithm);
            return s != null && s.equals(new String(cs1));
        } else {
            String s = digest(input, algorithm);
            return s != null && s.equals(digestCipher);
        }
    }

    /**
     * 对文本进行基本摘要
     *
     * @param input     要进行摘要的文本
     * @param algorithm 摘要算法 两种值 "MD5"(默认) "SHA-1"
     * @return 摘要后的密文
     */
    private static String digest(@NonNull String input, @Nullable Algorithm algorithm) {
        String name = "MD5";
        if (algorithm != null) {
            switch (algorithm) {
                case SHA1:
                    name = "SHA-1";
                    break;
                case MD5:
                    name = "MD5";
                default:
                    break;
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance(name);
            byte[] mdBytes = md.digest(input.getBytes());
            return bytes2hex(mdBytes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将byte数组转换为16进制字符串
     */
    private static String bytes2hex(@NonNull byte[] md5Bytes) {
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            String s = Integer.toHexString(md5Byte & 0xFF);
            if (s.length() == 1) {
                hexValue.append("0");
            }
            hexValue.append(s);
        }
        return hexValue.toString();
    }

    public enum Algorithm {
        MD5, SHA1
    }
}
