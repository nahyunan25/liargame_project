package com.liargame.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.liargame.dto.KeywordDto;
import com.liargame.model.Game;
import com.liargame.model.GameMode;
import com.liargame.model.GamePhase;
import com.liargame.model.Player;
import com.liargame.model.PlayerStatus;
import com.liargame.model.Role;
import com.liargame.repository.GameRepository;

@Service
public class GameService {

	private GameRepository gameRepository;
	private RoleAssigner roleAssigner;
	private ChatService chatService;
	private PickRandom pickRandom;

	public GameService(GameRepository gameRepository, RoleAssigner roleAssigner, ChatService chatService,
			PickRandom pickRandom) {
		this.gameRepository = gameRepository;
		this.roleAssigner = roleAssigner;
		this.chatService = chatService;
		this.pickRandom = pickRandom;
	}

	public void setGame(Game game) throws SQLException {
		game.setMaxRound(GameConfig.getMaxRound(game.getPlayers().size()));
		game.setLiars(roleAssigner.assign(game.getPlayers()));

		KeywordDto selected = pickRandom.pick(game.getDifficulty().name());
		game.setCategory(selected.getCategory());
		game.setKeyword(selected.getKeyword());

		game.setFoolKeyword(null);
		if (game.getGameMode() == GameMode.FOOL) {
			game.setFoolKeyword(pickRandom.pickOtherKeyword(selected));
		}
	}

	private List<Player> getAlivePlayers(Game game) {
		List<Player> alive = new ArrayList<>();
		for (Player p : game.getPlayers()) {
			if (p.getStatus() == PlayerStatus.ALIVE)
				alive.add(p);
		}
		return alive;
	}

	private boolean isLiarWinByCount(Game game) {
		int liarCount = 0;
		for (int i = 0; i < game.getLiars().size(); i++) {
			Player liar = game.getLiars().get(i);
			System.out.println(liar.getId());

		}
		int citizenCount = getAlivePlayers(game).size() - liarCount;
		return liarCount > 0 && citizenCount == liarCount;
	}
	
	public boolean isMaxRoundover(int round, int maxRound) {
		if (round >= maxRound)
			return true;
		else
			return false;
	}
	
	private void changePhase(Game game, GamePhase nextPhase) {
		game.setPhase(nextPhase);	
	}
	
	private boolean isSpectator(Player player) {
		if(player.getStatus() == PlayerStatus.DIED) {
			return true;
		}
		return false;
	}
	
	private void nextRound(Game game) {
		if(isLiarWinByCount(game)) {
			finishGame(game, Role.LIAR);
			return;
		}
		
		if(isMaxRoundover(game.getRound(), game.getMaxRound())){
			finishGame(game, Role.LIAR);
			return;
		}
		
		game.setRound(game.getRound() + 1);
		changePhase(game, GamePhase.CHATTING);
		chatService.startChat(game, getAlivePlayers(game));
		startTurn(game);
	}
	
	private void finishGame(Game game, Role winner) {
		//TODO: 해당 게임의 타이머 중지
		changePhase(game, GamePhase.RESULT);
		
		String liarIds = "";
		for(int i = 0; i<game.getLiars().size(); i++) {
			Player liar = game.getLiars().get(i);
			liarIds += "Player" + liar.getId() + " ";
		}
		
		String winnerMessage;
		
		if(winner == Role.LIAR) {
			winnerMessage = "라이어 승리";
		} else {
			winnerMessage = "시민 승리";
		}
		
		String resultMessage = winnerMessage + " / 라이어: " + liarIds + "/제시어: " + game.getKeyword();
		//TODO: 해당 방에 "==== 게임 종료 ===="와 resultMessage 전송
	
		//TODO: Roomservice 연결 후 방을 대기 상태로 변경
		
		for(int i = 0; i<game.getPlayers().size(); i++) {
			Player player = game.getPlayers().get(i);
			player.setChoosingRemain(true);
			
			//TODO: 해당 플레이어에게 다시하기, 종료하기 선택 안내
		}
	}
	
	public void startTurn(Game game) {
		Player player = chatService.getTurnPlayer(game);
		
		//TODO:해당 게임의 챗타이머 시작
		
		String turnMessage = "Player" + player.getId() + "의 차례입니다.(" + GameConfig.CHAT_TIME + "초)";
		
		//TODO: 해당 방에 turnMessage 전송
	}
}
