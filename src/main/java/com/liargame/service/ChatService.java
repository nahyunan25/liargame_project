package com.liargame.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.liargame.model.Game;
import com.liargame.model.Player;
import com.liargame.model.Role;



@Service
public class ChatService {

	// 채팅 시작
	public void startChat(Game game, List<Player> alivePlayers) {
		game.setTurnOrder(alivePlayers);
		game.setCurrentIndex(0);
		//순서, timer는 GameService에서 연결
	}
	
	public Player getTurnPlayer(Game game) {
		return game.getTurnOrder().get(game.getCurrentIndex());
	}
	
	public String handleChat(Game game, Player player, String text) {
		Player turnPlayer = getTurnPlayer(game);
		
		if(player.getId() != turnPlayer.getId()) {
			return "본인의 차례에 입력해주세요";
		}
		
		String playerKeyword = null;
		
		if(player.getRole() == Role.CITIZEN) {
			playerKeyword = game.getKeyword();
		}else {
			playerKeyword = game.getFoolKeyword();
		}
		
		if(playerKeyword != null && text.contains(playerKeyword)) {
			return "제시어는 입력할 수 없습니다. 다시 입력해주세요";
		}
		
		return null;
	}
	
	public boolean nextTurn(Game game) {
		int nextIndex =	 game.getCurrentIndex() +1;
		game.setCurrentIndex(nextIndex);
		return nextIndex >= game.getTurnOrder().size();
	}
	
	public boolean timeOut(Game game) {
		return nextTurn(game);
	}
}
