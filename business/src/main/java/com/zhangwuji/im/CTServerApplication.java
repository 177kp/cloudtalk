package com.zhangwuji.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.zhangwuji.im.websocket.server.IMWebSocketServerConfig;

@SpringBootApplication
@EnableCaching
@Configuration
@ServletComponentScan("com.zhangwuji.im")
@MapperScan("com.zhangwuji.im")
public class CTServerApplication {

    @SuppressWarnings("unused")
	public static void main(String[] args) {
    	ConfigurableApplicationContext context =  SpringApplication.run(CTServerApplication.class, args);
    }

}
