package com.raizlabs.android;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Description:
 */
public class JarContainer extends BaseContainer {

    /**
     * The jar libraries we found
     */
    private final Set<String> mJars = new HashSet<String>();

    public JarContainer(Project project) {
        super(project);
        File librariesFile = getProject().file("libs/");
        if (librariesFile != null && librariesFile.isDirectory()) {
            printLog("Found libs directory. Tracing through files");
            File[] libs = librariesFile.listFiles();
            if (libs != null) {
                for (File file : libs) {
                    printLog("Found: " + file.getName());

                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        if (!mJars.contains(file.getName())) {
                            printLog("*******Found Jar***** : " + file.getName());
                            mJars.add(file.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * This function is shorthand for "compile fileTree(dir: "libs" include: "*.jar")"
     */
    public void jars() {
        DependencyHandler dependencyHandler = getProject().getDependencies();
        dependencyHandler.add("compile", getProject().fileTree("libs").include("*.jar"));
    }

    /**
     * Compiles a list of jars in the default "\libs" directory
     * @param jars The jar names to compile without the .jar prefix
     */
    public void jar(String[] jars) {
        for(String jar: jars) {
            jar(jar);
        }
    }

    /**
     * Compiles a jar in the default "\libs" directory.
     * @param jarName The name of the jar file without the extension .jar
     */
    public void jar(String jarName) {
        jar("compile", jarName, "");
    }

    /**
     * Uses the default "compile" configuration.
     *
     * @param jarName      the name of the jar file without the extension
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #dependency(String, String, String)
     */
    public void jar(String jarName, String artifactName) {
        jar("compile", jarName, artifactName);
    }

    /**
     * This method will see if the jar exists within the 'libs' dir, if so it will use the local version.
     * If not, it will reference the specified artifactName remotely.
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param jarName         the name of the jar file without the extension
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void jar(String compilationMode, String jarName, String artifactName) {
        DependencyHandler dependencyHandler = getProject().getDependencies();

        String fileName = jarName.concat(".jar");

        // Local dependency
        if (mJars.contains(fileName)) {
            dependencyHandler.add(compilationMode, getProject().files("libs/" + fileName));
            printLog("Compiling local jar: " + jarName);
        } else {
            // Remote dependency
            dependencyHandler.add(compilationMode, artifactName);
            printLog("Compiling remote dependency: " + artifactName);
        }
    }
}
