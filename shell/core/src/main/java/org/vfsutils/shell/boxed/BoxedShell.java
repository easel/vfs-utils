package org.vfsutils.shell.boxed;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandParser;
import org.vfsutils.shell.MultilineCommandParser;
import org.vfsutils.shell.Arguments.Option;
import org.vfsutils.shell.Arguments.OptionMap;

public class BoxedShell extends org.vfsutils.shell.Shell {

	public BoxedShell(InputStream in, String path, boolean askUsername, boolean askPassword, boolean askDomain, boolean virtual, Map options) throws FileSystemException {		
		this.engine = new BoxedEngine(this, new BoxedCommandRegistry(), VFS.getManager());
		this.reader = new InputStreamReader(in);
		customizeEngine(engine);
		loadRc();
		
		((BoxedEngine) this.engine).setStartDir(path, askUsername, askPassword, askDomain, virtual, options);
	}
	
	public static void main(String[] args) {
		

		CommandParser parser = new MultilineCommandParser();
		Arguments arguments = parser.parse(args);
		
		try {
			BoxedShell shell = new BoxedShell(System.in, arguments.getCmd(), 
					arguments.hasFlag("u"),
					arguments.hasFlag("p"),
					arguments.hasFlag("d"),
					arguments.hasFlag("virtual"),
					transformOptions(arguments.getOptions()));
			
			shell.go();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		 
	}

	protected static Map transformOptions(OptionMap options) {
		Map result = new TreeMap();
		Iterator iterator = options.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Option option = (Option) options.get(key);
			result.put(option.getName(), option.getValue());
		}
		return result;
	}
}
