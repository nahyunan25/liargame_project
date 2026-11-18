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
		startTurn(); //ChatManager 내부 진행
	}
	
	//현재 차례 시작
	public void startTurn() {
		Player player = turnOrder.get(currentIndex);
		gameManager.startChatTimer();;
		gameManager.broadcast("Player" + player.getId()+ "의 차례입니다.(30초)");
	}
	
	//채팅 출력
	public void handleChat(Player player, String text) {
		Player turnPlayer = turnOrder.get(currentIndex);
		
		//현재 차례가 아닌 사람
		if(player.getId() != turnPlayer.getId()) {
			player.send("본인의 차례에 입력해주세요.");
			return;
		}
		
		gameManager.stopChatTimer();
		
		//현재 차례인 사람
		gameManager.broadcast("Player"+ player.getId()+ ": "+ text);
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
	public void timeOut() {
		nextTurn();
	};

}
