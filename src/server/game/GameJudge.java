package server.game;

import java.util.List;

import server.room.Player;

public class GameJudge {
	public boolean isLiar(Player target) {return false;}; //지목한 사람이 라이어인지
	public boolean checkLiarGuess(String answer, String keyword) {return false;}; //답이 맞는지
	
	//게임 종료 조건 두가지
	public boolean isAllLiarsFound(List<Player> foundLiayers, int liarCount) {return false;};
	public boolean isMaxRoundover(int round, int maxRound) {return false;};
}
