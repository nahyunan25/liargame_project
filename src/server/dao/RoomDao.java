package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import common.dto.RoomDto;

public class RoomDao {
	private final DBConnector connector;
	
	public RoomDao(DBConnector connector) {
		this.connector = connector;
	}
	
	//방 저장 -> DB가 만든 방 번호 반환
	public int insertRoom(RoomDto room) throws SQLException{
		String sql = "INSERT INTO rooms (title, game_mode, difficulty, is_public, current_players, max_players, state, password) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
			
			pstmt.setString(1, room.getTitle());
			pstmt.setString(2, room.getGameMode());
			pstmt.setString(3, room.getDifficulty());
			pstmt.setBoolean(4, room.isPublic());
			pstmt.setInt(5, room.getCurrentPlayers());
			pstmt.setInt(6, room.getMaxPlayers());
			pstmt.setString(7, room.getState());
			pstmt.setString(8, room.getPassword());
			
			pstmt.executeUpdate();
			
			try(ResultSet rs = pstmt.getGeneratedKeys()){
				if(rs.next()) {
					int roomId = rs.getInt(1);
					room.setRoomId(roomId);
					return roomId;
				}
			}
			throw new SQLException("방 번호를 가져오지 못했습니다.");
		}
	}
	
	//방 목록 조회(비밀번호 제외)
	public List<RoomDto> getAllRooms() throws SQLException{
		List<RoomDto> rooms = new ArrayList<>();
		
		String sql = "SELECT room_id, title, game_mode, difficulty, is_public, current_players, max_players, state "
				+ "FROM rooms ORDER BY room_id";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery()) {
			
			while(rs.next()) {
				RoomDto room = new RoomDto();
				
				room.setRoomId(rs.getInt("room_id"));
				room.setTitle(rs.getString("title"));
				room.setGameMode(rs.getString("game_mode"));
				room.setDifficulty(rs.getString("difficulty"));
				room.setPublic(rs.getBoolean("is_public"));
				room.setCurrentPlayers(rs.getInt("current_players"));
				room.setMaxPlayers(rs.getInt("max_players"));
				room.setState(rs.getString("state"));
				
				rooms.add(room);
			}
		}
		
		return rooms;
	}
	
	//입퇴장 시 현재 인원 변경
	public void updatePlayerCount(int roomId, int currentPlayers) throws SQLException{
		String sql = "UPDATE rooms SET current_players = ? WHERE room_id = ?";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setInt(1, currentPlayers);
			pstmt.setInt(2, roomId);
			
			pstmt.executeUpdate();
		}
	}
	
	//게임 시작, 종료 시 방 상태 변경
	public void updateState(int roomId, String state) throws SQLException{
		String sql = "UPDATE rooms SET state = ? WHERE room_id = ?";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setString(1, state);
			pstmt.setInt(2, roomId);
			
			pstmt.executeUpdate();
		}
	}
	
	//모두 퇴장한 방 삭제
	public void deleteRoom(int roomId) throws SQLException{
		String sql = "DELETE FROM rooms WHERE room_id = ?";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setInt(1, roomId);
			
			pstmt.executeUpdate();
		}
	}
	
	//room 테이블 리셋(서버 시작시)
	public void resetRooms() throws SQLException{
		String sql = "TRUNCATE TABLE rooms";
		
		try (Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			pstmt.executeUpdate();
		}
	}
}
