package org.vfsutils.shell.commands;

import java.math.BigInteger;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.Md5.Md5FileInfo;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Md5 extends AbstractCommand {
	
	protected org.vfsutils.Md5 helper;
	
	public Md5() {
		super("md5", "Calculate md5 checksum", "(<path> | -s <input>+) --checksum=<code> | -c <md5_path> ");
		this.helper = new org.vfsutils.Md5();
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(1);
		
		BigInteger checksum = null;
		if (args.hasOption("checksum")) {
			try {
				checksum = this.helper.fromString(args.getOption("checksum"));
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid checksum given");
			}
		}
		
		if (args.hasFlag('s')) {
			for (int i=0; i<args.size(); i++) {
				md5(args.getArgument(i), checksum, engine);
			}			
		}
		else if (args.hasFlag("c")) {
			FileObject[] files = engine.pathToFiles(args.getArgument(0));
			md5(files, true, engine);
		}
		else {
			FileObject[] files = engine.pathToFiles(args.getArgument(0));
			md5(files, checksum, engine);			
		}		
	}
	
	public void md5(FileObject[] files, BigInteger checksum, Engine engine) throws FileSystemException, CommandException {
		for (int i=0; i<files.length; i++) {
			FileObject file = files[i];
			engine.println(engine.toString(file));
			if (file.getType().equals(FileType.FOLDER)) {
				engine.error("You cannot calculate md5 on a directory");
			}
			else {
				md5(file, checksum, engine);
			}
		}
	}
	
	public void md5(FileObject[] files, boolean readMd5FromFile, Engine engine) throws FileSystemException, CommandException {
		for (int i=0; i<files.length; i++) {
			FileObject file = files[i];
			engine.println(engine.toString(file));
			if (file.getType().equals(FileType.FOLDER)) {
				engine.error("You cannot calculate md5 on a directory");
			}
			else {
				md5FromFile(file, engine);
			}
		}
	}
	
	public void md5(FileObject file, BigInteger checksum, Engine engine) throws CommandException, FileSystemException {		
		BigInteger bigInt = this.helper.calculateMd5(file);
		engine.println("MD5: " + this.helper.toString(bigInt));
		if (checksum!=null) {
			engine.println("Checksum is " + (bigInt.equals(checksum)?"identical":"different"));
		}
	}

	public void md5(String inputString, BigInteger checksum, Engine engine) throws CommandException, FileSystemException {
		engine.println(inputString);
		BigInteger bigInt = this.helper.calculateMd5(inputString);
		engine.println("MD5: " + this.helper.toString(bigInt));
		if (checksum!=null) {
			engine.println("Checksum is " + (bigInt.equals(checksum)?"identical":"different"));
		}
	}
	
	public void md5FromFile(FileObject checksumFile, Engine engine) throws CommandException, FileSystemException {
		
		
		Md5FileInfo info = this.helper.parseMd5File(checksumFile);
		
		BigInteger checksum = this.helper.fromString(info.checksum);
		
		String fileName;
		if (info.fileName != null) {
			fileName = info.fileName;
		}
		else if (info.fileName==null && checksumFile.getName().getExtension().equals("md5")) {
			//remove the .md5 from the checksum file
			String baseName = checksumFile.getName().getBaseName();
			fileName = baseName.substring(0, baseName.length()-4);
		}
		else {
			throw new FileSystemException(new IllegalArgumentException("Target file name can not be deduced"));
		}
				
		FileObject file = checksumFile.getParent().resolveFile(fileName);
		
		if (!file.exists()) {
			throw new CommandException("File " + engine.toString(file) + " does not exist ");
		}
		
		BigInteger bigInt = this.helper.calculateMd5(file);
		engine.println("Checksum: " + this.helper.toString(checksum));
		engine.println("Target file: " + engine.toString(file));
		engine.println("MD5: " + this.helper.toString(bigInt));
		engine.println("Checksum is " + (bigInt.equals(checksum)?"identical":"different"));
		
	}
	
	public void setMd5Helper(org.vfsutils.Md5 helper) {
		this.helper = helper;
	}

}
