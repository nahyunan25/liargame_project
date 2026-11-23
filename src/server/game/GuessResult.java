package server.game;

public enum GuessResult {
	WAITING, //다른 라이어의 답을 기다림
	LIAR_WIN, //한 명이라도 정답
	CITIZEN_WIN //모두 오답
}
