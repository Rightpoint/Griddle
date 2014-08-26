package com.raizlabs.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This plugin handles supplying the build process with the correct dependencies to use.
 */
public class RaizLibraryPlugin implements Plugin<Project> {

    public static final String MAVEN = "maven";

    public static final String ARTIFACTORY = "artifactory";

    public static final String GROUP = "com.raizlabs.android-modules";

    public static final String ARTIFACTORY_CONTEXT_URL = "artifactory_contextUrl";

    public static final String ARTIFACTORY_REPO_ENDPOINT = "/android-dev";

    public static final String ARTIFACTORY_USER = "artifactory_user";

    public static final String ARTIFACTORY_PASSWORD = "artifactory_password";

    @Override
    public void apply(Project project) {
        project.getConvention().getPlugins().put("RaizCompiler", new RaizDependencyCompiler(project));

        // this is for artifactory and releasing builds
        Task releaseTask = project.getTasks().findByPath("assembleRelease");
        if(releaseTask!=null) {
            // adds the configRelease task we want here
            releaseTask.dependsOn("configRelease");

            project.getPlugins().apply(MAVEN);
            project.getPlugins().apply(ARTIFACTORY);
            project.setGroup(GROUP);

            String contextUrl = (String) project.property(ARTIFACTORY_CONTEXT_URL);
            String user = (String) project.property(ARTIFACTORY_USER);
            String pass = (String) project.property(ARTIFACTORY_PASSWORD);

            project.getRepositories().maven(new ArtifactoryAction(contextUrl + ARTIFACTORY_REPO_ENDPOINT, user, pass));
        } else {
            System.out.println("Skipping dependency resolver as this is a debug build");
        }
    }


}
