package org.jake.java.eclipse;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class EclipseClasspath {
	
	private final List<ResourcePath> libEntries;

	private EclipseClasspath(Document document, File jakeJarFile) {
		super();
		this.libEntries = getLibClasspathEntry(jakeJarFile, document);
		
	}
	
	public static EclipseClasspath fromFile(File classpathFile, File jakeJarFile) {
		final Document document = getDotClassPathAsDom(classpathFile);
		return new EclipseClasspath(document, jakeJarFile);
	}
	
	private static List<ResourcePath> getLibClasspathEntry(File jakeJarFile, Document document) {
		final List<ResourcePath> result = new LinkedList<ResourcePath>();
		final NodeList nodeList = document.getElementsByTagName("classpathentry");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			final Element element = (Element) node;
			String kind = element.getAttribute("kind");
			if (kind.equals("lib")) {
				final String path = element.getAttribute("path");
				final ResourcePath resourceLocation = ResourcePath.fromClasspathEntryLib(path, false);
				result.add(resourceLocation);
			} else if (kind.equals("con")) {
				final String path = element.getAttribute("path");
				final List<ResourcePath> resourceLocations = ResourcePath.fromClasspathEntryCon(jakeJarFile, path);
				result.addAll(resourceLocations);
			
				
				
				
			}
		}
		return result;
	} 
	
	private static Document getDotClassPathAsDom(File classpathFile) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		if (!classpathFile.exists()) {
			throw new IllegalStateException(".classpath file not found.");
		}
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(classpathFile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			throw new RuntimeException("Error while parsing .classpath file : ", e);
		
		}
	}

	public List<ResourcePath> getLibEntries() {
		return libEntries;
	}
	
	
	
	
}