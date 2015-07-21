/*
 * 
 */
package com.welkinlan.whackamole;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class MoleGame.
 */
public class MoleGameThread extends Thread {

	/** The Constant UPPER_BOUND. */
	final static public int UPPER_BOUND = 10;
	
	/** The Constant INTERVAL. */
	final static public int INTERVAL = 1200;

	/** The upper bound. */
	private int upperBound;
	
	/** The time wait. */
	private int timeWait;
	
	/** The handler. */
	private Handler handler;

	/**
	 * The Enum GameState.
	 */
	public enum GameState{/** The running state. */
RUNNING, /** The stopped state. */
 STOPPED};

	/** The gstate. */
	private GameState gstate;

	/**
	 * Instantiates a new mole game.
	 *
	 * @param handler the handler
	 * @param upperBound the upper bound
	 */
	public MoleGameThread(Handler handler, int upperBound) {
		super();
		this.upperBound = UPPER_BOUND;
		gstate = GameState.RUNNING;
		this.handler = handler;
		this.timeWait = INTERVAL;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (gstate == GameState.RUNNING) {
				int newPosition;
				// generate random position
				newPosition = (int) (Math.random()*upperBound);
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
	}

	/**
	 * Next step.
	 *
	 * @param newPosition the new position
	 */
	private void nextStep(int newPosition){
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt("newPosition", newPosition);
		msg.setData(b);
		handler.sendMessage(msg);
	}

	/**
	 * Gets the upper bound.
	 *
	 * @return the upper bound
	 */
	public int getUpperBound() {
		return upperBound;
	}

	/**
	 * Sets the upper bound.
	 *
	 * @param upperBound the new upper bound
	 */
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Gets the game state.
	 *
	 * @return the game state
	 */
	public String getGameState() {
		return gstate.name();
	}

	/**
	 * Sets the state.
	 *
	 * @param set the new state
	 */
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

	/**
	 * Sets the time to wait.
	 *
	 * @param timeWait the new time to wait
	 */
	public void setTimeToWait(int timeWait){
		this.timeWait=timeWait;

	}


}
