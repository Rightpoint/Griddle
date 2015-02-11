package com.raizlabs

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Description:
 */
public class ModuleContainer extends BaseContainer {

    /**
     * The local modules we found
     */
    private final Set<String> mModules = new HashSet<String>()

    private final Set<String> mRemoteModules = new HashSet<>()

    static final Pattern bracketPattern = Pattern.compile("\\{.*\\}")

    static final Pattern bracketNamePattern = Pattern.compile(".+:\\{.*\\}:.*")

    static final Pattern bracketNameVersionPattern = Pattern.compile("\\{.*\\}:\\{.*\\}");

    static final Pattern doubleBracketPattern = Pattern.compile("\\{\\{.*\\}\\}")

    static final Pattern versionPattern = Pattern.compile("[0-9]+(.[0-9]+)+")

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
        "${GriddlePlugin.GROUP}:${module}:+${GriddlePlugin.LIBRARY_EXTENSION}"
    }

    /**
     * Returns the fully qualified local module name for the specified partial module.
     *
     * @param module
     * @return
     */
    private static String getFullyQualifiedName(String module) {
        "${GriddlePlugin.LIBRARY_DIRECTORY}:${module}"
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

    protected String[] getModuleNotationParts(String module) {
        String[] moduleNotationParts = module.split(':')

        boolean isBracketName = bracketNamePattern.matcher(module).find();

        // is a special case of brackets
        if (isBracketName) {
            moduleNotationParts = new String[3];

            int firstColon = module.indexOf(":")

            // first piece is the group id
            moduleNotationParts[0] = module.substring(0, firstColon)

            String remainder = module.substring(firstColon + 1);
            int secondColon = remainder.indexOf(":")
            boolean isBracketVersion = bracketNameVersionPattern.matcher(remainder).find();

            // has versions and names
            if(isBracketVersion) {
                Pattern firstBracket = Pattern.compile("\\{.*\\}:")
                Matcher bracketMatcher = firstBracket.matcher(remainder)
                if(bracketMatcher.find()) {
                    String piece = bracketMatcher.group()
                    moduleNotationParts[1] = piece.substring(0, piece.length()-1);
                    moduleNotationParts[2] = remainder.replace(moduleNotationParts[1]+":", "")
                }

            } else if(isBracketName) {
                if (remainder.startsWith("{")) {
                    secondColon = remainder.lastIndexOf("}") + 1
                }

                moduleNotationParts[1] = remainder.substring(0, secondColon)

                moduleNotationParts[2] = remainder.substring(secondColon + 1)
            }

            printLog "Changed parts to: ${moduleNotationParts}"
        }

        return moduleNotationParts
    }

    /**
     * Declares a module has having no source for the module
     * @see {@link #mod(String)}
     * @param module Can be any of the standard mod formatting
     */
    public void nsMod(String module) {
        nsMod module, 'compile'
    }

    /**
     * Declares a module has having no source for the module
     * @see {@link #mod(String)}
     * @param module Can be any of the standard mod formatting
     * @param configuration the configuration name to run
     */
    public void nsMod(String module, String configuration) {
        modResolve configuration, false, module
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
        mod module, 'compile'
    }

    /**
     * Declares a module with source automatically added for the module.
     * @param module Can be: full artifact name (the standard notation), simply a module name (which will get appended the
     * default library prefix if local, otherwise uses the group id and + for its remote), or a "swizzled" artifact notation.
     * Ex: mod 'com.raizlabs.android:{DBFlow-Core, DBFlow}:1.4.1', 'debugCompile' is the same as writing
     *
     * mod 'com.raizlabs.android:DBFlow-Core:1.4.1', 'debugCompile'
     * mod 'com.raizlabs.android:DBFlow:1.4.1', 'debugCompile'
     *
     * Also if names are specified, then versions can also be, except version names MUST be the same length as the names. If a
     * local module with the same "swizzled" name is found in the settings.gradle, then the local version is used instead.
     * @param configuration The configuration name to run
     */
    public void mod(String module, String configuration) {
        modResolve configuration, true, module
    }

    /**
     * This will resolve the notation for the module string passed in.
     * @see {@link #mod(String)} for more details
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     */
    private void modResolve(String compileMode, boolean addSource, String module) {

        String[] moduleNotationParts = getModuleNotationParts(module)

        if (moduleNotationParts.length > 2) {

            String version = moduleNotationParts[2].trim()

            // version checker. If we have a version specified in correct place, its an artifact
            if (versionPattern.matcher(version).find()) {

                String groupId = moduleNotationParts[0].trim()
                String modName = moduleNotationParts[1].trim()
                printLog "Modname: ${modName} version ${version}"

                // This is most likely a split library declaration
                if (hasBrackets(modName)) {

                    String[] modules = null;
                    if(doubleBracketPattern.matcher(modName).find()) {
                        String noBrackets = modName.substring(1, modName.length() - 1)
                        Matcher matcher = bracketPattern.matcher(noBrackets)
                        List<String> matches = new ArrayList<>()
                        while (matcher.find()) {
                            matches.add(matcher.group())
                        }
                        modules = matches.toArray(new String[matches.size()])
                    } else if(bracketPattern.matcher(modName).find()) {
                        String noBrackets = modName.substring(1, modName.length() - 1)
                        modules = noBrackets.split(',')
                    }

                    printLog "Modules ${modules}"

                    String[] versions = null;
                    if (hasBrackets(version)) {
                        versions = getParts(version)
                        printLog "Found versions: ${versions}"
                        if (modules.length != versions.length) {
                            throw new IllegalStateException("Module parts and version parts for ${module}" +
                                    " must be the same length if version is specified.")
                        }
                    }
                    for (int i = 0; i < modules.length; i++) {
                        String modPart = modules[i].trim()
                        String localName = modPart
                        String remoteName = modPart
                        printLog "modPart: ${modPart}"
                        if (hasBrackets(modPart)) {
                            String[] names = getParts(modPart)
                            if (names.length == 2) {
                                remoteName = names[0].trim().replace("remote: ", "")
                                localName = names[1].trim().replace("local: ", "")
                            } else {
                                remoteName = localName = modPart.replace('{', '').replace('}', '')
                            }
                        }
                        methodMod compileMode, addSource, localName, getFullyQualifiedArtifactName(groupId, remoteName,
                                versions != null ? versions[i].trim() : version)
                    }
                } else {
                    methodMod compileMode, addSource, modName, module
                }
            } else {
                methodMod compileMode, addSource, module, getArtifactName(module)
            }
        } else {
            methodMod compileMode, addSource, module, getArtifactName(module)
        }
    }

    /**
     * This will use the default "compile" mechanisms for getting dependencies.
     *
     * @param module the name of the module, does not have to be fully qualified as we will assume all libs are in "Libraries"
     * @param artifactName the fully qualified artifact name e.g: 'com.android.support:support-v4:1.xx.xx'
     * @see #mod(String, String, String)
     */
    private void methodMod(String method, boolean addSource, String module, String artifactName) {
        modAdd addSource, method, getFullyQualifiedName(module), artifactName
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

        printLog "Comparing ${module} with ${artifactName} for dependency"

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
