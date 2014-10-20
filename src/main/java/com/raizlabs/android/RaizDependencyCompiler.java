package com.raizlabs.android;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class RaizDependencyCompiler {

    /**
     * The project this corresponds to
     */
    private final Project mProject;

    /**
     * The modules we found
     */
    private final Set<String> mModules = new HashSet<String>();

    /**
     * The jar libraries we found
     */
    private final Set<String> mJars = new HashSet<String>();

    private final boolean printLogs;

    void printLog(String text) {
        if(printLogs) {
            System.out.println(text);
        }
    }

    /**
     * Constructs the instance of this object. It will traverse the settings.gradle file of the root project and find all
     * inclusions. It will use those to determine whether to compile locally or not.
     *
     * @param project
     */
    public RaizDependencyCompiler(Project project) {
        mProject = project;

        printLogs = project.hasProperty(RaizLibraryPlugin.PRINT_LOGS);


        Project root = project.getRootProject();

        //check the settings file for modules that we can use in compiling
        File settingsFile = root.file("settings.gradle");

        try {
            Scanner scanner = new Scanner(settingsFile);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                line = line.replace("include", "").trim();
                String[] modules = line.split(",");
                for (String module : modules) {
                    module = module.replaceAll("'", "").trim();
                    // not commented and we do not already have the module in the project
                    if(!module.startsWith("//") && !mModules.contains(module)) {
                        printLog("*******Found Module***** : " + module);
                        mModules.add(module);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        File librariesFile = project.file("libs/");
        if (librariesFile != null && librariesFile.isDirectory()) {
            printLog("Found libs directory. Tracing through files");
            File[] libs = librariesFile.listFiles();
            if (libs != null) {
                for (File file : libs) {
                    printLog("Found: " + file.getName());

                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        if(!mJars.contains(file.getName())) {
                            printLog("*******Found Jar***** : " + file.getName());
                            mJars.add(file.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the artifact name combination based on user preferences
     * @param module
     * @return
     */
    private static String getArtifactName(String module) {
        return String.format("%1s:%1s:%1s%1s", RaizLibraryPlugin.GROUP, module, "+", RaizLibraryPlugin.LIBRARY_EXTENSION);
    }

    /**
     * Returns the fully qualified local module name for the specified partial module.
     * @param module
     * @return
     */
    private static String getFullyQualifiedName(String module) {
        return ":" + RaizLibraryPlugin.LIBRARY_DIRECTORY + ":" + module;
    }

    /**
     * This will compile a whole listing of modules separated by a comma and surrounded by [].
     *
     * @param modules the list of modules we wish to apply compile to
     * @see #dependency(String)
     */
    public void dependencies(String[] modules) {
        for (String module : modules) {
            dependency(module);
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @see #dependency(String, String)
     */
    public void dependency(String module) {
        dependency(module, getArtifactName(module));
    }

    /**
     * This will use the default "compile" mechanisms for getting dependencies.
     *
     * @param module       the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #dependency(String, String, String)
     */
    public void dependency(String module, String artifactName) {
        dependency("compile", getFullyQualifiedName(module), artifactName);
    }

    /**
     * This function will compile a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param module          the name of the module, most always the fully-qualified name of the module (i.e: ":Libraries:Core"
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void dependency(String compilationMode, String module, String artifactName) {
        DependencyHandler dependencyHandler = mProject.getDependencies();

        // We found the module locally, compile it locally
        if (mModules.contains(module)) {
            printLog("Compiling local project: " + module);
            dependencyHandler.add(compilationMode, mProject.project(module));
        } else {
            // remote dependency, we will compile it using the params provided
            printLog("Compiling remote dependency: " + artifactName);
            dependencyHandler.add(compilationMode, artifactName);
        }
    }

    /**
     * This will runtime a whole listing of modules separated by a comma and surrounded by [].
     *
     * @param modules the list of modules we wish to apply compile to
     * @see #dependency(String)
     */
    public void rtDependencies(String[] modules) {
        for(String module: modules) {
            rtDependency(module);
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified
     * @param module
     */
    public void rtDependency(String module) {
        rtDependency(module, getArtifactName(module));
    }

    /**
     * This function will runtime a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *  @param module       the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void rtDependency(String module, String artifactName) {
        dependency("runtime", getFullyQualifiedName(module), artifactName);
    }

    /**
     * This function is shorthand for "compile fileTree(dir: "libs" include: "*.jar")"
     */
    public void jars() {
        DependencyHandler dependencyHandler = mProject.getDependencies();
        dependencyHandler.add("compile", mProject.fileTree("libs").include("*.jar"));
    }

    /**
     * Compiles a list of jars in the default "\libs" directory
     * @param jars The jar names to compile without the .jar prefix
     */
    public void jarDependency(String[] jars) {
        for(String jar: jars) {
            jarDependency(jar);
        }
    }

    /**
     * Compiles a jar in the default "\libs" directory.
     * @param jarName The name of the jar file without the extension .jar
     */
    public void jarDependency(String jarName) {
        jarDependency("compile", jarName, "");
    }

    /**
     * Uses the default "compile" configuration.
     *
     * @param jarName      the name of the jar file without the extension
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #dependency(String, String, String)
     */
    public void jarDependency(String jarName, String artifactName) {
        jarDependency("compile", jarName, artifactName);
    }

    /**
     * This method will see if the jar exists within the 'libs' dir, if so it will use the local version.
     * If not, it will reference the specified artifactName remotely.
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param jarName         the name of the jar file without the extension
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void jarDependency(String compilationMode, String jarName, String artifactName) {
        DependencyHandler dependencyHandler = mProject.getDependencies();

        String fileName = jarName.concat(".jar");

        // Local dependency
        if (mJars.contains(fileName)) {
            dependencyHandler.add(compilationMode, mProject.files("libs/" + fileName));
            printLog("Compiling local jar: " + jarName);
        } else {
            // Remote dependency
            dependencyHandler.add(compilationMode, artifactName);
            printLog("Compiling remote dependency: " + artifactName);
        }
    }
}
