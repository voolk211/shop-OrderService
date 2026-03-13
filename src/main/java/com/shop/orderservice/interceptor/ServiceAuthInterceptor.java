package com.shop.orderservice.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

public class ServiceAuthInterceptor implements RequestInterceptor {

    @Value("${internal.service-secret}")
    private String serviceSecret;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Service-Auth", serviceSecret);
    }
}
