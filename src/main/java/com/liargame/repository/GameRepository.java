package com.liargame.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.liargame.model.Game;

@Repository
public class GameRepository {
	private final Map<Integer, Game> games = new ConcurrentHashMap<>();
	
	public void save(Game game) {
		games.put(game.getRoomId(), game);
	}
	
	public Game findByRoomId(int roomId) {
		return games.get(roomId);
	}
	
	//TODO: 방이 삭제될때 해당 게임의 타이머 중지 후 game 제거
	public void deleteByRoomId(int roomId) {
		games.remove(roomId);
	}
	
}
