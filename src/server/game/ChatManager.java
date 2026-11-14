package server.game;

import java.util.List;

import common.Player;
import server.room.GameRoom;

public class ChatManager {
	private List<Player> turnOrder; //이번 라운드 발언 순서자
	private int currentIndex; //현재 차례 위치
	private GameManager gameManager; //채팅 단계 끝나면 알릴 대상
	private GameRoom gameRoom; //채팅 전송용
	private TurnTimer turnTimer; 
	
	//GameManager가 호출
	public void startChat(List<Player> alivePlayers) { //기본 세팅
		turnOrder = alivePlayers;
		currentIndex = 0;
		startTurn(); //ChatManager 내부 진행
	}; 
	public void handleChat(Player player, String text) {
		//gameRoom.sendToAll(player+": "+text); //예시코드(직접 채팅을 보내는 부분)
	};
	public void timeOut() {};
	
	//내부 진행
	public void startTurn() {};
	public void nextTurn() {};
	//끝났으면 gameManager.chatEnd()
}
