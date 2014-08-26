package com.raizlabs.android;

import groovy.lang.Closure;
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

    private final Project mProject;

    private final Set<String> mModules = new HashSet<String>();

    private final Set<String> mLibraries = new HashSet<String>();

    /**
     * Constructs the instance of this object. It will traverse the settings.gradle file of the root project and find all
     * inclusions. It will use those to determine whether to compile locally or not.
     *
     * @param project
     */
    public RaizDependencyCompiler(Project project) {
        mProject = project;


        Project root = project.getRootProject();

        //check the settings file for modules that we can use in compiling
        File settingsFile = root.file("settings.gradle");

        try {
            Scanner scanner = new Scanner(settingsFile);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println("********" + line + "*********");
                line = line.replace("include", "").trim();
                String[] modules = line.split(",");
                for (String module : modules) {
                    module = module.replaceAll("'", "").trim();
                    System.out.println("*******Found Module***** : " + module);
                    mModules.add(module);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        File librariesFile = root.file("libs/");
        if (librariesFile != null && librariesFile.isDirectory()) {
            File[] libs = librariesFile.listFiles();
            if (libs != null) {
                for (File file : libs) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        mLibraries.add(file.getName());
                    }
                }
            }
        }
    }

    /**
     * This will compile a whole listing of modules separated by a comma and surrounded by [].
     *
     * @param modules the list of modules we wish to apply compile to
     * @see #raizCompile(String)
     */
    public void raizCompile(String[] modules) {
        for (String module : modules) {
            raizCompile(module);
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @see #raizCompile(String, String)
     */
    public void raizCompile(String module) {
        String artifactName = String.format("%1s:%1s:%1s%1s", RaizLibraryPlugin.GROUP, module, "+", "@aar");
        raizCompile(module, artifactName);
    }

    /**
     * This will use the default "compile" and "@aar" mechanisms for getting dependencies.
     *
     * @param module       the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #raizCompile(String, String, String)
     */
    public void raizCompile(String module, String artifactName) {
        String fullyQualifiedName = ":Libraries:" + module;
        raizCompile("compile", fullyQualifiedName, artifactName.concat("@aar"));
    }

    /**
     * This function will compile a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param module          the name of the module, most always the fully-qualified name of the module (i.e: ":Libraries:Core"
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void raizCompile(String compilationMode, String module, String artifactName) {
        DependencyHandler dependencyHandler = mProject.getDependencies();

        // We found the module locally, compile it locally
        if (mModules.contains(module)) {
            dependencyHandler.add(compilationMode, mProject.project(module));
            System.out.println("Compiling local project: " + module);
        } else {
            // remote dependency, we will compile it using the params provided
            dependencyHandler.add(compilationMode, artifactName);
            System.out.println("Compiling remote dependency: " + artifactName);
        }
    }

    /**
     * Uses the default "compile" configuration.
     *
     * @param jarName      the name of the jar file without the extension
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #raizCompile(String, String, String)
     */
    public void raizJarCompile(String jarName, String artifactName) {
        raizCompile("compile", jarName, artifactName);
    }

    /**
     * This method will see if the jar exists within the 'libs' dir, if so it will use the local version.
     * If not, it will reference the specified artifactName remotely.
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param jarName         the name of the jar file without the extension
     * @param artifactName    the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void raizJarCompile(String compilationMode, String jarName, String artifactName) {
        DependencyHandler dependencyHandler = mProject.getDependencies();

        String fileName = jarName.concat(".jar");

        // Local dependency
        if (mLibraries.contains(fileName)) {
            dependencyHandler.add(compilationMode, mProject.file("libs/" + jarName));
            System.out.println("Compiling local jar: " + jarName);
        } else {
            // Remote dependency
            dependencyHandler.add(compilationMode, artifactName);
            System.out.println("Compiling remote dependency: " + artifactName);
        }
    }
}
