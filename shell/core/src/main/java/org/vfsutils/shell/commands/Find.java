package org.vfsutils.shell.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.selector.AttributeSelector;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;
import org.vfsutils.spider.VisitingSelector;

public class Find extends AbstractCommand {
	
	private class ExecSelector extends VisitingSelector {
		
		private String exec;
		private Engine engine;
		private boolean failOnError;
		
		public ExecSelector(FileSelector selector, Engine engine, String exec, boolean failOnError) {
			super(selector);
			this.engine = engine;
			this.exec = exec;
			this.failOnError = failOnError;
		}
	
		public void visitFile(FileObject file) throws Exception {
			
			try {
				if (exec.indexOf("$file")>-1) {
					Object oldValue = null;
					boolean hasOldValue = engine.getContext().isSet("file");
					if (hasOldValue) oldValue = engine.getContext().get("file");
					engine.getContext().set("file", file.getName().getPathDecoded());
					engine.handleCommand(exec);
					if (hasOldValue) {
						engine.getContext().set("file", oldValue);
					}
					else {
						engine.getContext().unset("file");
					}
				}
				else {
					engine.handleCommand(exec + " " + file.getName().getPathDecoded());
				}
			}
			catch (Exception e) {
				if (!failOnError) {
					throw e;
				}
				else {
					engine.error(e);
				}
			}
		}
	}

	public Find() {
		super("find", "Execute a command for the selected files", "<pattern> --exec=<command> [-dfC] [(--files|--folders)] [--size=<expression>] [--age=<expression>] [--attrs=[<name><==|!=><value>]]");
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(1);
		
		String exec;
		if (args.hasOption("exec")){
			exec = args.getOption("exec");
		}
		else {
			exec = "echo";
		}
		
		boolean depthFirst = args.hasFlag('d');
		boolean failOnError = args.hasFlag('f');
		boolean caseSensitive = !args.hasFlag('C');
		String pathPattern = args.getArgument(0);
				
		AttributeSelector selector = new AttributeSelector();
		selector.setName(pathPattern);
		selector.setCasesensitive(caseSensitive);
		
		if (args.hasOption("age")){
			selector.setAge(args.getOption("age"));
		}
		if (args.hasOption("size")) {
			selector.setSize(args.getOption("size"));
		}
		if (args.hasOption("attrs")) {
			selector.setAttributes(args.getOption("attrs"));
		}
		if (args.hasOption("maxdepth")) {
			selector.setMaxDepth(Integer.parseInt(args.getOption("maxdepth")));
		}	
		if (args.hasFlag("folders") && !args.hasFlag("files")) {
			selector.setIncludeFiles(false);
		}
		else if (args.hasFlag("files") && !args.hasFlag("folders")) {
			selector.setIncludeFolders(false);
		}
		
		VisitingSelector execSelector = new ExecSelector(selector, engine, exec, failOnError);
		
		List selected = new ArrayList();
		engine.getCwd().findFiles(execSelector, depthFirst, selected);
		
	}
	
	

}
