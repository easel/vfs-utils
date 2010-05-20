package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Stopwatch extends AbstractCommand {

	private class Timer {
		private long startedOn = 0;
		private long lapStartedOn = 0;
		private long lapCount=0;
		
		public Timer() {
			startedOn = System.currentTimeMillis();
			lapStartedOn = startedOn;
			lapCount++;
		}
		
		public long lap() {
			long now = System.currentTimeMillis();
			long result = now - lapStartedOn;
			lapStartedOn = now;
			lapCount++;
			return result;
		}
		
		public long getLapCount() {
			return this.lapCount;
		}
		
		public long total() {
			return System.currentTimeMillis() - startedOn;
		}
	}
	
	public Stopwatch() {
		super("stopwatch", "Measures elapsed time" , "start | lap | total | stop");
		
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(1);
		
		String subCommand = args.getArgument(0);
		
		if (subCommand.equals("start")) {
			start(engine);
		}
		else if (subCommand.equals("lap")) {
			lap(engine);
		}
		else if (subCommand.equals("stop")) {
			stop(engine);
		}
		else {
			total(engine);
		}
	}
	
	public void start(Engine engine) throws CommandException {
		Timer timer = new Timer();
		engine.getContext().set("stopwatch", timer);
		engine.println("Started");
	}
	
	public void total(Engine engine) throws CommandException {
		Timer timer = (Timer) engine.getContext().get("stopwatch");
		
		if (timer==null) {
			start(engine);
		}
		else {
			engine.println("Total " + formatTime(timer.total()));
		}
		
	}
	
	public void stop(Engine engine) throws CommandException {
		Timer timer = (Timer) engine.getContext().get("stopwatch");
		if (timer!=null) {
			engine.getContext().unset("stopwatch");
			engine.println("Stopped, total " + formatTime(timer.total()));
		}			
	}
	
	public void lap(Engine engine) throws CommandException {
		Timer timer = (Timer) engine.getContext().get("stopwatch");
		if (timer==null) {
			start(engine);
		}
		else {
			engine.println("Lap " + timer.lapCount + " " + formatTime(timer.lap()));
		}
	}

	protected String formatTime(long time) {
	  
	  long millis = time % 1000;
	  long timeInSec = time/1000;
	  long seconds = timeInSec % 60;
      long timeInMin = timeInSec/60;
      long minutes = timeInMin%60;
      long timeInHours = timeInMin/60;
      
      StringBuffer result = new StringBuffer(16);
      result.append(timeInHours).append(':');
      
      if (minutes<10) result.append('0');
      result.append(minutes).append(':');
      
      if (seconds<10) result.append('0');
      result.append(seconds).append(".");
      
      if (millis<10) result.append("00");
      else if (millis<100) result.append('0');
      result.append(millis);

      return result.toString();
	}
}
