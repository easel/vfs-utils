package org.vfsutils.shell.commands;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Sysinfo extends AbstractCommand {

	public Sysinfo() {
		super("sysinfo", "Shows system information such as memory, threads and properties", "[-mtpa] [--finalize] [--gc]");
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		if (args.getFlags().isEmpty()) {
			//show all high level info
			memory(false, false, engine);
			processors(engine);
			threads(false, engine);
			properties(false, engine);
		}
		else if (args.hasFlag('a')) {
			//show all details
			memory(args.hasFlag("finalize"), args.hasFlag("gc"), engine);
			processors(engine);
			threads(true, engine);
			properties(true, engine);
		}
		else {
			//only show info related to flag
			if (args.hasFlag("m") || args.hasFlag("finalize") || args.hasFlag("gc")) {
				memory(args.hasFlag("finalize"), args.hasFlag("gc"), engine);
			}
			if (args.hasFlag('t')) {
				threads(true, engine);
			}
			if (args.hasFlag('p')) {
				properties(true, engine);
			}
		}
	}

	public void memory(boolean finalize, boolean gc, Engine engine) {
		Runtime runtime = Runtime.getRuntime();
		
		if (finalize) {
			runtime.runFinalization();
			engine.println("Ran finalization");
		}
		
		if (gc) {
			long before = runtime.freeMemory();
			runtime.gc();
			long after = runtime.freeMemory();
			
			engine.println("GC freed " + toXByteString(after-before));
		}
		
		engine.println("Memory: " + toXByteString(runtime.freeMemory()) 
				+ " of " + toXByteString(runtime.totalMemory()) + " available" 
				+ " (max: " + toXByteString(runtime.maxMemory()) +")");
	}
	
	public void processors(Engine engine) {
		engine.println("Processors: " + Runtime.getRuntime().availableProcessors());
	}
	
	public void threads(boolean details, Engine engine) {
		
		ThreadGroup root = getRootThreadGroup();
		
		if (details) {
			engine.println("Threads per group: name (priority)");
			listThreads(root, 0, engine);
		}
		else {
			engine.println("Threads: " + root.activeCount());
		}
	}

	protected void listThreads(ThreadGroup group, int level, Engine engine) {
		
		String pad = "                                                                                                 ";
		
		engine.println(pad.subSequence(0, level*2) + "[Group] " + group.getName());
		
		Thread[] threads = new Thread[group.activeCount()+50];
		int nrOfThreads = group.enumerate(threads, false);
		for (int i=0; i<nrOfThreads; i++) {
			Thread t = threads[i];
			engine.println(pad.substring(0, (level+1)*2)
					+ "[Thread] "
					+ t.getName() 
					+ " (" + t.getPriority() + ")" 
					+ (t.isDaemon()?" Daemon":"" + ""));
		}
		ThreadGroup[] groups = new ThreadGroup[group.activeGroupCount() + 50];
		int nrOfGroups = group.enumerate(groups, false);
		for (int i=0; i<nrOfGroups; i++) {
			ThreadGroup g = groups[i];
			listThreads(g, level+1, engine);
		}
	}
	
	protected ThreadGroup getRootThreadGroup() {
		ThreadGroup root = Thread.currentThread().getThreadGroup();
		
		while (root.getParent()!=null) {
			root = root.getParent();
		}
		
		return root;
	}
	
	public void properties(boolean all, Engine engine) {
		engine.println("Properties:");
		Properties p = java.lang.System.getProperties();
		
		if (!all) {
			engine.println("  Java: " + p.getProperty("java.version") + " " + p.getProperty("java.vendor"));
			engine.println("  JRE: " + p.getProperty("java.runtime.name") + " (build " + p.getProperty("java.runtime.version") + ")" );
			engine.println("  JVM: " + p.getProperty("java.vm.name") + " (build " + p.getProperty("java.vm.version") + ", " + p.getProperty("java.vm.info") + ")" );
			engine.println("  OS: " + p.getProperty("os.name") + " " + p.getProperty("os.version") + " (" + p.getProperty("os.arch") + ")");
		}
		else {
			Iterator iterator = p.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				engine.println("  " + key + ": " + p.getProperty(key));
			}
		}
	}
	
	protected String toXByteString(long bytes) {
		
		float value = 0;
		String unit = "bytes";
		
		if (bytes > 1024 * 1024) {
			value = (float) bytes / (1024*1024);
			unit = "MB";
		}
		else if (bytes > 1024) {
			value = (float) bytes / 1024;
			unit = "KB";
		}
		
		return new DecimalFormat("0.##").format((double)value) + " " + unit;
	}
}
