package com.raizlabs.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This plugin handles supplying the build process with the correct dependencies to use.
 */
public class RaizLibraryPlugin implements Plugin<Project> {

    /**
     * Property key that specifies the default group to resolve if we simply specify the name of an artifact.
     */
    public static final String DEFAULT_GROUP = "rlp_default_group";

    /**
     * Property key that specifies the default library directory to search for local dependencies in relation to the project's root.
     */
    public static final String DEFAULT_LIBRARY = "rlp_default_library_directory";

    /**
     * Propertey key that specifies the default library extension on the unresolved artifact name. e.g. '@aar'
     */
    public static final String DEFAULT_LIBRARY_EXTENSION = "rlp_default_library_extension";

    /**
     * Property key. If this is present, we will print logs from the compiler
     */
    public static final String PRINT_LOGS = "print_logs";

    /**
     * The group name we found from gradle.properties
     */
    public static String GROUP;

    /**
     * The library directory we found from gradle.properties. The default is "Libraries".
     */
    public static String LIBRARY_DIRECTORY = "Libraries";

    /**
     * The library extension we found from gradle.properties. The default is left empty.
     */
    public static String LIBRARY_EXTENSION = "";

    @Override
    public void apply(Project project) {

        // The default group property
        if(project.hasProperty(DEFAULT_GROUP)) {
            GROUP = project.property(DEFAULT_GROUP).toString();
            System.out.println("Found group: " + GROUP);
        } else {
            throw new IllegalStateException("Project " + project.getName() + " must have a default group specified in" +
                    "a gradle.properties file. The recommended location is in the global ~/.gradle/ directory.");
        }

        if(project.hasProperty(DEFAULT_LIBRARY)) {
            LIBRARY_DIRECTORY = project.property(DEFAULT_LIBRARY).toString();
            System.out.println("Found default library: " + LIBRARY_DIRECTORY);
        }

        if(project.hasProperty(DEFAULT_LIBRARY_EXTENSION)) {
            LIBRARY_EXTENSION = project.property(DEFAULT_LIBRARY_EXTENSION).toString();
            System.out.println("Found default library extension: " + LIBRARY_EXTENSION);
        }

        project.getConvention().getPlugins().put("RaizCompiler", new RaizDependencyCompiler(project));
        project.getRepositories().mavenCentral();
    }


}
