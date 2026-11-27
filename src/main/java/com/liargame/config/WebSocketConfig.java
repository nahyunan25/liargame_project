package com.liargame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.liargame.controller.GameController;

// 웹소켓 주소 등록: 브라우저는 ws://서버주소:8080/ws 로 접속
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final GameController gameController;

	public WebSocketConfig(GameController gameController) {
		this.gameController = gameController;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(gameController, "/ws")
				.setAllowedOriginPatterns("*");   // 개발 중에는 모든 주소에서 접속 허용
	}
}