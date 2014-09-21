package org.jake.java;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jake.JakeClassLoader;
import org.jake.JakeDirSet;
import org.jake.JakeLog;
import org.jake.JakeOptions;
import org.jake.JakeZip;
import org.jake.utils.JakeUtilsFile;
import org.jake.utils.JakeUtilsReflect;

public final class JakeJavadoc {

	private static final String JAVADOC_MAIN_CLASS_NAME = "com.sun.tools.javadoc.Main";

	private final JakeDirSet srcDirs;

	private final String extraArgs;

	private final Class<?> doclet;

	private final Iterable<File> classpath;

	private JakeJavadoc(JakeDirSet srcDirs, Class<?> doclet, Iterable<File> classpath, String extraArgs) {
		this.srcDirs = srcDirs;
		this.extraArgs = extraArgs;
		this.doclet = doclet;
		this.classpath = classpath;
	}

	public static JakeJavadoc of(JakeDirSet sources) {
		return new JakeJavadoc(sources, null, null, "");
	}

	public JakeJavadoc withDoclet(Class<?> doclet) {
		return new JakeJavadoc(srcDirs, doclet, classpath, extraArgs);
	}

	public JakeJavadoc withClasspath(Iterable<File> classpath) {
		return new JakeJavadoc(srcDirs, doclet, classpath, extraArgs);
	}

	private void doProcess(File outputDir) {
		JakeLog.startAndNextLine("Generating javadoc");
		final String[] args = toArguments(outputDir);
		execute(doclet, JakeLog.infoStream(),JakeLog.warnStream(),JakeLog.errorStream(), args);
	}

	public void processAndZip(File outputDir, File zip) {
		doProcess(outputDir);
		if (outputDir.exists()) {
			JakeZip.of(outputDir).create(zip);
		}
		JakeLog.done();
	}

	public void process(File outputDir) {
		doProcess(outputDir);
		JakeLog.done();
	}


	private String[] toArguments(File outputDir) {
		final List<String> list = new LinkedList<String>();
		list.add("-sourcepath");
		list.add(JakeUtilsFile.toPathString(this.srcDirs.listRoots(), ";"));
		list.add("-d");
		list.add(outputDir.getAbsolutePath());
		if (JakeOptions.isVerbose()) {
			list.add("-verbose");
		} else {
			list.add("-quiet");
		}
		list.add("-docletpath");
		list.add(JakeUtilsJdk.toolsJar().getPath());
		if (classpath != null && classpath.iterator().hasNext()) {
			list.add("-classpath");
			list.add(JakeUtilsFile.toPathString(this.classpath, ";"));
		}
		if (!this.extraArgs.trim().isEmpty()) {
			final String[] extraArgs = this.extraArgs.split(" ");
			list.addAll(Arrays.asList(extraArgs));
		}


		for (final File sourceFile : this.srcDirs.listFiles()) {
			if (sourceFile.getPath().endsWith(".java")) {
				list.add(sourceFile.getAbsolutePath());
			}

		}
		return list.toArray(new String[0]);
	}


	private static void execute(Class<?> doclet, PrintStream normalStream, PrintStream warnStream, PrintStream errorStream, String[] args) {

		final String docletString = doclet != null ? doclet.getName() : "com.sun.tools.doclets.standard.Standard";
		final Class<?> mainClass = getJavadocMainClass();
		JakeUtilsReflect.newInstance(mainClass);
		final Method method = JakeUtilsReflect.getMethod(mainClass, "execute", String.class, PrintWriter.class, PrintWriter.class, PrintWriter.class, String.class, new String[0].getClass());
		JakeUtilsReflect.invoke(null, method, "Javadoc", new PrintWriter(errorStream), new PrintWriter(warnStream),
				new PrintWriter(normalStream), docletString, args);
	}

	public static Class<?> getJavadocMainClass() {
		final JakeClassLoader classLoader = JakeClassLoader.current();
		Class<?> mainClass = classLoader.loadIfExist(JAVADOC_MAIN_CLASS_NAME);
		if (mainClass == null) {
			classLoader.addEntry(JakeUtilsJdk.toolsJar());
			mainClass = classLoader.loadIfExist(JAVADOC_MAIN_CLASS_NAME);
			if (mainClass == null) {
				throw new RuntimeException("It seems that you are running a JRE instead of a JDK, please run Jake using a JDK.");
			}
		}
		return mainClass;
	}


}
