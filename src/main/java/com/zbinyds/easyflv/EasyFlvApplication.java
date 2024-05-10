package com.zbinyds.easyflv;

import com.zbinyds.easyflv.service.ConverterContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author zbinyds
 * @Create 2024-05-07 10:23
 */

@SpringBootApplication
public class EasyFlvApplication implements DisposableBean {
    public static void main(String[] args) {
        SpringApplication.run(EasyFlvApplication.class, args);
    }

    @Override
    public void destroy() {
        ConverterContext.dumpAll().forEach((key, converter) -> converter.exit());
    }
}
