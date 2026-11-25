package server.network;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import common.Difficulty;
import common.GameMode;
import common.Player;
import server.game.GameManager;
import server.room.Room;
import server.room.RoomManager;

public class RoomEntryHandler {
	private RoomManager roomManager;
	private Map<Integer, GameManager> gameManagers;
	
	public RoomEntryHandler(RoomManager roomManager, Map<Integer,GameManager> gameManagers ) {
		this.roomManager = roomManager;
		this.gameManagers = gameManagers;
	}
	
	public Room enterRoom(Player player, BufferedReader bf) throws IOException {
	
		//CREATE or JOIN?
		player.send("1.방생성   2.방입장");
		String choice = bf.readLine();
		
		if (choice == null) {
			return null;
		}
		
		switch(choice) {
		case "1" :
			return createRoom(player, bf);
		case "2" :
			return joinRoom(player, bf);
			
		default:
			player.send("1 또는 2를 입력해주세요.");
			return null;
		}
	}
	
	private Room createRoom(Player player, BufferedReader bf) throws IOException{
			player.send("방 이름을 입력하세요:");
			String title = bf.readLine();
			
			// ============ 수정 부분 ==============
			GameMode gameMode = selectGameMode(player, bf);
			
			Difficulty difficulty = selectDifficulty(player, bf);
			// =====================================
		
			player.send("최대 인원을 입력하세요(3~10):");
			int maxPlayers = Integer.parseInt(bf.readLine());
			
			// =====================================
			boolean isPublic = selectRoomType(player, bf);
			
			String password = null;
			if(!isPublic) {
				player.send("비밀번호를 입력하세요:");
				password = bf.readLine();
			}
		
			Room room = roomManager.createRoom(player, title, gameMode, difficulty, maxPlayers, isPublic, password);
			// ========================================
			GameManager gameManager = new GameManager(room);
			gameManagers.put(room.getRoomId(), gameManager);
		
			player.send("방이 생성되었습니다.");
			player.send("방 번호:" + room.getRoomId());
			player.send("방 이름:" + room.getTitle());
			// ===========================================
			player.send("게임 모드:"+room.getGameMode());
			player.send("난이도:"+room.getDifficulty());
			player.send("최대 인원:" + room.getMaxPlayers());
			if(!isPublic) player.send("비밀번호:"+room.getPassword());
			// =============================================
		
			player.send(room.getTitle()+"방에 방장으로 입장했습니다.\n당신은 Player" + player.getId() + "입니다.");
		
			return room;
	}

	private Room searchRoom(Player player, BufferedReader bf) throws IOException {
		player.send("검색할 방 제목을 입력하세요: ");
		String roomname = bf.readLine();
		if (roomname == null) return null;
		
		if (roomname.isEmpty()) {
			player.send(roomname+"(이)가 포함된 방이 없습니다.");
			return null;
		}
		
		return joinRoom(player, bf, roomManager.searchRoom(roomname));
	}
	
	private Room joinRoom(Player player, BufferedReader bf, LIst<Room> rooms) throws IOException{
		List<Room> rooms = roomManager.getAllRooms();
		player.send("==== 방 목록 ====");
		
		for(int i =0; i<rooms.size(); i++) {
			Room listedRoom = rooms.get(i);
			
			player.send("방 번호:"+listedRoom.getRoomId() 
			+ " / 방 이름:"+ listedRoom.getTitle()
			+ " / 인원:" + listedRoom.getPlayerCount()
			+"/" + listedRoom.getMaxPlayers());
		}
		
		//방 선택
		player.send("입장할 방 번호를 입력하세요:");
		int roomId = Integer.parseInt(bf.readLine());
		Room room = roomManager.getRoom(roomId);
		if (room == null) {
			player.send("없는 방 번호입니다.");
			return null;
		}
		
		String password = null;
		if (!room.isPublic()) {
			player.send("비공개방입니다. 비밀번호를 입력하세요: ");
			password = bf.readLine();
			if (password == null) return null;
			
			if (!room.checkPassword(password)) {
				player.send("비밀번호가 틀렸습니다");
				return null;
			}
		}
	
		if(!roomManager.joinRoom(roomId, player, password)) {
			player.send("방이 가득 찼거나 게임이 진행 중이라 입장할 수 없습니다.");
			return null;
		}
            
		player.send(room.getTitle()+"방에 입장했습니다.\n당신은 Player" + player.getId() + "입니다.");

		return room;
	}
	
	private GameMode selectGameMode(Player player, BufferedReader bf) throws IOException {
		
		player.send("게임 모드: 1. 라이어게임  2. 바보게임");
		String choice = bf.readLine();

	    if (choice == null) {
	        return null;
	    }

	    switch (choice) {
	        case "1":
	            return GameMode.LIAR;

	        case "2":
	            return GameMode.FOOL;

	        default:
	            player.send("1 또는 2를 입력해주세요.");
	            return null;
	        }
	}
	
	private Difficulty selectDifficulty(Player player, BufferedReader bf) throws IOException {
		player.send("게임 난이도: 1. 이지  2. 하드");
		String choice = bf.readLine();

	    if (choice == null) {
	        return null;
	    }

	    switch (choice) {
	        case "1":
	            return Difficulty.EASY;

	        case "2":
	            return Difficulty.HARD;

	        default:
	            player.send("1 또는 2를 입력해주세요.");
	            return null;
	    }
	}
	private Boolean selectRoomType(Player player, BufferedReader bf) throws IOException {
		player.send("공개 여부: 1. 공개방  2. 비공개방");
		String choice = bf.readLine();

	    if (choice == null) {
	        return null;
	    }

	    switch (choice) {
	        case "1":
	            return true;

	        case "2":
	            return false;

	        default:
	            player.send("1 또는 2를 입력해주세요.");
	            return null;
	    }
	}
}
