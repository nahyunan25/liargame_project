package server.game;

import java.util.List;
import java.util.Random;

import server.room.Player;

public class RoleAssigner {
	private Random random = new Random(); //무작위 선택용
	
	//라이어 무작위 선정
	public Player selectLiars(List<Player> players) {return null;};
	
	//각 플레이어에게 역할 표시(라이어/시민)
	public void assingRoles(List<Player> players, List<Player> liars) {};
	
	//카테고리, 키워드 무작위 선정
	//다른 클래스로 옮겨도 좋을듯(기능분리)
	public String pickCategory() {return null;}; 
	public String pickKeyword(String catogory) {return null;};
	
}
