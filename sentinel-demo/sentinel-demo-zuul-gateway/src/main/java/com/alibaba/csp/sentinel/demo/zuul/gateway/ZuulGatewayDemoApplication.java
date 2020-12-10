package com.alibaba.csp.sentinel.demo.zuul.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * <p>A demo for using Zuul 1.x with Spring Cloud and Sentinel.</p>
 *
 * <p>To integrate with Sentinel dashboard, you can run the demo with the parameters (an example):
 * <code>
 * -Dproject.name=zuul-gateway -Dcsp.sentinel.dashboard.server=localhost:8080
 * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1
 * </code>
 * </p>
 *
 * @author Eric Zhao
 */
@SpringBootApplication
@EnableZuulProxy
public class ZuulGatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayDemoApplication.class, args);
    }
}
