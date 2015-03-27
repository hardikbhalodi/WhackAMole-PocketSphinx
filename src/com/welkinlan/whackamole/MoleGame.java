package com.welkinlan.whackamole;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MoleGame extends Thread {

	final static public int LOWERBOUND = 0;
	final static public int UPPERBOUND = 5;
	final static public int INTERVAL = 2500;

	private int upperBound;
	private int timeWait;
	private Handler handler;

	public enum GameState{RUNNING, STOPPED};

	private GameState gstate;

	public MoleGame(Handler handler) {
		super();
		this.upperBound = UPPERBOUND;
		gstate = GameState.RUNNING;
		this.handler=handler;
		this.timeWait=INTERVAL;
	}

	@Override
	public void run() {

		while(gstate == GameState.RUNNING) {
			int newPosition;
			// generate random position
			newPosition=(int) (Math.random()*(upperBound+1));
			//set up the new position
			nextStep(newPosition);
			
			//wait time
			try {
				sleep(timeWait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	private void nextStep(int newPosition){
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt("newPosition", newPosition);
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public String getGameState() {
		return gstate.name();
	}

	public void setState(String state) {
		if (state.equals(GameState.RUNNING.name()))	this.gstate = GameState.RUNNING;
		else this.gstate = GameState.STOPPED;
			
	}
	
	/**
	 * Finish thread in clean.
	 * */
	public synchronized void stopThread() {
		this.gstate = GameState.STOPPED;
		
	}
	
	public void setTimeToWait(int timeWait){
		this.timeWait=timeWait;
		
	}
	

}
