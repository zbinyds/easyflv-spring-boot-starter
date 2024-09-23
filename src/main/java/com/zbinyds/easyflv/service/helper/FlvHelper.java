package com.zbinyds.easyflv.service.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * flv-helper。提供了对于flv格式转换的便捷支持
 *
 * @Author zbinyds
 * @Create 2024-05-08 14:42
 */
public interface FlvHelper {

    /**
     * 开启flv转换
     *
     * @param url      rtsp/rtmp流地址
     * @param request  请求体
     * @param response 响应体
     */
    void open(String url, HttpServletRequest request, HttpServletResponse response);

    /**
     * md5加密
     *
     * @param plainText 明文
     * @return 加密字符串
     */
    default String md5(String plainText) {
        StringBuilder buf;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] b = md.digest();
            int i;
            buf = new StringBuilder();
            for (byte value : b) {
                i = value;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return buf.toString();
    }
}
