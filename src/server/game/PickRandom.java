package server.game;

import java.util.Random;

//TODO: DB 구현후 랜덤 이용해서 구현 필요
public class PickRandom { 
	private Random random = new Random();
	
	//static선언: GameManager에서 객체 생성 안하고 바로 부름
	public static String pickCategory() {return "과일";};
	public static String pickKeyword(String category) {return "사과";};
}
