package main.com.raizlabs.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

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

    public static final String ARTIFACTORY_USER = "artifactory_user";

    public static final String ARTIFACTORY_PASSWORD = "artifactory_password";

    @Override
    public void apply(Project project) {
        project.getConvention().getPlugins().put("RaizCompiler", new RaizDependencyCompiler(project));

        // this is for artifactory and releasing builds
        if(project.getTasks().findByPath("assembleRelease")!=null) {

            project.getPlugins().apply(MAVEN);
            project.getPlugins().apply(ARTIFACTORY);
            project.setGroup(GROUP);

            String contextUrl = (String) project.property(ARTIFACTORY_CONTEXT_URL);
            String user = (String) project.property(ARTIFACTORY_USER);
            String pass = (String) project.property(ARTIFACTORY_PASSWORD);

            project.getRepositories().maven(new ArtifactoryAction(contextUrl + "/android-dev", user, pass));
        } else {
            System.out.println("Skipping dependency resolver as this is a debug build");
        }
    }


}
