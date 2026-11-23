package server.network;

import common.Player;
import server.game.GameManager;

public class GameRequestHandler {
	private GameManager gameManager;
	
	public GameRequestHandler(GameManager gameManager) {
		this.gameManager = gameManager;
	}
	
	public void handleRequest(Player player, String request) {
		String[] tokens = request.split(":", 2);
		
		//메세지 case
		switch(tokens[0]) {
			case "CHAT":
				gameManager.handleChat(player, tokens[1]);
				break;
				
			case "VOTE":
				try{
					int targetId = Integer.parseInt(tokens[1]);
					gameManager.handleVote(player, targetId);
				}catch(NumberFormatException e) {
					player.send("의심되는 player의 번호를 입력해주세요.");
				}
				break;
				
			case "GUESS":
				gameManager.handleLiarGuess(player, tokens[1]);
				break;
				
			default:
				System.out.println("알 수 없는 메세지: "+ request);
				break;
		}
	}
}
