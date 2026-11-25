package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
	private static final String URL = "jdbc:mysql://localhost:3306/liargame"; //ip 확인 후 수정
	private static final String USER = "liargame_user";
	private static final String PASS = ""; //공유한 비밀번호 추가
	
	public Connection getConnection() throws SQLException	{
		return DriverManager.getConnection(URL, USER, PASS);
	}
	
}