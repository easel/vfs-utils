package org.vfsutils.shell.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;
import org.vfsutils.xml.sax.CollectingErrorHandler;
import org.vfsutils.xml.sax.DocTypeParser;
import org.vfsutils.xml.sax.VfsInputSource;
import org.vfsutils.xml.transform.VfsSaxSource;
import org.vfsutils.xml.transform.VfsStreamSource;
import org.xml.sax.SAXParseException;

public class XmlValidate extends AbstractCommand {
	
	private Map schemaMap = new HashMap();
	private Map cachedSchemas = new HashMap();
	
	public XmlValidate() {
		super("xml-validate", "Validates XML against a schema. \n" +
				"With map you can map the systemId, publicId, name or root to a schema location. \n" +
				"With usemap you activate the map during a validation. \n" +
				"With strict warnings also invalidate a document. \n" +
				"With copyto you can copy invalid documents.", 
				"<xmlPath> [--schema=<schemaPath> --usemap --copyto=<folderpath> --strict -f] | map [<key> <schemaPath>]");
		
		// add a null reference for null keys
		this.schemaMap.put(null, null);
		
	}

	
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
					
		args.assertSize(1);
		
		if (args.getArgument(0).equals("map") && args.getArguments().size()==1) {
			printMap(engine);
		} else if (args.getArgument(0).equals("map")) {
			args.assertSize(3);
			map(args.getArgument(1), args.getArgument(2), engine);
		} else {
			try {
			
				String schemaFilePath = args.getOption("schema");
				boolean automap = args.hasFlag("usemap");
				boolean failOnError = args.hasFlag("f");
				boolean strict = args.hasFlag("strict");
						
				FileObject[] files = engine.pathToFiles(args.getArgument(0));
				
				FileObject schema = null;
				
				if (!automap && schemaFilePath!=null) {
					schema = engine.pathToExistingFile(schemaFilePath);
				}
				
				FileObject copyToFolder = (args.hasOption("copyto") ? engine.pathToExistingFile(args.getOption("copyto")): null);
				
				verify(files, schema, automap, failOnError, strict, copyToFolder, engine);
				
			} finally {
				this.cachedSchemas.clear();
			}
		}
    }
	
	public void printMap(Engine engine) {
		Iterator iterator = this.schemaMap.keySet().iterator();
		engine.println("Schema mapping: ");
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			if (key!=null) {
				engine.println(key + " - " + this.schemaMap.get(key));
			}
		}
	}
	
	public void map(String key, String schemaLocation, Engine engine) {
		this.schemaMap.put(key, schemaLocation);
	}
	
	public void verify(FileObject[] files, FileObject schemaFile, boolean automap, boolean failOnError, boolean strict, FileObject copyToFolder, Engine engine) throws CommandException, FileSystemException {
		Schema schema = (automap ? null : getSchema(schemaFile));
		verify(files, schema, automap, failOnError, strict, copyToFolder, engine);
	}
	
	public void verify(FileObject[] files, Schema schema, boolean automap, boolean failOnError, boolean strict, FileObject copyToFolder, Engine engine) throws CommandException, FileSystemException {
		for (int i = 0; i<files.length; i++) {
			FileObject file = files[i];
			try {
				verify(file, schema, automap, strict, copyToFolder, engine);
			}
			catch (Exception e) {
				
				if (failOnError) {
					if (e instanceof CommandException) {
						throw (CommandException) e;
					} else if (e instanceof FileSystemException) {
						throw (FileSystemException) e;
					} else {
						throw new CommandException(e);
					}
				}
				else {
					engine.println(e.getMessage());
				}
			}
		}
	}
	
	public void verify(FileObject file, FileObject schemaFile, boolean automap, boolean strict, FileObject copyToFolder, Engine engine) throws CommandException, FileSystemException {
		Schema schema = (automap ? null : getSchema(schemaFile));
        verify(file, schema, automap, strict, copyToFolder, engine);	
	}
	
	public void verify(FileObject file, Schema schema, boolean automap, boolean strict, FileObject copyToFolder, Engine engine) throws CommandException, FileSystemException, IllegalArgumentException {
		
		if (schema==null && automap) {
			schema = getMappedSchema(file, engine);
		}
		
		try {
			CollectingErrorHandler errorHandler = newErrorHandler();
			Validator validator = schema.newValidator();
			validator.setErrorHandler(errorHandler);
			validator.validate(new VfsSaxSource(new VfsInputSource(file)));
			handleDocument(file, errorHandler, strict, copyToFolder, engine);
	    } catch (Exception e) {
	        throw new CommandException("Error validating xml", e);
	    }         
	}
	
	protected Schema getSchema(FileObject schemaFile) throws CommandException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
        	
            Schema schema;
            if (schemaFile!=null) {
            	Source schemaSource = new VfsStreamSource(schemaFile);
            	schema = schemaFactory.newSchema(schemaSource);
            }
            else {
            	schema = schemaFactory.newSchema();
            }
            return schema;
        } catch (Exception e) {
            throw new CommandException("Unable to create xml verifier", e);
        }
	}
	
	protected Schema getMappedSchema(FileObject xmlFile, Engine engine) throws CommandException, FileSystemException, IllegalArgumentException {
		
		DocTypeParser dtParser = new DocTypeParser();
		DocTypeParser.DocType docType;
		try {
			docType = dtParser.getDocType(xmlFile);
		} catch (Exception e) {
			throw new CommandException("Could not parse xml");
		} 
	
		
		
		String schemaLocation = (String) this.schemaMap.get(docType.getSystemId());
		if (schemaLocation==null) {
			schemaLocation = (String) this.schemaMap.get(docType.getPublicId());
		}
		if (schemaLocation==null) {
			schemaLocation = (String) this.schemaMap.get(docType.getName());
		}
		if (schemaLocation==null) {
			schemaLocation = (String) this.schemaMap.get(docType.getRootElement());
		}
		if (schemaLocation==null) {
			throw new CommandException("Could not find schema");
		}
		
		Schema schema = (Schema) this.cachedSchemas.get(schemaLocation);
		
		if (schema==null) {
			FileObject schemaFile = engine.pathToExistingFile(schemaLocation);
			schema = getSchema(schemaFile);
		}
		
		return schema;
		
	}
	
	/**
	 * Hook for customized error handling
	 * @return
	 */
	protected CollectingErrorHandler newErrorHandler() {
		return new CollectingErrorHandler();
	}
	
	/**
	 * Hook for customized processing, by default it will print the errors.
	 * @param file
	 * @param errorHandler
	 * @param engine
	 * @throws FileSystemException 
	 */
	protected void handleDocument(FileObject file, CollectingErrorHandler errorHandler, boolean strict, FileObject copyToFolder, Engine engine) throws FileSystemException {
		
		if (isInvalid(errorHandler, strict)) {
			engine.println(engine.toString(file) + " is invalid");
			printErrors(errorHandler, engine);
			
			if (copyToFolder!=null) {
				String relativePath = engine.getCwd().getName().getRelativeName(file.getName());
				FileObject virtualFile = copyToFolder.resolveFile(relativePath);
				virtualFile.copyFrom(file, Selectors.SELECT_SELF);
			}
		}
	}
	
	protected boolean isInvalid(CollectingErrorHandler errorHandler, boolean strict) {
		boolean isInvalid = errorHandler.hasFatalErrors() || errorHandler.hasErrors();
		if (!isInvalid && strict) {
			isInvalid = errorHandler.hasWarnings();
		}
		return isInvalid;
	}
	
	protected void printErrors(CollectingErrorHandler errorHandler, Engine engine) {
		
		List fatalErrors = errorHandler.getFatalErrors();
		if (fatalErrors.size()>0) {
			engine.println("Fatal errors: ");
			for (int i=0; i<fatalErrors.size();i++) {
				SAXParseException e = (SAXParseException) fatalErrors.get(i);
				printError(e, i+1, engine);
			}
		}
		
		List errors = errorHandler.getErrors();
		if (errors.size()>0) {
			engine.println("Errors: ");
			for (int i=0; i<errors.size();i++) {
				SAXParseException e = (SAXParseException) errors.get(i);
				printError(e, i+1, engine);
			}
		}
		
		List warnings = errorHandler.getWarnings();
		if (warnings.size()>0) {
			engine.println("Warnings: ");
			for (int i=0; i<warnings.size();i++) {
				SAXParseException e = (SAXParseException) warnings.get(i);
				printError(e, i+1, engine);
			}
		}
	}
	
	protected void printError(SAXParseException e, int index, Engine engine) {
		engine.println(padBefore(Integer.toString(index),4) + ": " + e.getMessage() + " [" + e.getLineNumber() + "," + e.getColumnNumber()+"]");	
	}
	
	private String padBefore(String value, int size) {
		String padding = "                                                                                                                            ";
		if (value==null) {
			return padding.substring(0, size);
		} else if (size>padding.length()) {
			//security check
			return padBefore(value, padding.length());
		} else if (value.length() >= size) {
			return value;
		} else {
			return padding.substring(0, size-value.length()) + value;
		}
	}
	
}
