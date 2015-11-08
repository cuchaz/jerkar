package org.jerkar.api.tooling;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jerkar.api.depmanagement.JkDepExclude;
import org.jerkar.api.depmanagement.JkDependencyExclusions;
import org.jerkar.api.depmanagement.JkModuleId;
import org.jerkar.api.depmanagement.JkRepo;
import org.jerkar.api.depmanagement.JkRepos;
import org.jerkar.api.depmanagement.JkVersionProvider;
import org.jerkar.api.utils.JkUtilsIterable;

/**
 *
 * @author djeang
 * @formatter:off
 */
class CodeWriter {

    private final EffectivePom pom;

    private final VersionConstanter constanter;

    CodeWriter(EffectivePom effectivePom) {
	super();
	this.pom = effectivePom;
	this.constanter = VersionConstanter.of(pom.versionProvider());
    }

    public String wholeClass(String packageName, String className) {
	final StringBuilder builder = new StringBuilder()
		.append(packageDeclaration(packageName))
		.append("\n\n")
		.append(imports())
		.append("\n\n")
		.append(classDeclaration(className))
		.append("\n\n")
		.append(constants())
		.append("\n\n")
		.append(this.moduleId())
		.append("\n\n")
		.append(this.version())
		.append("\n\n")
		.append(dependencies());
	final String repos = downloadRepositories();
	if (downloadRepositories() != null) {
	    builder.append("\n\n");
	    builder.append(repos);
	}
	final String versionProvider = versionProvider();
	if (versionProvider != null) {
	    builder.append("\n\n");
	    builder.append(versionProvider);
	}
	final String exclusions = dependencyExclusions();
	if (exclusions != null) {
	    builder.append("\n\n");
	    builder.append(exclusions);
	}
	builder.append("\n\n").append(this.endClass());
	return builder.toString();


    }

    public String constants() {
	final StringBuilder builder = new StringBuilder();
	final Map<String, String> constMap = constanter.constantNameValue();
	for (final String constantName : constMap.keySet()) {
	    builder.append("    public static final String ").append(constantName)
	    .append(" = \"").append(constMap.get(constantName))
	    .append("\";\n\n");
	}
	return builder.toString();
    }

    public String packageDeclaration(String packageName) {
	return "package " + packageName + ";";
    }

    public String imports() {
	final StringBuilder builder = new StringBuilder()
		.append("import org.jerkar.api.depmanagement.JkDependencies;\n")
		.append("import org.jerkar.api.depmanagement.JkDependencyExclusions;\n")
		.append("import org.jerkar.api.depmanagement.JkModuleId;\n")
		.append("import org.jerkar.api.depmanagement.JkRepos;\n")
		.append("import org.jerkar.api.depmanagement.JkVersion;\n")
		.append("import org.jerkar.api.depmanagement.JkVersionProvider;\n")
		.append("import org.jerkar.tool.builtins.javabuild.JkJavaBuild;\n");
	return builder.toString();
    }

    public String classDeclaration(String className) {
	return new StringBuilder()
		.append("/**\n")
		.append(" * Jerkar build class (generated by Jerkar from existing pom).\n")
		.append(" * @formatter:off\n")
		.append(" */\n")
		.append("public final class " + className + " extends JkJavaBuild {").toString();
    }

    public String endClass() {
	return "}";
    }


    public String moduleId() {
	return new StringBuilder()
		.append("    @Override\n")
		.append("    public JkModuleId moduleId() {\n")
		.append("        return JkModuleId.of(\"" +  pom.groupId() + "\", \"" + pom.artifactId() + "\");\n")
		.append("    }").toString();
    }

    public String version() {
	return new StringBuilder()
		.append("    @Override\n")
		.append("    public JkVersion version() {\n")
		.append("        return JkVersion.ofName(\"" +  pom.version() + "\");\n")
		.append("    }").toString();
    }

    public String dependencies() {
	final StringBuilder builder = new StringBuilder()
		.append("    @Override\n")
		.append("    public JkDependencies dependencies() {\n")
		.append("        return ")
		.append(pom.dependencies().toJavaCode(8))
		.append("\n    }");
	return builder.toString();
    }

    public String downloadRepositories() {
	final JkRepos repos = pom.repos();
	if (repos.isEmpty()) {
	    return null;
	}
	final StringBuilder builder = new StringBuilder()
		.append("    @Override\n")
		.append("    public JkRepos downloadRepositories() {\n")
		.append("        return JkRepos.maven(");
	for (final JkRepo repo : repos) {
	    builder.append("\"").append(repo.url().toString()).append("\", ");
	}
	builder.delete(builder.length()-2, builder.length());
	builder.append(");\n");
	builder.append("    }");
	return builder.toString();
    }

    public String versionProvider() {
	final JkVersionProvider versionProvider = pom.versionProvider();
	if (versionProvider.moduleIds().isEmpty()) {
	    return null;
	}
	final StringBuilder builder = new StringBuilder()
		.append("    @Override\n")
		.append("    public JkVersionProvider versionProvider() {\n")
		.append("        return JkVersionProvider.of()");
	final List<JkModuleId> moduleIds = JkUtilsIterable.listOf(versionProvider.moduleIds());
	Collections.sort(moduleIds, JkModuleId.GROUP_NAME_COMPARATOR);
	for (final JkModuleId moduleId : moduleIds) {
	    builder.append("\n            .and(\"").append(moduleId.groupAndName())
	    .append("\", ")
	    .append(constanter.versionLiteral(moduleId))
	    .append(")");
	}
	builder.append(";\n");
	builder.append("    }");
	return builder.toString();
    }

    public String dependencyExclusions() {
	final JkDependencyExclusions exclusions = pom.dependencyExclusion();
	if (exclusions.isEmpty()) {
	    return null;
	}
	final StringBuilder builder = new StringBuilder()
		.append("    @Override\n")
		.append("    public JkDependencyExclusions dependencyExclusions() {\n")
		.append("        return JkDependencyExclusions.builder()");
	final List<JkModuleId> moduleIds = JkUtilsIterable.listOf(exclusions.moduleIds());
	Collections.sort(moduleIds, JkModuleId.GROUP_NAME_COMPARATOR);
	for (final JkModuleId moduleId : moduleIds) {
	    builder.append("\n            .on(\"").append(moduleId.groupAndName()).append("\"");
	    for (final JkDepExclude depExclude : exclusions.get(moduleId)) {
		builder.append(", \"").append(depExclude.moduleId().groupAndName()).append("\"");
	    }
	    builder.append(")");
	}
	builder.append(".build();\n");
	builder.append("    }");
	return builder.toString();
    }





}
