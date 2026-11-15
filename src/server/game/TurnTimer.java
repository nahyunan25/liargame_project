package server.game;

public class TurnTimer {
	
	private GameManager gameManager; //시간초과를 알릴 대상
	private Thread timerThread;
	
	public TurnTimer(GameManager gameManager) {};
	
	public void start(int seconds) {}; //타이머 시작(새 스레드 만들어서 seconds초 동안 sleep
	
	public void stop() {}; //1. 시간 끝나기 전에 모두 완료된 경우: 스레드를 interrupt해서 깨움
	public void timeout() {}; //2. 끝까지 잤을 경우 
}
