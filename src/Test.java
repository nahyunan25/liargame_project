import java.util.ArrayList;
import java.util.List;

import common.Player;
import server.game.ChatManager;
import server.game.GameManager;
import server.game.RoleAssigner;
import server.game.VoteManager;
import server.room.RoomManager;

public class Test {
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RoleAssigner roleAssigner = new RoleAssigner();
		ChatManager chatManager = new ChatManager();
		VoteManager voteManager = new VoteManager();
		RoomManager roomManager = new RoomManager();
		List<Player> players = new ArrayList<>();
		List<Player> liars = new ArrayList<>();
		GameManager gameManager = new GameManager(roomManager, players);
		
		Player player1 = new Player(1, false); 
		Player player2 = new Player(2, false);
		Player player3 = new Player(3, false);
		Player liar1 = new Player(4, false);
		
		gameManager.join(player1);
		gameManager.join(player2);
		gameManager.join(player3);
		gameManager.join(liar1);	
		
		gameManager.handleChat(player1, "빨간색이에요");
		gameManager.handleChat(player2, "아침에 먹으면 좋아요");
		gameManager.handleChat(player3, "껍질째 먹기도 해요");
		gameManager.handleChat(liar1, "달콤해요");
		
		gameManager.handleVote(player1, 4);
		gameManager.handleVote(player2, 4);
		gameManager.handleVote(player3, 4);
		gameManager.handleVote(liar1, 4);
		
		gameManager.handleLiarGuess(liar1, "바나나");
	}

}
