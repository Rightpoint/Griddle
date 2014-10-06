package com.raizlabs.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPluginUtil;
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention;
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig;
import org.jfrog.gradle.plugin.artifactory.extractor.GradleArtifactoryClientConfigUpdater;

import java.lang.reflect.Field;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This plugin handles supplying the build process with the correct dependencies to use.
 */
public class RaizLibraryPlugin implements Plugin<Project> {

    public static final String MAVEN = "maven";

    public static final String ARTIFACTORY = "artifactory";

    public static final String DEFAULT_GROUP = "rlp_default_group";

    public static final String DEFAULT_LIBRARY = "rlp_default_library_directory";

    public static final String DEFAULT_LIBRARY_EXTENSION = "rlp_default_library_extension";

    public static final String ARTIFACTORY_CONTEXT_URL = "artifactory_contextUrl";

    public static final String ARTIFACTORY_REPO_ENDPOINT = "/android-dev";

    public static final String ARTIFACTORY_USER = "artifactory_user";

    public static final String ARTIFACTORY_PASSWORD = "artifactory_password";

    public static final String JFROG_VERSION = "2.0.9";

    public static final String JFROG_URL = "http://dl.bintray.com/jfrog/jfrog-jars";

    public static final String GRADLE_TOOLS_VERSION = "0.12.+";

    public static final Object PUBLISHER_REPO_KEY = "android-dev";

    public static String GROUP;

    public static String LIBRARY_DIRECTORY = ":Libraries";

    public static String LIBRARY_EXTENSION = "";

    @Override
    public void apply(Project project) {

        // Retrieve global properties
        String contextUrl = (String) project.property(ARTIFACTORY_CONTEXT_URL);
        String user = (String) project.property(ARTIFACTORY_USER);
        String pass = (String) project.property(ARTIFACTORY_PASSWORD);

        // The default group property
        if(project.hasProperty(DEFAULT_GROUP)) {
            GROUP = project.property(DEFAULT_GROUP).toString();
        } else {
            throw new IllegalStateException("Project " + project.getName() + " must have a default group specified in" +
                    "a gradle.properties file. The recommended location is in the global ~/.gradle/ directory.");
        }

        if(project.hasProperty(DEFAULT_LIBRARY)) {
            LIBRARY_DIRECTORY = project.property(DEFAULT_LIBRARY).toString();
        }

        if(project.hasProperty(DEFAULT_LIBRARY_EXTENSION)) {
            LIBRARY_EXTENSION = project.property(DEFAULT_LIBRARY_EXTENSION).toString();
        }

        // Add our methods to the plugins
        project.getConvention().getPlugins().put("RaizCompiler", new RaizDependencyCompiler(project));
        project.getRepositories().mavenCentral();

        // this is for artifactory and releasing builds
        if(project.hasProperty("publish")) {
            if(!project.hasProperty("noIncrement")) {
                project.getConvention().getPlugins().put("RaizBuildIncrementer", new RaizBuildIncrementer(project));
            }

            // Set up plugins so we never need to add them to a build.gradle
            project.getPlugins().apply(MAVEN);
            project.getPlugins().apply(ARTIFACTORY);
            project.setGroup(GROUP);

            // Add Artifactory repo to the repositories
            project.getRepositories().maven(new ArtifactoryAction(contextUrl + ARTIFACTORY_REPO_ENDPOINT, user, pass));

            // We will define the plugin convention here so all of our modules do not need to
            // declare the artifactory closure manually
            ArtifactoryPluginConvention pluginConvention =
                    ArtifactoryPluginUtil.getArtifactoryConvention(project);
            pluginConvention.setContextUrl(contextUrl);

            PublisherConfig publisherConfig = new PublisherConfig(pluginConvention);
            publisherConfig.setContextUrl(contextUrl);
            pluginConvention.setPublisherConfig(publisherConfig);

            // Use reflection to access private field
            PublisherConfig.Repository repository = null;

            Field[] fields = PublisherConfig.class.getDeclaredFields();
            for(Field field : fields) {
                if(field.getName().equalsIgnoreCase("repository")) {
                    try {
                        field.setAccessible(true);
                        repository = (PublisherConfig.Repository) field.get(publisherConfig);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if(repository != null) {
                repository.setPassword(pass);
                repository.setUsername(user);
                repository.setRepoKey(PUBLISHER_REPO_KEY);
                repository.setMavenCompatible(true);
            }

            GradleArtifactoryClientConfigUpdater.update(pluginConvention.getClientConfig(), project.getRootProject());
        } else {
            System.out.println("Skipping dependency resolver as this is a debug build");
        }
    }


}
