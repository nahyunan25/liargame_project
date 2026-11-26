package com.liargame.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.liargame.model.Player;
import com.liargame.model.Role;

@Service
public class RoleAssigner {
	private Random random = new Random();
	
	public List<Player> assign(List<Player> players){
		List<Player> liars = selectLiars(players);
		assignRoles(players, liars);
		return liars;
	}
	
	public List<Player> selectLiars(List<Player> players){
		List<Player> liars = new ArrayList<>();
		int liarCount = GameConfig.getLiarCount(players.size());
		
		while(liars.size() < liarCount) {
			int idx = random.nextInt(players.size());
			Player p = players.get(idx);
			
			if(!liars.contains(p)) {
				liars.add(p);
			}
		}
		return liars;
	}
	
	public void assignRoles(List<Player> players, List<Player> liars) {
		for(Player p: players) {
			if(liars.contains(p)) p.setRole(Role.LIAR);
			else p.setRole(Role.CITIZEN);
		}
	}
}
