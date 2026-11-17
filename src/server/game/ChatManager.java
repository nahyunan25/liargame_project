package server.game;

import java.util.List;
import common.Player;
import server.room.Room;

public class ChatManager {
	private List<Player> turnOrder; //이번 라운드 발언 순서자
	private int currentIndex; //현재 차례 위치
	private GameManager gameManager; //채팅 단계 끝나면 알릴 대상
	/* 지금 필요 없음!
	private Room room; //채팅 전송용
	private TurnTimer turnTimer; 

	
	 private GameRoom gameRoom; //채팅 전송용
	 private TurnTimer turnTimer; 
	 */

	
	//GameManager 연결
	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}
	
	//채팅 시작
	public void startChat(List<Player> alivePlayers) { //기본 세팅
		turnOrder = alivePlayers;
		currentIndex = 0;
		gameManager.sendAll("제시어에 대해서 설명하세요.");
		startTurn(); //ChatManager 내부 진행
	}
	
	//현재 차례 시작
	public void startTurn() {
		Player player = turnOrder.get(currentIndex);
		gameManager.sendAll("Player" + player.getId() + "의 차례입니다.");
	}
	
	//채팅 출력
	public void handleChat(Player player, String text) {
		nextTurn();
	}
	
	//다음 차례
	public void nextTurn(){
		currentIndex++;
		
		if(currentIndex == turnOrder.size()) {
			gameManager.chatEnd();
		}
		else {
			startTurn();
		}
	}
	
	//시간
	public void timeOut() {};

}
