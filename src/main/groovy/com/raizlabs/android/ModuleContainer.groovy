package com.raizlabs.android

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

import java.util.regex.Pattern

/**
 * Description:
 */
public class ModuleContainer extends BaseContainer {

    /**
     * The local modules we found
     */
    private final Set<String> mModules = new HashSet<String>();

    private final Set<String> mRemoteModules = new HashSet<>();

    /**
     * Constructs the instance of this object. It will traverse the settings.gradle file of the root project and find all
     * inclusions. It will use those to determine whether to compile locally or not.
     *
     * @param project
     */
    public ModuleContainer(Project project) {
        super(project);

        Project root = project.rootProject;

        //check the settings file for modules that we can use in compiling
        File settingsFile = root.file "settings.gradle"

        try {
            Scanner scanner = new Scanner(settingsFile);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                line = line.replace("include", "").trim();
                String[] modules = line.split(",");
                for (String module : modules) {
                    module = module.replaceAll("'", "").trim();
                    // not commented and we do not already have the module in the project
                    if (!module.startsWith("//") && !mModules.contains(module)) {
                        printLog "*******Found Module***** : ${module}"
                        mModules.add(module);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.message);
            e.printStackTrace();
        }

    }

    /**
     * Returns the artifact name combination based on user preferences
     *
     * @param module
     * @return
     */
    private static String getArtifactName(String module) {
        "${RaizLibraryPlugin.GROUP}:${module}:+${RaizLibraryPlugin.LIBRARY_EXTENSION}"
    }

    /**
     * Returns the fully qualified local module name for the specified partial module.
     *
     * @param module
     * @return
     */
    private static String getFullyQualifiedName(String module) {
        ":${RaizLibraryPlugin.LIBRARY_DIRECTORY}:${module}"
    }

    private static String getFullyQualifiedArtifactName(String groupId, String name, String version) {
        "${groupId}:${name}:${version}"
    }

    /**
     * This will compile a whole listing of modules separated by a comma and surrounded by [].
     *
     * @param modules the list of modules we wish to apply compile to
     * @see #mod(String)
     */
    public void mods(String[] modules) {
        modules.each { String m ->
            mod m
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @see #mod(String, String)
     */
    public void mod(String module) {
        String[] moduleNotationParts = module.split(':')
        if (moduleNotationParts.length > 2) {
            String version = moduleNotationParts[2]

            // version checker. If we have a version specified in correct place, its an artifact
            Pattern pattern = Pattern.compile("[0-9][0-9]?\\.[0-9][0-9]?\\.[0-9][0-9]*")
            if (pattern.matcher(version).find()) {

                // This is a split library declaration
                if (moduleNotationParts[1].startsWith("{") && moduleNotationParts[1].endsWith("}")) {
                    String[] modules = moduleNotationParts[1].replace('{', '').replace('}', '').split(',')
                    for (String modPart : modules) {
                        mod modPart, getFullyQualifiedArtifactName(moduleNotationParts[0], modPart, moduleNotationParts[1])
                    }
                } else {
                    mod module, module
                }
            } else {
                mod module, getArtifactName(module)
            }
        } else {
            mod module, getArtifactName(module)
        }
    }

    /**
     * This will use the default "compile" mechanisms for getting dependencies.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #mod(String, String, String)
     */
    public void mod(String module, String artifactName) {
        mod 'compile', getFullyQualifiedName(module), artifactName
    }

    /**
     * This function will compile a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param module the name of the module, most always the fully-qualified name of the module (i.e: ":Libraries:Core"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void mod(String compilationMode, String module, String artifactName) {
        DependencyHandler dependencyHandler = project.getDependencies();

        // We found the module locally, compile it locally
        if (mModules.contains(module)) {
            printLog "Compiling local project: ${module}"
            dependencyHandler.add compilationMode, project.project(module)
        } else {
            mRemoteModules.add(artifactName);
            // remote dependency, we will compile it using the params provided
            printLog "Compiling remote dependency: ${artifactName}"
            dependencyHandler.add compilationMode, artifactName

            dependencyHandler.add 'linkSources', "${artifactName}:sources@jar"
        }
    }

    /**
     * This will runtime a whole listing of modules separated by a comma and surrounded by [].
     *
     * @param modules the list of modules we wish to apply compile to
     * @see #mod(String)
     */
    public void rtMod(String[] modules) {
        modules.each { String m ->
            rtMod m
        }
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified
     *
     * @param module
     */
    public void rtMod(String module) {
        rtMod module, getArtifactName(module)
    }

    /**
     * This function will runtime a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    public void rtMod(String module, String artifactName) {
        mod 'runtime', getFullyQualifiedName(module), artifactName
    }

    Set<String> getRemoteModules() {
        return mRemoteModules
    }
}
