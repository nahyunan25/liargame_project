package com.liargame.dto;

import java.util.HashMap;
import java.util.Map;

// 서버 ↔ 브라우저가 주고받는 공통 메시지 형식 (JSON)
// 예: {"type":"CHAT", "roomId":1, "data":{"text":"빨간색이에요"}}
public class GameMessageDto {

	private String type;                              // 메시지 종류 (CHAT, VOTE, TURN_START ...)
	private Integer roomId;                           // 방 번호 (방과 상관없는 메시지는 null)
	private Map<String, Object> data = new HashMap<>(); // 종류별 내용

	public GameMessageDto() { }   // JSON 변환용 기본 생성자 (지우면 안 됨)

	public GameMessageDto(String type, Integer roomId, Map<String, Object> data) {
		this.type = type;
		this.roomId = roomId;
		if (data != null) this.data = data;
	}

	// 서버에서 보낼 메시지를 간단히 만들기
	public static GameMessageDto of(String type, Integer roomId, Map<String, Object> data) {
		return new GameMessageDto(type, roomId, data);
	}

	// data 안의 값을 문자열/숫자로 꺼내기 (없으면 null)
	public String getString(String key) {
		Object v = data.get(key);
		return v == null ? null : String.valueOf(v);
	}

	public Integer getInt(String key) {
		Object v = data.get(key);
		if (v instanceof Number) return ((Number) v).intValue();
		try {
			return v == null ? null : Integer.parseInt(String.valueOf(v).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public Integer getRoomId() { return roomId; }
	public void setRoomId(Integer roomId) { this.roomId = roomId; }

	public Map<String, Object> getData() { return data; }
	public void setData(Map<String, Object> data) { this.data = (data == null) ? new HashMap<>() : data; }
}