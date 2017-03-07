package com.gavin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * User: Gavin
 * E-mail: GavinChangCN@163.com
 * Desc:
 * Date: 2017-03-07
 * Time: 16:56
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    protected static final String TAG = "WebMvcConfig";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        // urlPath 是希望 用户 访问的地址栏显示，ViewName 是 html 的目录+文件名
        registry.addViewController("/chat").setViewName("/websocket");
    }
}
