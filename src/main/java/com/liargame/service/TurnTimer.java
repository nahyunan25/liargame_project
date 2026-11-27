package com.liargame.service;


public class TurnTimer implements Runnable{
	
	private Runnable timeoutAction;
	private Thread timerThread;
	private int seconds;
	
	public TurnTimer(Runnable timeoutAction) {
		this.timeoutAction = timeoutAction;
	}
	
	public void start(int seconds) {
		this.seconds = seconds;
		timerThread = new Thread(this);
		timerThread.start();
	}
	
	public void stop() {
		if(timerThread != null) {
			timerThread.interrupt();
		}
	}
	
	public void timeout() {
		timeoutAction.run();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(seconds*1000);
			timeout();
		} catch(InterruptedException e) {
			
		}
	}
	
	public boolean isCurrentThread() {
		return Thread.currentThread() == timerThread;
	}
	
	
	
}
