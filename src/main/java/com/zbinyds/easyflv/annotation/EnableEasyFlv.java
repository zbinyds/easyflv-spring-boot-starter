package com.zbinyds.easyflv.annotation;

import com.zbinyds.easyflv.config.EasyFlvConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启EasyFlv
 *
 * @Author zbinyds
 * @Create 2024-05-09 13:53
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EasyFlvConfiguration.class)
public @interface EnableEasyFlv {
}
