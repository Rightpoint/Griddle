package com.raizlabs;

import org.gradle.api.Project

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
        File librariesFile = project.file('libs/')
        if (librariesFile && librariesFile.isDirectory()) {
            printLog "Found libs directory. Tracing through files"
            File[] libs = librariesFile.listFiles();
            if (libs) {
                libs.each { File file ->
                    printLog("Found: " + file.name);

                    if (file.isFile() && file.name.endsWith('.jar')) {
                        if (!mJars.contains(file.name)) {
                            printLog "*******Found Jar***** : ${file.name}"
                            mJars.add(file.name)
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
        project.dependencies.add("compile", project.fileTree("libs").include("*.jar"));
    }

    /**
     * Compiles a jar in the default "\libs" directory.
     * @param jarName The name of the jar file without the extension .jar
     */
    public void jar(String jarName) {
        jar("compile", jarName);
    }

    /**
     * This method will see if the jar exists within the 'libs' dir, if so it will use the local version.
     * If not, it will reference the specified artifactName remotely.
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param jarName         the name of the jar file without the extension
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void jar(String compilationMode, String jarName) {
        String fileName = jarName.concat(".jar")

        // Local dependency
        if (mJars.contains(fileName)) {
            project.dependencies.add(compilationMode, getProject().files("libs/" + fileName))
            printLog "Compiling local jar: " + jarName
        } else {
            printLog "Could not find local jar: ${jarName}"
        }
    }
}
