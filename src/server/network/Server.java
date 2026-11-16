package server.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import server.game.GameManager;
import common.Player;
import server.room.RoomManager;
import server.room.RoomService;

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
            RoomManager roomManager = new RoomManager();
            RoomService roomService = new RoomService(roomManager);
            List<Player> players = new ArrayList<>();
            
            GameManager gameManager = new GameManager(roomManager, players);

            //클라이언트 연결
            while (true) {
                Socket socket = serverSocket.accept();

                consoleLog("클라이언트가 연결되었습니다: "
                        + socket.getRemoteSocketAddress());
                // (1116) ClientHandler 생성자에 roomService 추가하였습니다
                ClientHandler handler = new ClientHandler(socket, listWriters, gameManager, roomService);
                
                handler.start();
            }

        } catch (IOException e) {
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