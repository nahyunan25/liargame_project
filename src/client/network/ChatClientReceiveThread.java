package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClientReceiveThread extends Thread {
	private Socket socket;
	private SendMessageThread sendThread;
	
	//서버와 연결된 소켓 저장
	ChatClientReceiveThread(Socket socket, SendMessageThread sendThread){
		this.socket = socket;
		this.sendThread = sendThread;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try(BufferedReader br = new BufferedReader(
				new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8))){
			
			//서버에서 보낸 메세지 받기
			while(true) {
				String msg = br.readLine();
				
				if(msg == null) {
					System.out.println("서버와의 연결이 끊어졌습니다.");
					break;
				}
				
				//받은 메세지 출력(phase 구분 메세지와 일반 메세지 구분)
				if(msg.startsWith("PHASE:")) {
					String phase = msg.substring(6);
					
					sendThread.setPhase(phase);
				}else {
					System.out.println(msg);
				}
			}
		}catch(IOException e) {
			System.out.println("서버로 부터 메세지 수신 중 오류 발생: " + e.getMessage());
		}
	}
	
}
