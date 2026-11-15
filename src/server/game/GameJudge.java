package server.game;

import java.util.List;
import common.Role;
import common.Player;

public class GameJudge {
	public boolean isLiar(Player target) {  //지목한 사람이 라이어인지
		if (target.getRole() == Role.LIAR) return true;
		else return false;
	};
	public boolean checkLiarGuess(String answer, String keyword) {  //라이어가 입력한 답이 맞는지
		if (answer.equals(keyword)) return true;
		else return false;
	};
	
	//-----------게임 종료 조건 두가지-----------
	//모든 라이어를 다 찾았을 경우
	public boolean isAllLiarsFound(List<Player> foundLiayers, int liarCount) {
		if (foundLiayers.size() == liarCount) return true;
		else return false;
	};
	
	//최대 라운드까지 진행되었을 때
	public boolean isMaxRoundover(int round, int maxRound) {
		if (round >= maxRound) return true;
		else return false;
	};
}