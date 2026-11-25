package client.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientNetwork {
	private static final int SERVER_PORT = 50023;
	private static Socket socket;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		//서버 IP 입력 받기
		System.out.print("Server IP를 입력하세요: ");
		String serverIp = sc.nextLine();
		
		socket = new Socket();
		
		try {
			//서버 연결
			socket.connect(new InetSocketAddress(serverIp, SERVER_PORT));
			consoleLog("서버 연결 완료!");
			
			//메세지를 받는 스레드와 보내는 스레드 실행
			SendMessageThread sendThread = new SendMessageThread(socket);
			
			sendThread.setDaemon(true); //송신 혼자 남으면 종료
			
			ChatClientReceiveThread receiveThread = new ChatClientReceiveThread(socket, sendThread);
			receiveThread.start();
			sendThread.start();
		} catch(IOException e) {
			System.out.println("서버 연결 실패: "+ e.getMessage());
		}
	}
	
	//클라이언트 화면에 메세지 출력
	private static void consoleLog(String log) {
		System.out.println(log);
	}
}
