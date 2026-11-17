package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SendMessageThread extends Thread {
	private PrintWriter pw;
	private Socket socket;
	private BufferedReader reader;
	private int count = 0;
	
	//메세지를 보낼 준비
	public SendMessageThread(Socket socket) {
		this.socket = socket;
		try {
			reader = new BufferedReader(
					new InputStreamReader(System.in,StandardCharsets.UTF_8));
			pw = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),true);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//메세지 입력 받기
	public void sendMessage() throws IOException{
		String message = reader.readLine();
		if(message == null) return;
		
		String request = "";
		
		//메시지 구분
		if(count == 0) {
			request = "CHAT:"+message;
		} else if(count ==1) {
			request = "VOTE:" + message;
		} else {
			request = "GUESS:" + message;
		}
		
		//서버에 메세지 보내기
		pw.println(request);
		count++;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//메세지 계속 입력받기
		while(!Thread.currentThread().isInterrupted()) {
			try {
				sendMessage();
			}catch(IOException e) {
				System.out.println("서버로 메세지 전송 중 오류 발생: "+ e.getMessage());
				break;
			}
		}
	}
	
	
}
