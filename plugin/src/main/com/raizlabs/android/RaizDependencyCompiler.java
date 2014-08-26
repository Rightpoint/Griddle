package main.com.raizlabs.android;

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

    /**
     * Constructs the instance of this object. It will traverse the settings.gradle file of the root project and find all
     * inclusions. It will use those to determine whether to compile locally or not.
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
                if(line.startsWith("include: ")){
                    line = line.replace("include:", "").trim();
                    String[] modules = line.split(",");
                    for(String module: modules) {
                        mModules.add(module.replaceAll("'", ""));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified.
     * @param module - the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     */
    public void raizCompile(String module) {
        raizCompile(module, "+");
    }

    /**
     * This uses simply the module name (non-fully qualified) and the version.
     * @see #raizCompile(String, String, String)
     * @param module - the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param version - the version of the repo in gradle version format. (i.e: '1.0.+")
     */
    public void raizCompile(String module, String version) {
        raizCompile(module, RaizLibraryPlugin.GROUP, version);
    }

    /**
     * This uses the same name for the module locally as remotely.
     * @see #raizCompile(String, String, String, String)
     * @param module - the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param group - the group of the remote repository (if the module is not locally found)
     * @param version - the version of the repo in gradle version format. (i.e: '1.0.+")
     */
    public void raizCompile(String module, String group, String version) {
        String fullyQualifiedName = ":Libraries:" + module;
        raizCompile(fullyQualifiedName, group, module, version);
    }

    /**
     * This function will compile a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     * @param module - the name of the module, most always the fully-qualified name of the module (i.e: ":Libraries:Core"
     * @param group - the group of the remote repository (if the module is not locally found)
     * @param groupModuleName - the name of the artifact that exists remotely (if different than local)
     * @param version - the version of the repo in gradle version format. (i.e: '1.0.+")
     */
    public void raizCompile(String module, String group, String groupModuleName, String version) {
        DependencyHandler dependencyHandler = mProject.getDependencies();

        // We found the module locally, compile it locally
        if(mModules.contains(module)) {
            dependencyHandler.add("compile", module);
            System.out.println("Compiling local project: " + module);
        } else {
            // remote dependency, we will compile it using the params provided
            dependencyHandler.add("compile", String.format("%1s:%2s:%3s@aar", group, groupModuleName, version));
        }
    }
}
