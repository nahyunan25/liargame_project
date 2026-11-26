package com.liargame.messaging;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.liargame.dto.GameMessageDto;
import com.liargame.model.Player;

import tools.jackson.databind.json.JsonMapper;

// 메시지 전송 전담: B(방), C(게임)는 이 클래스만 써서 보냄
// 기존 GameManager의 broadcast/sendTo와 같은 역할 (PrintWriter 대신 웹소켓 세션 사용)
@Component
public class GameMessenger {

	private final SessionRegistry sessionRegistry;
	private final JsonMapper jsonMapper;   // 객체 ↔ JSON 변환 (스프링이 만들어 줌)

	public GameMessenger(SessionRegistry sessionRegistry, JsonMapper jsonMapper) {
		this.sessionRegistry = sessionRegistry;
		this.jsonMapper = jsonMapper;
	}

	//===== 한 사람에게 =====
	public void sendTo(Player player, GameMessageDto message) {
		send(sessionRegistry.getSession(player), toJson(message));
	}

	public void sendTo(Player player, String type, Integer roomId, Map<String, Object> data) {
		sendTo(player, GameMessageDto.of(type, roomId, data));
	}

	//===== 방 전체에게 =====
	public void broadcast(int roomId, GameMessageDto message) {
		String json = toJson(message);
		for (WebSocketSession s : sessionRegistry.getRoomSessions(roomId)) {
			send(s, json);
		}
	}

	public void broadcast(int roomId, String type, Map<String, Object> data) {
		broadcast(roomId, GameMessageDto.of(type, roomId, data));
	}

	//===== 접속한 모든 사람에게 (방 목록 갱신 등) =====
	public void broadcastAll(GameMessageDto message) {
		String json = toJson(message);
		for (WebSocketSession s : sessionRegistry.getAllSessions()) {
			send(s, json);
		}
	}

	//===== 서버 콘솔에만 (투표 내역 등 비공개 기록) =====
	public void log(String msg) {
		System.out.println("[서버] " + msg);
	}

	//===== 내부 =====
	private String toJson(GameMessageDto message) {
		return jsonMapper.writeValueAsString(message);
	}

	private void send(WebSocketSession session, String json) {
		if (session == null || !session.isOpen()) return;   // 이미 나간 사람
		try {
			session.sendMessage(new TextMessage(json));
		} catch (IOException e) {
			log("전송 실패: " + e.getMessage());
		}
	}
}