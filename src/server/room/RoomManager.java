package server.room;

import java.util.List;

import common.Player;
import server.game.GameManager;
import server.room.Room;
import server.room.RoomService;

public class RoomManager {
	private RoomService roomService;
	private GameManager gameManager;
	private RoomChatManager roomChatManager = new RoomChatManager();
	
	public RoomManager(RoomService roomService) {
	    this.roomService = roomService;
	}
	

	// ClientHandler와 RoomService 사이를 연결해줌
	public Room createRoom(Player host, String title,
            String gameMode, String difficulty, int maxPlayers,
            boolean isPublic, String password) {

        return roomService.createRoom(host, title, gameMode,
                difficulty, maxPlayers, isPublic, password);
    }
	
	//11.22 임시. 방장, 제목, 최대 인원만
	public Room createRoom(Player host, String title, int maxPlayers) {
		return createRoom(host, title, "LIAR", "EASY", maxPlayers, true, null);
	}
    
    public void deleteRoom(int roomId) {
        roomService.deleteRoom(roomId);
    }

    public List<Room> searchRoom(String title) {
        return roomService.searchRoom(title);
    }

    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }
	
    public Room getRoom(int roomId) {
    	return roomService.getRoom(roomId);
    }
    
    // RoomManager 만의 메소드
	
    public boolean joinRoom(int roomId, Player player) {
        Room room = roomService.getRoom(roomId);

        if (room == null) {
            return false;
        }
        
        if(!room.addPlayer(player)) {
        	return false;
        }

		int number = room.getPlayerNumber(player);

        // 방에 있는 플레이어들에게 다른 플레이어의 입장을 알림
        for (Player p : room.getPlayers()) {
            if (p.isConnected()) {
                p.send("Player" + number + " 입장 ("
                        + room.getPlayerCount() + "/"
                        + room.getMaxPlayers() + ")");
            }
        }
		return true;
    }
    
	public void leaveRoom(Room room, Player player) {
		synchronized(room) {
			room.removePlayer(player);
			
	        if (room.isEmpty()) {
	            roomService.deleteRoom(room.getRoomId());
	        } else if (player.equals(room.getHost())) {
	            room.changeHost();
	            
	            String msg = "Player" + room.getHost().getId() + "이 새로운 방장이 되었습니다.";
	            System.out.println(msg);
	            for(int i = 0; i<room.getPlayers().size(); i++) {
	            	room.getPlayers().get(i).send(msg);
	            }
	        }
        }
    }
	
	public int getPlayerNumber(int roomId, Player player) {
	    Room room = roomService.getRoom(roomId);

	    if (room == null) {
	        return 0;
	    }

	    return room.getPlayerNumber(player);
	}
	
	// 게임 시작은 방장 권한
	// 게임 시작 조건 : 게임이 시작 전이고 최소 인원이 채워졌을 때
	// 문구 출력 책임은 ClientHandler에게 전달 ,,
	public boolean canStartGame(Room room, Player player) {
		if (!player.equals(room.getHost())) return false;
		
	    if (!room.isWaiting()) {
	        player.send("게임이 진행 중입니다.");
	        return false;
	    }

	    if (room.getPlayerCount() < 3) {
	        player.send("최소 3명 이상의 플레이어가 필요합니다");
	        return false;
	    }
	    
	    for(int i=0; i<room.getPlayers().size(); i++) {
	    	Player p = room.getPlayers().get(i);
	    	
	    	if(p.isChoosingRemain()) {
	    		player.send("아직 게임 종료 후 선택 중인 참가자가 있습니다.");
	    		return false;
	    	}
	    }
	    player.send("게임을 시작할 수 있습니다.");
	    return true;
	}
	
	public boolean startGame(Room room, Player player) {
		synchronized(room) {
			if (!canStartGame(room, player)) {
		        return false;
		    }
		    room.startGame();
		    return true;
		}
	}
	
	public void handleRoomChat(int roomId, Player player, String message) {
	    Room room = roomService.getRoom(roomId);

	    if (room == null) {
	        return;
	    }
	    roomChatManager.handleChat(room, player, message);
	}
}