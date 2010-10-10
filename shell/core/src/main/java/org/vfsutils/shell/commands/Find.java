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
		private boolean execNeedsReplacement;
		
		public ExecSelector(FileSelector selector, Engine engine, String exec, boolean failOnError) {
			super(selector);
			this.engine = engine;
			this.exec = exec;
			this.execNeedsReplacement = (exec.indexOf("{abspath}")>-1 || exec.indexOf("{relpath}")>-1 || exec.indexOf("{fullpath}")>-1);
			this.failOnError = failOnError;
		}
	
		
		
		public void visitFile(FileObject file) throws Exception {
			
			try {
				if (this.execNeedsReplacement) {
					
					String command = exec;
					if (exec.indexOf("{relpath}")>-1) {
						command = command.replaceAll("\\{relpath\\}", this.engine.getCwd().getName().getRelativeName(file.getName()));
					}
					if (exec.indexOf("{abspath}")>-1) {
						command = command.replaceAll("\\{abspath\\}", file.getName().getPathDecoded());
					}
					if (exec.indexOf("{fullpath}")>-1) {
						command = command.replaceAll("\\{fullpath\\}", file.getName().getURI());
					}
					
					engine.handleCommand(command);
					
				}
				else {
					// use relative path
					engine.handleCommand(exec + " " + this.engine.getCwd().getName().getRelativeName(file.getName()));
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
		super("find", "Execute a command for the selected files", "<pattern> [--exec=<command>] [-dfC] [(--files|--folders)] [--size=<expression>] [--age=<expression>] [--attrs=[<name><==|!=><value>]]\n" + 
																  "       In exec you can use {relpath} for relative paths, {abspath} for absolute paths, and {fullpath} for the full URI");
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
