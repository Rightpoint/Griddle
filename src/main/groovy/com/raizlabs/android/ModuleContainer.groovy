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

    protected static boolean hasBrackets(String compare) {
        compare.startsWith('{') && compare.endsWith('}')
    }

    protected static String[] getParts(String string) {
        string.replace('{', '').replace('}', '').split(',')
    }

    /**
     * Declares a module has having no source for the module
     * @param module Can be any of the standard mod formatting
     */
    public void nsMod(String module) {
        modResolve false, module
    }

    /**
     * Declares a module with source automatically added for the module.
     * @param module Can be: full artifact name (the standard notation), simply a module name (which will get appended the
     * default library prefix if local, otherwise uses the group id and + for its remote), or a "swizzled" artifact notation.
     * Ex: mod 'com.raizlabs.android:{DBFlow-Core, DBFlow}:1.4.1' is the same as writing
     *
     * mod 'com.raizlabs.android:DBFlow-Core:1.4.1'
     * mod 'com.raizlabs.android:DBFlow:1.4.1'
     *
     * Also if names are specified, then versions can also be, except version names MUST be the same length as the names. If a
     * local module with the same "swizzled" name is found in the settings.gradle, then the local version is used instead.
     */
    public void mod(String module) {
        modResolve true, module
    }

    /**
     * This will always use the latest, non-snapshot version of the module specified.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     */
    private void modResolve(boolean addSource, String module) {
        String[] moduleNotationParts = module.split(':')
        if (moduleNotationParts.length > 2) {
            String version = moduleNotationParts[2]

            // version checker. If we have a version specified in correct place, its an artifact
            Pattern pattern = Pattern.compile("[0-9][0-9]?\\.[0-9][0-9]?\\.[0-9][0-9]*")
            if (pattern.matcher(version).find()) {

                // This is a split library declaration
                if (hasBrackets(moduleNotationParts[1])) {
                    String[] modules = getParts(moduleNotationParts[1])
                    String[] versions = null;
                    if (hasBrackets(moduleNotationParts[2])) {
                        versions = getParts(moduleNotationParts[2])
                        if (modules.length != versions.length) {
                            throw new IllegalStateException("Module parts and version parts for ${module}" +
                                    " must be the same length if version is specified.")
                        }
                    }
                    for (int i = 0; i < modules.length; i++) {
                        String modPart = modules[i].trim()
                        compileMod addSource, modPart, getFullyQualifiedArtifactName(moduleNotationParts[0].trim(), modPart,
                                versions != null ? versions[i].trim() : moduleNotationParts[2].trim())
                    }
                } else {
                    compileMod addSource, module, module
                }
            } else {
                compileMod addSource, module, getArtifactName(module)
            }
        } else {
            compileMod addSource, module, getArtifactName(module)
        }
    }

    /**
     * This will use the default "compile" mechanisms for getting dependencies.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #mod(String, String, String)
     */
    private void compileMod(boolean addSource, String module, String artifactName) {
        modAdd addSource, 'compile', getFullyQualifiedName(module), artifactName
    }

    /**
     * This function will compile a module locally if the inclusion exists within the settings.gradle. If not,
     * it will use the args provided for a remote compile lookup
     *
     * @param compilationMode the build variant, project flavor, or combination that we want to compile with
     * @param module the name of the module, most always the fully-qualified name of the module (i.e: ":Libraries:Core"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     */
    private void modAdd(boolean addSource, String compilationMode, String module, String artifactName) {
        DependencyHandler dependencyHandler = project.getDependencies();

        // We found the module locally, compile it locally
        if (mModules.contains(module)) {
            printLog "Compiling local project: ${module}"
            dependencyHandler.add compilationMode, project.project(module)
        } else if (!mRemoteModules.contains(artifactName)) {
            mRemoteModules.add(artifactName);
            // remote dependency, we will compile it using the params provided
            printLog "Compiling remote dependency: ${artifactName}"
            dependencyHandler.add compilationMode, artifactName

            if (addSource) {
                dependencyHandler.add 'linkSources', "${artifactName}:sources@jar"
            }
        }
    }
}
