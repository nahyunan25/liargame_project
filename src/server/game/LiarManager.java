package server.game;

import java.util.ArrayList;
import java.util.List;

import common.Player;
import common.PlayerStatus;
import common.Role;

public class LiarManager {
	private List<Player> liars;
	private List<Player> foundLiars = new ArrayList<>();
	private String keyword;
	private GameManager gameManager;
	private Player target;
	private String answer;
	
	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}
	
	public void setLiarManager(List<Player> liars, String keyword) {
		this.liars = liars;
		this.keyword = keyword;
		foundLiars.clear();
	}
	
	//=============GameManager가 호출하는 부분=============
	public boolean isLiar(Player target) {  //지목한 사람이 라이어인지
		if (target.getRole() == Role.LIAR) return true;
		else return false;
	};
	
	public boolean reveal(Player target) {
		target.setStatus(PlayerStatus.REVEALED_LIAR);
		foundLiars.add(target);
		return isAllLiarsFound(foundLiars, liars.size());
	}
	
	public Role handleLiarGuess(String answer) {
		if (checkLiarGuess(answer, keyword)) {
			return Role.LIAR;
		}
		else {
			return Role.CITIZEN;
		}
	}
	
	public void announceLiarCount() {
		gameManager.broadcast("이번 게임의 라이어는 " + liars.size() + "명입니다.");
	}
	
	//=============내부 코드===================
	private boolean checkLiarGuess(String answer, String keyword) {  //라이어가 입력한 답이 맞는지
		if (answer.equals(keyword)) return true;
		else return false;
	};
	
	private boolean isAllLiarsFound(List<Player> foundLiayers, int liarCount) {
		if (foundLiayers.size() == liarCount) return true;
		else return false;
	};
}