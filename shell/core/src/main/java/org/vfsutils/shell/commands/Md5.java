package org.vfsutils.shell.commands;

import java.math.BigInteger;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Md5 extends AbstractCommand {
	
	protected org.vfsutils.Md5 helper;
	
	public Md5() {
		super("md5", "Calculate md5 checksum", "<path> | -s <input>");
		this.helper = new org.vfsutils.Md5();
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(1);
		
		if (args.hasFlag('s')) {
			String inputString = args.getArgument(0);
			md5(inputString, engine);
		}
		else {
			FileObject[] files = engine.pathToFiles(args.getArgument(0));			
			md5(files, engine);			
		}		
	}
	
	public void md5(FileObject[] files, Engine engine) throws FileSystemException, CommandException {
		for (int i=0; i<files.length; i++) {
			FileObject file = files[i];
			engine.println(engine.toString(file));
			if (file.getType().equals(FileType.FOLDER)) {
				engine.error("You cannot calculate md5 on a directory");
			}
			else {
				md5(file, engine);
			}
		}
	}
	
	public void md5(FileObject file, Engine engine) throws CommandException, FileSystemException {		
		BigInteger bigInt = this.helper.calculateMd5(file);
		engine.println("MD5: " + this.helper.toString(bigInt));
	}

	public void md5(String inputString, Engine engine) throws CommandException, FileSystemException {
		engine.println(inputString);
		BigInteger bigInt = this.helper.calculateMd5(inputString);
		engine.println("MD5: " + this.helper.toString(bigInt));
	}
	
	public void setMd5Helper(org.vfsutils.Md5 helper) {
		this.helper = helper;
	}
	
	

}
