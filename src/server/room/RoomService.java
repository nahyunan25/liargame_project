package server.room;

import common.Player;
import server.room.Room;
import server.room.RoomManager;

public class RoomService {
	private RoomManager roomManager;
	
	public RoomService(RoomManager roomManager) {
        this.roomManager = roomManager;
    }
	
	public void joinRoom(Room room, Player player) {
		if (room.isPlaying()) {
	        return;
	    }
        room.addPlayer(player);
    }

	public void leaveRoom(Room room, Player player) {
		room.removePlayer(player);
		
        if (room.isEmpty()) {
            roomManager.deleteRoom(room.getRoomId());
        } else if (player.equals(room.getHost())) {
            room.changeHost();
        }
    }
	
	// 게임 시작은 방장 권한
	// 게임 시작 조건 : 게임이 시작 전이고 최소 인원이 채워졌을 때
	// 문구 출력 책임은 ClientHandler에게 전달 ,,
	public boolean canStartGame(Room room, Player player) {
		if (!player.equals(room.getHost())) return false;
		
	    if (!room.isWaiting()) {
	        System.out.println("게임이 진행 중입니다.");
	        return false;
	    }

	    if (room.getPlayerCount() < 3) {
	        System.out.println("최소 3명 이상의 플레이어가 필요합니다.");
	        return false;
	    }

	    System.out.println("게임을 시작할 수 있습니다.");
	    return true;
	}
	
	public boolean startGame(Room room, Player player) {
	    if (!canStartGame(room, player)) {
	        return false;
	    }
	    room.startGame();
	    return true;
	}
}