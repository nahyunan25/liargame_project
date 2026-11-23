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
			
			// 기존 코드 : gameManager.join(player); => 삭제
			
			
			//CREATE or JOIN?
			player.send("1.방생성   2.방입장");
			String choice = bf.readLine();
			
			if (choice == null) {
				return;
			}
			
			switch(choice){
				case "1":
					// 1. Client 가 CREATE ROOM 요청(title, maxPlayers까지만 입력 임시)
					player.send("방 이름을 입력하세요:");
					String title = bf.readLine();
					
					player.send("최대 인원을 입력하세요(3~10):");
					int maxPlayers = Integer.parseInt(bf.readLine());
					
					room = roomManager.createRoom(player, title, maxPlayers);
					gameManager = new GameManager(room);
					gameManagers.put(room.getRoomId(), gameManager);
					
					player.send("방이 생성되었습니다.");
					player.send("방 번호:" + room.getRoomId());
					player.send("방 이름:" + room.getTitle());
					player.send("최대 인원:" + room.getMaxPlayers());
					
					player.send(room.getTitle()+"방에 방장으로 입장했습니다.\n당신은 Player" + player.getId() + "입니다.");
					break;
					
				case "2":
					// 2. Client 가 JOIN ROOM 요청
					//전체 방 목록 출력
					List<Room> rooms = roomManager.getAllRooms();
					player.send("==== 방 목록 ====");
					for(int i =0; i<rooms.size(); i++) {
						Room listedRoom = rooms.get(i);
						
						player.send("방 번호:"+listedRoom.getRoomId() 
						+ " / 방 이름:"+ listedRoom.getTitle()
						+ " / 인원:" + listedRoom.getPlayerCount()
						+"/" + listedRoom.getMaxPlayers());
					}
					
					//방 선택
					player.send("입장할 방 번호를 입력하세요:");
					int roomId = Integer.parseInt(bf.readLine());
					
					if (roomManager.joinRoom(roomId, player)) {
						room = roomManager.getRoom(roomId);
						gameManager = gameManagers.get(roomId);
		
			            player.send(room.getTitle()+"방에 입장했습니다.\n당신은 Player" + player.getId() + "입니다.");
			        } else {
			            player.send("방 입장에 실패했습니다.");
			            return;
			        }
					break;
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