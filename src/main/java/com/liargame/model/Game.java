package com.liargame.model;

import java.util.ArrayList;
import java.util.List;

public class Game {

	//기존 GameManager가 가지고 있던 게임 데이터
	
	private int roomId;
	//방 설정
	private GameMode gameMode;
	private Difficulty difficulty;
	//참가자와 라이어
	private List<Player> players;
	private List<Player> liars = new ArrayList<>();
	//게임 진행 상태
	private GamePhase phase = GamePhase.WAITING;
	private int round = 0;
	private int maxRound;
	//주제와 제시어
	private String category;
	private String keyword;
	private String foolKeyword;
	//발언 순서
	private List<Player> turnOrder = new ArrayList<>();
	private int currentIndex = 0;
	public Game(int roomId, GameMode gameMode, Difficulty difficulty, List<Player> players) {
		this.roomId = roomId;
		this.gameMode = gameMode;
		this.difficulty = difficulty;
		this.players = new ArrayList<>(players);
		}
	
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	public GameMode getGameMode() {
		return gameMode;
	}
	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}
	public Difficulty getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}
	public List<Player> getPlayers() {
		return players;
	}
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	public List<Player> getLiars() {
		return liars;
	}
	public void setLiars(List<Player> liars) {
		this.liars = liars;
	}
	public GamePhase getPhase() {
		return phase;
	}
	public void setPhase(GamePhase phase) {
		this.phase = phase;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public int getMaxRound() {
		return maxRound;
	}
	public void setMaxRound(int maxRound) {
		this.maxRound = maxRound;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getFoolKeyword() {
		return foolKeyword;
	}
	public void setFoolKeyword(String foolKeyword) {
		this.foolKeyword = foolKeyword;
	}
	public List<Player> getTurnOrder() {
		return turnOrder;
	}
	public void setTurnOrder(List<Player> turnOrder) {
		this.turnOrder = turnOrder;
	}
	public int getCurrentIndex() {
		return currentIndex;
	}
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}
	
	
}
