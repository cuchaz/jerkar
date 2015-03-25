package org.jake.java.eclipse;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jake.JakeDirSet;
import org.jake.depmanagement.JakeDependencies;
import org.jake.java.build.JakeJavaBuild;
import org.junit.Test;

public class DotClasspathTest {

	private static final String SAMPLE_NAME = "classpath";

	@Test
	public void testFromFile() throws URISyntaxException {
		sample();
	}

	@Test
	public void testSourceDirs() throws URISyntaxException {
		final JakeDirSet dirSet = sample().sourceDirs(structure(), Sources.ALL_PROD).prodSources;
		assertEquals(2, dirSet.jakeDirs().size());
	}

	@Test
	public void testLibs() throws URISyntaxException {
		final List<Lib> libs = sample().libs(structure(), Lib.SMART_LIB);
		assertEquals(5, libs.size());
	}

	@Test
	public void testToDependencies() throws URISyntaxException {
		final List<Lib> libs = sample().libs( structure(), Lib.SMART_LIB);
		assertEquals(5, libs.size());

		final JakeDependencies deps = Lib.toDependencies(null, libs, Lib.SMART_LIB);


		assertEquals(0, deps.dependenciesDeclaredWith(JakeJavaBuild.TEST).size());
		//final JakeFilesDependency filesDependency = (JakeFilesDependency) deps.dependenciesDeclaredWith(JakeJavaBuild.TEST).iterator().next();
		//assertEquals(1, filesDependency.files().size());
	}

	private DotClasspath sample() throws URISyntaxException {
		final URL sampleFileUrl = DotClasspathTest.class.getResource("samplestructure/" + SAMPLE_NAME);
		final File sampleFile = new File(sampleFileUrl.toURI().getPath());
		return DotClasspath.from(sampleFile);
	}

	private File structure() throws URISyntaxException {
		final URL sampleFileUrl = DotClasspathTest.class.getResource("samplestructure/" + SAMPLE_NAME);
		return new File(sampleFileUrl.toURI().getPath()).getParentFile();
	}

}
