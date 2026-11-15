package server.room;

import java.util.ArrayList;
import java.util.List;

import common.Player;

public class Room {
	private int roomId;
	public int getRoomId() {
		return roomId;
	}

	private Player host;
	public Player getHost() {
		return host;
	}
	private RoomState state;
	
	// 사용자에게 입력받는 정보
	private String title;
	public String getTitle() {
		return title;
	}
	private String gameMode;
	private String difficulty;
	private int maxPlayers;
	private boolean isPublic;
	private String password;
	

	// 공개방 생성자
	// host는 방을 생성할 때 createRoom을 호출한 플레이어
	public Room(Player host, int roomId, String title,
	            String gameMode, String difficulty, int maxPlayers) {

	    this.host = host;
	    this.roomId = roomId;
	    this.title = title;
	    this.gameMode = gameMode;
	    this.difficulty = difficulty;
	    this.maxPlayers = maxPlayers;

	    this.isPublic = true;
	    this.password = null;
	    this.state = RoomState.WAITING;

	    this.players = new ArrayList<>();
	    this.players.add(host);
	}

	// 비공개방 생성자
	public Room(Player host, int roomId, String title,
	            String gameMode, String difficulty, int maxPlayers,
	            String password) {

	    this(host, roomId, title, gameMode, difficulty, maxPlayers);

	    this.isPublic = false;
	    this.password = password;
	}


	private List<Player> players;
	
	public void addPlayer(Player player) {
		if (isPlaying() || isFull()) {
	        return;
	    }
		players.add(player);
	}
	
	// 수정 필요
	public void addBotPlayer(Player botPlayer) {
		players.add(botPlayer);
	}
	
	
	public void removePlayer(Player player) {
		players.remove(player);
	};
	
	// 방장이 leaveRoom을 실행한 후, 입장 순서가 그 다음인 사람이 방장이 된다
	public void changeHost() {
	    if (!players.isEmpty()) {
	        host = players.get(0);
	    } else {
	        host = null;
	    }
	}
	
	// 해당 룸에 현재 플레이어가 몇 명인지 확인
	public int getPlayerCount() {
		return players.size();
	};
	
	private boolean isFull() {
	    if (players.size() >= maxPlayers)
	        return true;
	    else
	        return false;
	}
	public boolean isEmpty() {
	    return players.isEmpty();
	}
	
	// 대기실 클래스를 따로 만들지 않고, 상태로 구분
	// 게임이 시작되면 방의 상태를 변경
	public boolean isWaiting() {
        return state == RoomState.WAITING;
    }
	public boolean isPlaying() {
        return state == RoomState.PLAYING;
    }
}