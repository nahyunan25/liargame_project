package server.game;

import java.util.List;
import common.Player;

public class GameMessenger {
	//==============소켓 서버 메시지==================
		public void broadcast(List<Player> players, String msg) { //참가자 모두에게
			if(!msg.startsWith("PHASE:")) {
				System.out.println(msg);
			}
			for (Player p : players) {
		        if (p.isConnected()) p.send(msg);
		    }
		}

		public void sendTo(Player player, String msg) { //한 사람한테만
			player.send(msg);
		}	
		

}
