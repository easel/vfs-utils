package org.vfsutils.shell.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;
import org.vfsutils.xml.VfsResolver;
import org.vfsutils.xml.sax.VfsEntityResolver;
import org.vfsutils.xml.sax.VfsInputSource;
import org.vfsutils.xml.transform.VfsDomSource;
import org.vfsutils.xml.transform.VfsSaxSource;
import org.vfsutils.xml.transform.VfsStreamSource;
import org.vfsutils.xml.transform.VfsUriResolver;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Xslt extends AbstractCommand {

	protected class CacheEntry {

		private long modified = 0;
		private Templates templates = null;

		protected CacheEntry(Templates templates, long modified) {
			this.templates = templates;
			this.modified = modified;
		}

	}

	public Xslt() {
		super(
				"xslt",
				"Performs an XSL transformation. If [out] is not set the result will be printed to the console. "
						+ "All options are set as parameters. Activate xsl caching with c. Default mode is sax. Mode stream " 
						+ "does not allow VFS enabled entity resolving. ",
				"<in> <xsl> [<out>] [-c] [--param1=x --param2=y] [--sax|--dom|--stream]");
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(2);

		FileObject in = engine.pathToExistingFile(args.getArgument(0));
		FileObject xsl = engine.pathToExistingFile(args.getArgument(1));
		FileObject result = null;
		if (args.size() > 2) {
			result = engine.pathToFile(args.getArgument(2));
		}

		Map params = new HashMap();
		Iterator iterator = args.getOptions().keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = args.getOption(key);
			params.put(key, value);
		}

		boolean useCache = args.hasFlag('c');

		String mode = "sax";
		if (args.hasFlag("stream")) {
			mode = "stream";
		} else if (args.hasFlag("dom")) {
			mode = "dom";
		}

		xsl(in, xsl, result, params, mode, useCache, engine);
	}

	public void xsl(FileObject in, FileObject xsl, FileObject out, Map params,
			String mode, boolean cache, Engine engine) throws CommandException {

		try {

			long timestamp = System.currentTimeMillis();
			// check type of out, if directory fail otherwise a folder might be
			// converted to a file
			if (out != null && out.exists()
					&& out.getType().equals(FileType.FOLDER)) {
				throw new IllegalArgumentException(
						"Invalid out parameter: file exists and is a folder");
			}

			// generate a Transformer.
			Transformer transformer;
			if (cache) {
				Map xslCache = getTemplateCache(engine);
				String key = engine.toString(xsl);
				CacheEntry cacheEntry = (CacheEntry) xslCache.get(key);
				Templates templates;
				if (cacheEntry == null
						|| cacheEntry.modified != xsl.getContent()
								.getLastModifiedTime()) {
					templates = newTemplates(xsl, mode, engine);
					cacheEntry = new CacheEntry(templates, xsl.getContent()
							.getLastModifiedTime());
					xslCache.put(key, cacheEntry);
				} else {
					templates = cacheEntry.templates;
				}
				transformer = templates.newTransformer();
			} else {
				Templates templates = newTemplates(xsl, mode, engine);
				transformer = templates.newTransformer();
			}

			engine.println("Stylesheet compilation took "
					+ (System.currentTimeMillis() - timestamp) + " ms");

			// set params
			if (params != null) {
				Iterator iterator = params.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					Object value = params.get(key);
					transformer.setParameter(key, value);
				}
			}

			Source xml = asSource(in, mode, engine);

			StreamResult result = null;
			try {
				if (out == null) {
					result = new StreamResult(engine.getConsole().getOut());
				} else {
					result = new StreamResult(out.getContent()
							.getOutputStream());
				}

				timestamp = System.currentTimeMillis();
				transformer.transform(xml, result);
			} finally {
				engine.println("Transformation took "
						+ (System.currentTimeMillis() - timestamp) + " ms");

				// close the result
				if (out != null && result != null
						&& result.getOutputStream() != null)
					result.getOutputStream().close();
				if (out != null && result != null && result.getWriter() != null)
					result.getWriter().close();
			}
		} catch (Exception e) {
			throw new CommandException(e);
		}
	}

	protected Source asSource(FileObject file, String mode, Engine engine)
			throws ParserConfigurationException, SAXException, IOException {
		if ("stream".equals(mode)) {
			return new VfsStreamSource(file);
		} else if ("dom".equals(mode)) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			// set the custom entity resolver
			builder.setEntityResolver(newEntityResolver(file, engine));

			Document doc = builder.parse(new VfsInputSource(file));

			return new VfsDomSource(doc, file.getName());

		} else {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			// set the custom entity resolver
			reader.setEntityResolver(newEntityResolver(file, engine));

			return new VfsSaxSource(reader, new VfsInputSource(file));
		}
	}

	protected Templates newTemplates(FileObject xsl, String mode, Engine engine)
			throws TransformerConfigurationException,
			ParserConfigurationException, SAXException, IOException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		// initialize context of URI resolver to XSL parent folder
		tFactory.setURIResolver(newUriResolver(xsl, engine));

		Templates templates = tFactory
				.newTemplates(asSource(xsl, mode, engine));
		return templates;
	}

	protected Map getTemplateCache(Engine engine) {
		Map cache = (Map) engine.getContext().get("vfs.xslcache");
		if (cache == null) {
			cache = new HashMap();
			engine.getContext().set("vfs.xslcache", cache);
		}
		return cache;
	}

	protected URIResolver newUriResolver(FileObject xsl, Engine engine)
			throws FileSystemException {
		return new VfsUriResolver(newVfsResolver(xsl.getParent(), engine));
	}

	protected EntityResolver newEntityResolver(FileObject xml, Engine engine)
			throws FileSystemException {
		return new VfsEntityResolver(newVfsResolver(xml.getParent(), engine));
	}

	protected VfsResolver newVfsResolver(FileObject file, Engine engine)
			throws FileSystemException {
		return new VfsResolver(file.getParent());
	}
}
