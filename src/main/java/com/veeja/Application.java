package com.veeja;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

/**
 * spring boot 启动类
 *
 * @author veeja
 */
@SpringBootApplication
@MapperScan("mapper")
@EnableScheduling //开启对定时任务的支持
@Slf4j
public class Application {

    public static void main(String[] args) {

        String version = System.getProperty("java.version");
        String[] str = version.split("\\.");
        String msg = "jdk版本不得低于1.8，并且请不要使用open jdk; 当前版本： " + version;
        if (str.length >= 2) {
            try {
                if (Integer.parseInt(str[0]) < 1) {
                    log.error(msg);
                }
                if (Integer.parseInt(str[0]) <= 1 && Integer.parseInt(str[1]) < 8) {
                    log.error(msg);
                }
            } catch (Exception e) {
                log.error(msg, e);
            }
            SpringApplication.run(Application.class, args);
        } else {
            log.error(msg);
        }
    }
}
