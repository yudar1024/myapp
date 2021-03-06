package com.dynamic.discover.demo.config.eureka;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("eureka")
@Configuration
@EnableDiscoveryClient
public class EurekaConfiguration {
}
