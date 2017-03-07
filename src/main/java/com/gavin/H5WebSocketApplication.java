package com.gavin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class H5WebSocketApplication {

	public static void main(String[] args) {
		// 启动一个生产者线程，模拟任务的产生
		SpringApplication.run(H5WebSocketApplication.class, args);
	}
}
