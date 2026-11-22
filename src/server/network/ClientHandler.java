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
	private static int nextId = 1;
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
		
		// Player 객체 생성
		int id = nextId++;
	    this.player = new Player(id, false);

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
			
			// 기존 코드 : gameManager.join(player); => 삭제
			
			
			// 1. Client 가 CREATE ROOM 요청
			// 예시
			Room room = roomManager.createRoom(player, "test", "LIAR", "EASY",4, true, null);
			GameManager gameManager = new GameManager(room);
			gameManagers.put(room.getRoomId(), gameManager);
			
			
			// 2. Client 가 JOIN ROOM 요청
			// searchRoom 등을 통해 Client가 입장하고 싶은 roomId를 알아냈다고 가정
			// 예를 들어 4번 방에 입장
			int roomId=4;
			// joinRoom 이 Client가 입장가능한지 Room의 조건을 확인, 결과를 boolean으로 반환
			if (roomManager.joinRoom(roomId, player)) {
				int number = roomManager.getPlayerNumber(roomId, player);
	            player.send("당신은 Player" + player.getId() + "입니다.");
	        } else {
	            player.send("방 입장에 실패했습니다.");
	        }
			
			
			String request = "";
			
			//클라이언트에서 보낸 메세지 받기
			while(true) {
				request = bf.readLine();
				
				if (request == null) {
					log("클라이언트로부터 연결 끊김 (EOF)");
					break;
				}
				
				if(request.trim().isEmpty()) continue;
				
				String[] tokens = request.split(":", 2);
				
				
				//메세지 case
				switch(tokens[0]) {
					case "CHAT":
						if(tokens.length >1) {
							gameManager.handleChat(player, tokens[1]);
						}
						break;
						
					case "VOTE":
						try{
							int targetId = Integer.parseInt(tokens[1]);
							gameManager.handleVote(player, targetId);
						}catch(NumberFormatException e) {
							player.send("의심되는 player의 번호를 입력해주세요.");
						}
						
						break;
						
					case "GUESS":
						gameManager.handleLiarGuess(player, tokens[1]);
						break;
						
					default:
						System.out.println("알 수 없는 메세지: "+ request);
						break;
				}
				
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			//클라이언트 연결 끊기
			if (pw != null) {
				removeWriter(pw);
			}
			
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