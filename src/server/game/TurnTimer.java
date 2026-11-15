package server.game;

public class TurnTimer implements Runnable{
	
	private GameManager gameManager; //시간초과를 알릴 대상
	private Thread timerThread;
	private int seconds;   //변수 추가
	
	public TurnTimer(GameManager gameManager) {
		this.gameManager = gameManager;
	};
	
	public void start(int seconds) {  //타이머 시작(새 스레드 만들어서 seconds초 동안 sleep
		this.seconds = seconds;
		timerThread = new Thread(this);
		timerThread.start();
	};
	
	
	public void stop() { //1. 시간 끝나기 전에 모두 완료된 경우: 스레드를 interrupt해서 깨움
		timerThread.interrupt();
	}; 
	
	public void timeout() { //2. 끝까지 잤을 경우 
		gameManager.timeOut();
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {    // 2의 경우: timeout()부르기
			Thread.sleep(seconds*1000);
			timeout();
		}
		catch (InterruptedException e) {  // 1의 경우: stop()부르기
			// TODO: handle exception
		}
	};
	
	
}
