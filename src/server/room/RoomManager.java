package server.room;

import java.util.Map;

import common.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class RoomManager {
	Map<Integer, Room> rooms = new LinkedHashMap<>();
	
	int nextRoomId = 0;
	
	public Room createRoom(Player host, String title, 
			String gameMode, String difficulty, int maxPlayers, 
			boolean isPublic, String password) {

		int roomId = nextRoomId++;
		Room room;
    
		if (isPublic) {
			room = new Room(host, roomId, title, 
					gameMode, difficulty, maxPlayers);
		} else {
			room = new Room(host, roomId, title, gameMode, 
					difficulty, maxPlayers, password);
		}
    
		rooms.put(roomId, room);
		return room;
		}
	
    	public void deleteRoom(int roomId){
    		rooms.remove(roomId);
    	};
    	
    	//제목으로 방 찾기
    	public List<Room> searchRoom(String title) {
            List<Room> result = new ArrayList<>();

            for (Room room : rooms.values()) {
                if (room.getTitle().contains(title)) {
                    result.add(room);
                }
            }
            return result;
        }
    	
    	public List<Room> getAllRooms() {
    		return new ArrayList<>(rooms.values());
    		}
	
}

