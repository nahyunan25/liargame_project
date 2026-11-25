package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.dto.KeywordDto;

public class KeywordDao {
	private final DBConnector connector;
	
	public KeywordDao(DBConnector connector) {
		this.connector = connector;
	}
	
	
	//category id 조회
	public List<Integer> getCategoryIds() throws SQLException{
		List<Integer> categoryIds = new ArrayList<>();
		
		String sql = "SELECT category_id FROM categories ORDER BY category_id";
		
		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery()){
			
			while(rs.next()) {
				categoryIds.add(rs.getInt("category_id"));
			}
		}
		return categoryIds;
	}
	
	
	//주제와 난이도에 맞는 keyword 조회
	public List<KeywordDto> getKeywords(int categoryId, String difficulty) throws SQLException{
		List<KeywordDto> keywords = new ArrayList<>();
		
		String sql = "SELECT k.keyword_id, k.category_id, c.category, k.keyword, k.difficulty "
				+ "FROM keywords k "
				+ "JOIN categories c ON k.category_id = c.category_id "
				+ "WHERE k.category_id = ? AND k.difficulty = ? "
				+ "ORDER BY k.keyword_id";

		try(Connection conn = connector.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setInt(1, categoryId);
			pstmt.setString(2,difficulty);
			
			try(ResultSet rs = pstmt.executeQuery()){
				while(rs.next()) {
					KeywordDto keyword = new KeywordDto();
					
					keyword.setKeywordId(rs.getInt("keyword_id"));
					keyword.setCategoryId(rs.getInt("category_id"));
					keyword.setCategory(rs.getString("category"));
					keyword.setKeyword(rs.getString("keyword"));
					keyword.setDifficulty(rs.getString("difficulty"));
					
					keywords.add(keyword);
				}
			}
		}
		return keywords;	
	}
}