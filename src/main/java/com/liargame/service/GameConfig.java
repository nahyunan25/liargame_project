package com.liargame.service;

public class GameConfig {
	// 인원
	public static final int MIN_PLAYERS = 3;
	public static final int MAX_PLAYERS = 10;
	// 제한 시간
	public static final int CHAT_TIME = 30;
	public static final int VOTE_TIME = 30;
	public static final int LIAR_GUESS_TIME = 30; // 라이어가 정답 맞추는 시간

	private GameConfig() {
	}

	// 인원수에 따른 라이어 수 설정
	public static int getLiarCount(int playerCount) {
		if (playerCount <= 6)
			return 1;
		else
			return 2;
	}

	// 인원수에 따른 최대 라운드
	public static int getMaxRound(int playerCount) {
		if (playerCount <= 6)
			return 2;
		else
			return 4;
	}
}
