package server.room;

import java.util.Map;

import common.Difficulty;
import common.GameMode;
import common.Player;
import common.dto.RoomDto;
import server.dao.DBConnector;
import server.dao.RoomDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class RoomService {
	private Map<Integer, Room> rooms = new LinkedHashMap<>();

	private RoomDao roomDao = new RoomDao(new DBConnector());

	public synchronized Room createRoom(Player host, String title, GameMode gameMode, Difficulty difficulty,
			int maxPlayers, boolean isPublic, String password) throws SQLException {

		RoomDto dto = new RoomDto();

		dto.setTitle(title);
		dto.setGameMode(gameMode.name());
		dto.setDifficulty(difficulty.name());
		dto.setMaxPlayers(maxPlayers);
		dto.setCurrentPlayers(1);
		dto.setPublic(isPublic);
		dto.setPassword(password);
		dto.setState("WAITING");

		int roomId = roomDao.insertRoom(dto);

		Room room = new Room(host, roomId, title, gameMode, difficulty, maxPlayers, isPublic, password);

		rooms.put(roomId, room);

		return room;
	}

	public synchronized void deleteRoom(int roomId) {
		rooms.remove(roomId);
		
		try {
			roomDao.deleteRoom(roomId);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	};

	// 제목으로 방 찾기
	public synchronized List<Room> searchRoom(String title) {
		List<Room> result = new ArrayList<>();

		for (Room room : rooms.values()) {
			if (room.getTitle().contains(title)) {
				result.add(room);
			}
		}
		return result;
	}

	// roomId에 해당하는 room 객체 반환
	public synchronized Room getRoom(int roomId) {
		return rooms.get(roomId);
	}

	public synchronized List<Room> getAllRooms() {
		return new ArrayList<>(rooms.values());
	}
	
	public void updatePlayerCount(Room room) {
	    try {
	        roomDao.updatePlayerCount(
	                room.getRoomId(), room.getPlayerCount());
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateState(int roomId, String state) {
	    try {
	        roomDao.updateState(roomId, state);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public synchronized void resetRooms() throws SQLException {
	    roomDao.resetRooms();
	    rooms.clear();
	}
	
	public List<RoomDto> getRoomList() throws SQLException {
	    return roomDao.getAllRooms();
	}

}