package server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.Player;
import common.Role;

public class RoleAssigner {
	private Random random = new Random(); //무작위 선택용
	
/*	
	//========================진짜 코드=========================
	//라이어 무작위 선정
	public List<Player> selectLiars(List<Player> players) {
		List<Player> liars = new ArrayList<>();
		int liarCount = GameConfig.getLiarCount(players.size());
		
		while (liars.size() < liarCount) {  // 리스트에서 사람 랜덤으로 뽑기
			int idx = random.nextInt(players.size());
			Player p = players.get(idx);
			
			if (!liars.contains(p)) {  // 처음 뽑힌 사람이면 라이어 리스트에 추가
				liars.add(p);
			}
		}
		return liars;
	};
*/
	
	//======================시나리오용(라이어 지정)==============================
	public List<Player> selectLiars(List<Player> players) {
		List<Player> liars = new ArrayList<>();
		int liarCount = 1;
		
		while (liars.size() < liarCount) {  // 리스트에서 사람 랜덤으로 뽑기
			int idx = random.nextInt(players.size());
			Player p = players.get(idx);
			
			for (Player pl : players) {
				if (pl.getId() == 4) {  // 4번이면 라이어 리스트에 추가
					liars.add(pl);
				}
			}

		}
		return liars;
	};
	//================여기까지 시나리오 코드===================
	
	
	//각 플레이어에게 역할 표시(라이어/시민)
	public void assignRoles(List<Player> players, List<Player> liars) {
		for (Player p : players) {
			if (liars.contains(p)) p.setRole(Role.LIAR);
			else p.setRole(Role.CITIZEN);
		}
	};
	
	//카테고리, 키워드 무작위 선정
	//다른 클래스로 옮겨도 좋을듯(기능분리)
	public String pickCategory() {return "과일";};
	public String pickKeyword(String category) {return "사과";};
	
}
