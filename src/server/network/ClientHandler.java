package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import server.game.GameManager;
import common.Player;

public class ClientHandler extends Thread {
	private GameManager gameManager;
	private Player player;
	private Socket socket;
	List<PrintWriter> lw;
	
	//서버와 클라이언트 연결 중비
	public ClientHandler(Socket socket, List<PrintWriter> lw, GameManager gameManager) {
		this.socket = socket;
		this.lw = lw;
		this.gameManager = gameManager;

	}
	
	//플레이어 정보 저장
	public void setPlayer(Player player) {
		this.player = player;
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
				
				//모든 클라이언트한테 입력 내용 보여주기
				write("Player" + player.getId()+ ": " + tokens[1]);
				//메세지 case
				switch(tokens[0]) {
					case "CHAT":
						if(tokens.length >1) {
							gameManager.handleChat(player, tokens[1]);
						}
						break;
						
					case "VOTE":
						int targetId = Integer.parseInt(tokens[1]);
						gameManager.handleVote(player, targetId);
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
