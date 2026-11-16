package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClientReceiveThread extends Thread {
	private Socket socket;
	
	//서버와 연결된 소켓 저장
	ChatClientReceiveThread(Socket socket){
		this.socket = socket;
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
				
				//받은 메세지 출력
				System.out.println(msg);
			}
		}catch(IOException e) {
			System.out.println("서버로 부터 메세지 수신 중 오류 발생: " + e.getMessage());
		}
	}
	
}
