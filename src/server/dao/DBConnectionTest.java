package server.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionTest {

    public static void main(String[] args) {
    	
    	DBConnector connector = new DBConnector();
    	
        try (Connection connection = connector.getConnection()) {
            System.out.println("DB 연결 성공!");

        } catch (SQLException e) {
            System.out.println("DB 연결 실패");
            e.printStackTrace();
        }
    }
}