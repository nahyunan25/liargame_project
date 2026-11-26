package com.liargame.controller;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.liargame.dto.GameMessageDto;
import com.liargame.messaging.GameMessenger;
import com.liargame.messaging.SessionRegistry;
import com.liargame.model.Player;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// 웹소켓으로 들어온 메시지를 받아서 type에 맞는 서비스로 넘김
// (기존 ClientHandler + GameRequestHandler 역할)
@Component
public class GameController extends TextWebSocketHandler {

	private final SessionRegistry sessionRegistry;
	private final GameMessenger messenger;
	private final JsonMapper jsonMapper;
	// TODO: B, C의 서비스가 준비되면 생성자에 추가
	// private final RoomService roomService;
	// private final GameService gameService;

	public GameController(SessionRegistry sessionRegistry, GameMessenger messenger, JsonMapper jsonMapper) {
		this.sessionRegistry = sessionRegistry;
		this.messenger = messenger;
		this.jsonMapper = jsonMapper;
	}

	// 브라우저가 접속했을 때
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Player player = sessionRegistry.register(session);
		messenger.log("접속: Player" + player.getId());
		messenger.sendTo(player, "CONNECTED", null, Map.of("playerId", player.getId()));
	}

	// 메시지가 들어왔을 때
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		Player player = sessionRegistry.getPlayer(session);
		if (player == null) return;

		GameMessageDto msg;
		try {
			msg = jsonMapper.readValue(message.getPayload(), GameMessageDto.class);
		} catch (JacksonException e) {
			messenger.sendTo(player, "ERROR", null, Map.of("message", "메시지 형식이 올바르지 않습니다."));
			return;
		}
		if (msg.getType() == null) return;

		messenger.log("Player" + player.getId() + " → " + msg.getType() + " " + msg.getData());

		switch (msg.getType()) {
			// 연결 확인용
			case "PING" -> messenger.sendTo(player, "PONG", null, Map.of());

			// 방 (B: RoomService)
			case "CREATE_ROOM", "JOIN_ROOM", "LEAVE_ROOM", "ROOM_LIST", "ROOM_CHAT", "START_GAME" -> {
				// TODO: roomService로 전달
			}

			// 게임 (C: GameService)
			case "CHAT" -> {
				// TODO: gameService.handleChat(msg.getRoomId(), player, msg.getString("text"));
			}
			case "VOTE" -> {
				// TODO: gameService.handleVote(msg.getRoomId(), player, msg.getInt("targetId"));
			}
			case "GUESS" -> {
				// TODO: gameService.handleLiarGuess(msg.getRoomId(), player, msg.getString("answer"));
			}

			default -> messenger.sendTo(player, "ERROR", null, Map.of("message", "알 수 없는 요청: " + msg.getType()));
		}
	}

	// 브라우저가 나갔을 때 (창 닫기, 새로고침 포함)
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Player player = sessionRegistry.remove(session);
		if (player != null) {
			messenger.log("퇴장: Player" + player.getId());
			// TODO: roomService / gameService에 퇴장 알리기
		}
	}
}