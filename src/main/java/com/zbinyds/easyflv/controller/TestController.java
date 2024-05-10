package com.zbinyds.easyflv.controller;

import com.zbinyds.easyflv.service.helper.FlvHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author zbinyds
 * @Create 2024-05-08 14:41
 */

@CrossOrigin
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {
    private final FlvHelper flvHelper;

    @GetMapping("/stream_{channel}.flv")
    public void flv(@PathVariable String channel, @RequestParam String url,
                    HttpServletRequest request, HttpServletResponse response) {
        // 示例 url = rtsp://192.168.1.1:554/Streaming/Channels/，channel = 1
        url = url + channel;
        flvHelper.open(url, request, response);
    }
}
