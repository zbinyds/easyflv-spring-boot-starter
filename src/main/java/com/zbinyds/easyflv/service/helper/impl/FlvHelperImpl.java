package com.zbinyds.easyflv.service.helper.impl;

import com.zbinyds.easyflv.service.Converter;
import com.zbinyds.easyflv.service.ConverterContext;
import com.zbinyds.easyflv.service.helper.FlvHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * flv转换器helper，提供便捷的方法进行转换
 *
 * @Author zbinyds
 * @Create 2024-05-08 14:43
 */

@Slf4j
@Service
public class FlvHelperImpl implements FlvHelper, DisposableBean {
    private static final String CHECK_MATCH = "^(rtsp|rtmp)://.*";

    @Override
    public void destroy() {
        log.debug("正在销毁Converter实例");
        ConverterContext.dumpAll().forEach((key, converter) -> converter.exit());
    }

    @Override
    public void open(String url, HttpServletRequest request, HttpServletResponse response) {
        String key = md5(url);
        if (null == key || !isValid(url)) {
            log.error("url不合法");
            return;
        }
        AsyncContext context = request.startAsync();
        context.setTimeout(0);

        Converter converter = ConverterContext.getConverter(key);
        if (null != converter) {
            // 此url已存在，被转换中，添加一个entity输出即可
            try {
                converter.addOutputStreamEntity(key, context);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new IllegalArgumentException(e.getMessage());
            }
        } else {
            // 此url不存在，需要新建一个转换器
            converter = ConverterContext.generateAndRunning(url, key, context);
            ConverterContext.register(key, converter);
        }
        // 设置响应头信息
        response.setContentType("video/x-flv");
        response.setHeader("Connection", "keep-alive");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            // 刷新buffer强制写入客户端
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static boolean isValid(String url) {
        return url.matches(CHECK_MATCH);
    }
}
