package com.liargame.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import com.liargame.model.Player;

// 연결(세션) 관리: 세션 ↔ 플레이어, 방 번호 ↔ 플레이어 목록
// 서버에 하나만 있음(@Component) → 여러 스레드가 동시에 쓰므로 ConcurrentHashMap 사용
@Component
public class SessionRegistry {

	private final AtomicInteger nextPlayerId = new AtomicInteger(1);

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();  // 세션ID → 세션
	private final Map<String, Player> players = new ConcurrentHashMap<>();             // 세션ID → 플레이어
	private final Map<Integer, String> sessionIdOfPlayer = new ConcurrentHashMap<>();  // 플레이어ID → 세션ID
	private final Map<Integer, Set<Integer>> rooms = new ConcurrentHashMap<>();        // 방 번호 → 플레이어ID 목록

	// 새 연결 등록 → 플레이어 생성
	public Player register(WebSocketSession session) {
		// 타이머 스레드와 요청 스레드가 동시에 보내도 안전하도록 감싸기
		WebSocketSession safe = new ConcurrentWebSocketSessionDecorator(session, 5000, 64 * 1024);

		Player player = new Player(nextPlayerId.getAndIncrement(), false);
		sessions.put(session.getId(), safe);
		players.put(session.getId(), player);
		sessionIdOfPlayer.put(player.getId(), session.getId());
		return player;
	}

	// 연결 종료 → 모든 목록에서 제거, 나간 플레이어 반환 (방·게임 정리에 사용)
	public Player remove(WebSocketSession session) {
		sessions.remove(session.getId());
		Player player = players.remove(session.getId());
		if (player != null) {
			sessionIdOfPlayer.remove(player.getId());
			for (Set<Integer> members : rooms.values()) {
				members.remove(player.getId());
			}
		}
		return player;
	}

	//===== 방 =====

	// 방에 들어감 (B의 RoomService가 입장 성공 후 호출)
	public void joinRoom(int roomId, Player player) {
		rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(player.getId());
	}

	// 방에서 나감
	public void leaveRoom(int roomId, Player player) {
		Set<Integer> members = rooms.get(roomId);
		if (members == null) return;
		members.remove(player.getId());
		if (members.isEmpty()) rooms.remove(roomId);
	}

	//===== 조회 =====

	public Player getPlayer(WebSocketSession session) {
		return players.get(session.getId());
	}

	// 이 플레이어에게 보낼 세션 (연결이 끊겼으면 null)
	public WebSocketSession getSession(Player player) {
		String sessionId = sessionIdOfPlayer.get(player.getId());
		return sessionId == null ? null : sessions.get(sessionId);
	}

	// 방에 있는 플레이어들의 세션
	public List<WebSocketSession> getRoomSessions(int roomId) {
		List<WebSocketSession> result = new ArrayList<>();
		Set<Integer> members = rooms.get(roomId);
		if (members == null) return result;
		for (Integer playerId : members) {
			String sessionId = sessionIdOfPlayer.get(playerId);
			WebSocketSession s = (sessionId == null) ? null : sessions.get(sessionId);
			if (s != null) result.add(s);
		}
		return result;
	}

	// 연결된 모든 세션 (방 목록 갱신 등 전체 알림용)
	public List<WebSocketSession> getAllSessions() {
		return new ArrayList<>(sessions.values());
	}
}
