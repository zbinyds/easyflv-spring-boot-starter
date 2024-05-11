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

    void open(String rtspUrl, HttpServletRequest request, HttpServletResponse response);

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
