package br.com.returnvoid.returnengine.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public abstract class Game{
	private boolean running = false;
	private Thread threadTps, threadFps;
	protected JFrame window;
	private GameSpeedTracker speedTracker;

	public Game(int tps, int maxFps, JFrame window) {
		this.speedTracker = new GameSpeedTracker(tps,maxFps);
		this.window = window;
		//this.window.setUndecorated(true);
		//this.window.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		this.window.setIgnoreRepaint(true);
		this.window.setLocation(100, 100);
		//this.window.setVisible(true);
		
		this.threadTps = new Thread(new Runnable() {
			@Override
			public void run() {
				runTps();
			}
		});
		
		this.threadFps = new Thread(new Runnable() {
			@Override
			public void run() {
				runFps();
			}
		});
	}
	
	public void stop() {
		this.running = false;
	}
	
	public void runTps() {
		while(this.isRunning()) {
			this.speedTracker.startTps();			
			this.onLoop();
			this.speedTracker.stopTps();			
			this.speedTracker.ensureTps();			
		}
	}
	
	public void runFps() {
		while(this.isRunning()) {
			this.speedTracker.startFps();
			BufferStrategy bufferStrategy = window.getBufferStrategy();
			Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
		    g.setColor(Color.BLACK);
		    g.fillRect(0, 0, window.getWidth(), window.getHeight());
		    this.onRender(g);
		    g.setColor(Color.white);
	        g.fillRect(0, 30, 60, 30);
	        g.setColor(Color.black);
	        g.setFont(new Font("", Font.BOLD, 12));
	        g.drawString(this.speedTracker.getTps() + " tps", 10, 42);
	        g.drawString(this.speedTracker.getFps() + " fps", 10, 54);
	        
	        g.dispose();
	        try {
	        	bufferStrategy.show();
			}catch (java.lang.IllegalStateException e) {
				//Essa exceção pode ser ignorada, ocorre quando finaliza-se o jogo
				//Enquanto essa thread ocorre
			}	
			this.speedTracker.stopFps();
			this.speedTracker.ensureFps();			
		}
	}
	protected abstract void onLoop();
	protected abstract void onRender(Graphics2D g);

	public void run() {
		this.running = true;
		this.window.setVisible(true);
		this.window.createBufferStrategy(2);
		
		this.threadFps.start();
		this.threadTps.start();
	}

	public boolean isRunning() {
		return running;
	}
	
	
	private class GameSpeedTracker {
		public final double MILI_SECOND = 1e3;
		
		private int max_fps;		
		private double startTimeFps;
		private double sleepTimeFps;
		private double loopTimeFps;
		private double expectedLoopTimeFps;
		
		private int expectedTps;		
		private double startTimeTps;
		private double sleepTimeTps;
		private double loopTimeTps;
		private double expectedLoopTimeTps;
		
		
		public GameSpeedTracker(int expectedTps, int max_fps) {
			this.expectedTps = expectedTps;
			this.max_fps = max_fps;
		}
		
		public int getTps() {				
			return (int)(this.MILI_SECOND/((this.loopTimeTps + this.sleepTimeTps)));
		}
		
		public int getFps() {			
			return (int)(this.MILI_SECOND/((this.loopTimeFps + this.sleepTimeFps)));
		}
		
		public void startFps() {
			this.startTimeFps = System.currentTimeMillis();				
		}
		
		public void startTps() {
			this.startTimeTps = System.currentTimeMillis();
		}
		
		public void stopFps() {
			this.loopTimeFps = (System.currentTimeMillis() - this.startTimeFps);
		}
		
		public void stopTps() {
			this.loopTimeTps = (System.currentTimeMillis() - this.startTimeTps);
		}
		
		public void ensureTps() {
			this.expectedLoopTimeTps = (this.MILI_SECOND / this.expectedTps);
			this.sleepTimeTps = (this.expectedLoopTimeTps - this.loopTimeTps);	
			try {
				if(this.sleepTimeTps > 0)		
					Thread.sleep((long)Math.floor(this.sleepTimeTps));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void ensureFps() {
			this.expectedLoopTimeFps = (this.MILI_SECOND / this.max_fps);	
			this.sleepTimeFps =  (this.expectedLoopTimeFps - this.loopTimeFps);					
			try {
				if(this.sleepTimeFps > 0)					
					Thread.sleep((long)Math.floor(this.sleepTimeFps));				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
		
	}
}



