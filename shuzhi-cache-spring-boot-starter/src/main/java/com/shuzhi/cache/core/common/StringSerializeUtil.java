package com.shuzhi.cache.core.common;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by wangxingzhe on 16/3/23.
 */
public class StringSerializeUtil {


    /**
     * 把gzip后的hexstring 解压为json数据。
     * @param gzipString
     * @return json
     */
    public static String deserializeJSONFromGzipString(String gzipString) {
        String md5Str=gzipString.substring(0,gzipString.indexOf(","));
        String dataStr = gzipString.substring(gzipString.indexOf(",")+1);
        String checkMd5 = string2MD5(dataStr);
        if(StringUtils.equals(md5Str,checkMd5)) {
            String ret;
            try {
                byte[] redoData = hexStringToBytes(dataStr);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redoData);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
                BufferedReader gzipBufferedInputStream = new BufferedReader(new InputStreamReader(gzipInputStream,"UTF-8"));
                ret = gzipBufferedInputStream.readLine();
                gzipBufferedInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("read gzip data error!",e);
            }
            return ret;
        }else{
            throw new RuntimeException("md5 check failed! md5 by string:"+md5Str+", md5 by data:"+checkMd5);
        }
    }

    /**
     * 将json转换为gzip后的hex字符串
     * @param serialStr
     * @return
     */
    public static String serializeJSONToGzipHexString(String serialStr) {
        try {
            if(StringUtils.isNotBlank(serialStr)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                gzipOutputStream.write(serialStr.getBytes("UTF-8"));
                gzipOutputStream.close();
                byte[] newData=outputStream.toByteArray();
                String hex=bytesToHexString(newData);
                String md5 = string2MD5(hex);
                return md5+","+hex;
            }
        } catch (IOException e) {
            throw new RuntimeException("write gzip data error !",e);
        }
        return null;
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr){
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        return byte2MD5(byteArray);
    }

    /***
     * MD5加码 生成32位md5码
     */
    public static String byte2MD5(byte[] byteArray){
        MessageDigest md5;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }

        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

}
