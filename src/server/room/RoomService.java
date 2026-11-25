package server.room;

import java.util.Map;

import common.Difficulty;
import common.GameMode;
import common.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class RoomService {
	private Map<Integer, Room> rooms = new LinkedHashMap<>();
	
	private int nextRoomId = 1;
	
	public synchronized Room createRoom(Player host, String title, 
			GameMode gameMode, Difficulty difficulty, int maxPlayers, 
			boolean isPublic, String password) {
		
		int roomId = nextRoomId++;
		Room room = new Room(host, roomId, title, gameMode, difficulty, maxPlayers, isPublic, password);

		rooms.put(roomId, room);
		return room;
		}
	
    	public synchronized void deleteRoom(int roomId){
    		rooms.remove(roomId);
    	};
    	
    	//제목으로 방 찾기
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
	
}