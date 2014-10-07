package com.raizlabs.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This plugin handles supplying the build process with the correct dependencies to use.
 */
public class RaizLibraryPlugin implements Plugin<Project> {

    public static final String DEFAULT_GROUP = "rlp_default_group";

    public static final String DEFAULT_LIBRARY = "rlp_default_library_directory";

    public static final String DEFAULT_LIBRARY_EXTENSION = "rlp_default_library_extension";

    public static String GROUP;

    public static String LIBRARY_DIRECTORY = "Libraries";

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

        // Add our methods to the plugins
        project.getConvention().getPlugins().put("RaizCompiler", new RaizDependencyCompiler(project));
        project.getRepositories().mavenCentral();
    }


}
