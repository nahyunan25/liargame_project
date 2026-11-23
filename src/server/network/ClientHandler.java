package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import server.game.GameManager;
import server.room.Room;
import server.room.RoomManager;
import common.Player;

public class ClientHandler extends Thread {
	private GameManager gameManager;
	private Map<Integer, GameManager> gameManagers;
	private RoomManager roomManager;
	private Player player;
	private Room room;
	private RoomEntryHandler roomEntryHandler;
	private GameRequestHandler gameRequestHandler;
	private Socket socket;
	List<PrintWriter> lw;


	/* 기존 코드 => 생성자 수정
	public ClientHandler(Socket socket, List<PrintWriter> lw, GameManager gameManager, RoomService roomService) {
		this.socket = socket;
		this.lw = lw;
		this.gameManager = gameManager;
		this.roomService = roomService;
	 */
	
	//서버와 클라이언트 연결 중비
	public ClientHandler(Socket socket, List<PrintWriter> lw, Map<Integer, GameManager> gameManagers, RoomManager roomManager) {
		this.socket = socket;
		this.lw = lw;
		this.gameManagers = gameManagers;
		this.roomManager = roomManager;
		
		this.roomEntryHandler = new RoomEntryHandler(roomManager,gameManagers);
		// Player 객체 생성
	    this.player = new Player(0, false);

	}
	
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		PrintWriter pw = null;
		
		try {
			//메세지 주고받기 준비
			BufferedReader bf = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8),true);
			addWriter(pw);
			player.setWriter(pw);
			
			room = roomEntryHandler.enterRoom(player, bf);
			if(room == null) {
				return;
			}
			
			gameManager = gameManagers.get(room.getRoomId());
			
			gameRequestHandler = new GameRequestHandler(gameManager);
			
			String request = "";
			
			//클라이언트에서 보낸 메세지 받기
			while(true) {
				request = bf.readLine();
				
				if (request == null) {
					log("클라이언트로부터 연결 끊김 (EOF)");
					break;
				}
				
				if(request.trim().isEmpty()) continue;
				
				//게임 종료 후 선택 처리
				if(player.isChoosingRemain()) {
					if(request.equals("1")) {
						player.resetForNewGame();
						player.setChoosingRemain(false);
						
						player.send("PHASE:WAITING");
						player.send("대기실로 이동했습니다.");
					} else if(request.equals("2")) {
						player.send("접속을 종료합니다.");
						return;
					}
					continue;
				}
				//방에서 게임 기다리기 -> 방장이 시작 입력하면 시작
				if(room.isWaiting()) {
					if(player.equals(room.getHost()) && request.equals("시작")) {
						if(roomManager.startGame(room, player)) {
							gameManager.startGame();
						}
					} else {
						roomManager.handleRoomChat(room.getRoomId(), player, request);
					}
					continue;
				}
				
				gameRequestHandler.handleRequest(player, request);
				
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			//방에서 플레이어 제거
			if(room != null) {
				roomManager.leaveRoom(room, player);
				
				//모두 나간 방의 GameManager 제거
				if(room.isEmpty()) {
					gameManagers.remove(room.getRoomId());
				}
			}
			

			//전송 목록 제거
			if (pw != null) {
				removeWriter(pw);
			}
			//소켓 연결 종료
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//연결된 클라이언트 추가
	private void addWriter(PrintWriter writer) {
		synchronized (lw) {
			lw.add(writer);
		}
	}
	
	//연결 끊긴 클라이언트 삭제
	private void removeWriter(PrintWriter writer) {
		synchronized (lw) {
			lw.remove(writer);
		}
	}
	
	//모든 클라이언트에게 메세지 보내기
	private void write(String data) {
		synchronized(lw) {
			for(PrintWriter writer : lw) {
				writer.println(data);
				writer.flush();
			}
		}
	}
	
	//서버에 메세지 출력
	private void log(String log) {
		System.out.println(log);
	}
	
}