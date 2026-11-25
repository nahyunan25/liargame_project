package server.room;

import java.util.ArrayList;
import java.util.List;

import common.Difficulty;
import common.GameMode;
import common.Player;

public class Room {
	private int roomId;
	public int getRoomId() {
		return roomId;
	}

	private Player host;
	public synchronized Player getHost() {
		return host;
	}
	private RoomState state;
	
	// 사용자에게 입력받는 정보
	private String title;
	public String getTitle() {
		return title;
	}
	private GameMode gameMode;
	public GameMode getGameMode() {
		return gameMode;
	}

	private Difficulty difficulty;
	public Difficulty getDifficulty() {
		return difficulty;
	}
	
	private int maxPlayers;
	public int getMaxPlayers() {
		return maxPlayers;
	}

	private boolean isPublic;
	private String password;
	public String getPassword() {
		return password;
	}
	

	// 공개방 생성자
	// host는 방을 생성할 때 createRoom을 호출한 플레이어
	// 11.25 공개방/비공개방 생성자 통합
	
	public Room(Player host, int roomId, String title,
	            GameMode gameMode, Difficulty difficulty, int maxPlayers,
	            boolean isPublic, String password) {

	    this.host = host;
	    this.roomId = roomId;
	    this.title = title;
	    this.gameMode = gameMode;
	    this.difficulty = difficulty;
	    this.maxPlayers = maxPlayers;
	    this.isPublic = isPublic;
	    this.password = isPublic ? null : password;
	    
	    this.state = RoomState.WAITING;

	    this.players = new ArrayList<>();
	    host.setId(1);
	    this.players.add(host);

	}
	/*
	// 비공개방 생성자
	public Room(Player host, int roomId, String title,
	            GameMode gameMode, Difficulty difficulty, int maxPlayers,
	            String password) {

	    this(host, roomId, title, gameMode, difficulty, maxPlayers);

	    this.isPublic = false;
	    this.password = password;
	}
	*/
	

	private List<Player> players;
	
	//list 읽는 중 새 player 추가 됐을 때 오류 방지
	public synchronized List<Player> getPlayers() {
		return new ArrayList<>(players);
	}
	

	public synchronized boolean addPlayer(Player player) {
		if (isPlaying() || isFull()) {
	        return false;
	    }
		
		//첫 player id
		int id = 1;
		//사용 중인 번호면 다음 번호 확인
		for(int i = 0; i< players.size(); i++ ) {
			if(players.get(i).getId() == id) {
				id++;
				i=-1;
			}
		}
		player.setId(id);
		players.add(player);
		return true;
	}
	
	// 수정 필요
	public synchronized void addBotPlayer(Player botPlayer) {
		players.add(botPlayer);
	}
	
	
	public synchronized void removePlayer(Player player) {
		players.remove(player);
	};
	
	// 방장이 leaveRoom을 실행한 후, 입장 순서가 그 다음인 사람이 방장이 된다
	public synchronized void changeHost() {
	    if (!players.isEmpty()) {
	        host = players.get(0);
	    } else {
	        host = null;
	    }
	}
	
	public synchronized int getPlayerNumber(Player player) {
	  return player.getId();
	}
	
	// 해당 룸에 현재 플레이어가 몇 명인지 확인
	public synchronized int getPlayerCount() {
		return players.size();
	};
	
	private synchronized boolean isFull() {
	    if (players.size() >= maxPlayers)
	        return true;
	    else
	        return false;
	}
	public synchronized boolean isEmpty() {
	    return players.isEmpty();
	}
	
	
	// 대기실 클래스를 따로 만들지 않고, 상태로 구분
	// 게임이 시작되면 방의 상태를 변경
	public synchronized boolean isWaiting() {
        return state == RoomState.WAITING;
    }
	public synchronized boolean isPlaying() {
        return state == RoomState.PLAYING;
    }
	
	public synchronized void startGame() {
	    state = RoomState.PLAYING;
	}
	
	public synchronized void returnToWaiting() {
		state = RoomState.WAITING;
	}
}