package server.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import server.game.GameManager;
import common.Player;
import server.room.RoomService;
import server.room.RoomManager;

public class Server {
    public static final int PORT = 50023; //서버 포트 번호

    public static void main(String[] args) {
    	
        ServerSocket serverSocket = null;
        List<PrintWriter> listWriters =
                Collections.synchronizedList(new ArrayList<>());

        try {
        	//서버 시작
            serverSocket = new ServerSocket(PORT);

            consoleLog(serverSocket.getInetAddress().getHostAddress()
                    + ":" + PORT + " 서버가 시작됨!");

            //게임 관리 객체 생성
            RoomService roomService = new RoomService();
            roomService.resetRooms();
            
            RoomManager roomManager = new RoomManager(roomService);
            // 기존 코드 : List<Player> players = new ArrayList<>(); => 삭제

            // 기존 코드 : GameManager gameManager = new GameManager(roomManager, players); => 수정
            Map<Integer, GameManager> gameManagers = new ConcurrentHashMap<>();

            //클라이언트 연결
            while (true) {
                Socket socket = serverSocket.accept();

                consoleLog("클라이언트가 연결되었습니다: "
                        + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, listWriters, gameManagers, roomManager);
                
                handler.start();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    //서버 콘솔 출력
    private static void consoleLog(String log) {
        System.out.println("[server "
                + Thread.currentThread().threadId()
                + "] " + log);
    }
}