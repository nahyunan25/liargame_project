package server.game;

import java.util.List;

import server.room.Player;

public class ChatManager {
	private List<Player> turnOrder; //이번 라운드 발언 순서자
	private int currentIndex; //현재 차례 위치
	
	public void setTurnOrder(List<Player> alivePlayers) {}; //살아있는 플레이어 중에서만 발언순서지정
	public Player getCurrentPlayer() {return null;}; //현재 차례인 플레이어
	public void nextTurn() {};
	public boolean isRoundOver() {return false;}; //모두 한번씩 말했는지 확인
}
