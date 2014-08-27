package com.raizlabs.android;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.invocation.Gradle;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class RaizLibraryInitPlugin implements Plugin<Gradle> {
    @Override
    public void apply(Gradle target) {
        target.allprojects(new Action<Project>() {
            @Override
            public void execute(Project project) {
                String contextUrl = (String) project.property(RaizLibraryPlugin.ARTIFACTORY_CONTEXT_URL);
                String user = (String) project.property(RaizLibraryPlugin.ARTIFACTORY_USER);
                String pass = (String) project.property(RaizLibraryPlugin.ARTIFACTORY_PASSWORD);

                // injecting our buildscript data into the repo so we never have to do this again
                project.getBuildscript().getDependencies().add("classpath", "org.jfrog.buildinfo:build-info-extractor-gradle:" + RaizLibraryPlugin.JFROG_VERSION);
                project.getBuildscript().getDependencies().add("classpath", "com.android.tools.build:gradle:" + RaizLibraryPlugin.GRADLE_TOOLS_VERSION);

                project.getBuildscript().getRepositories().mavenCentral();
                project.getBuildscript().getRepositories().jcenter();
                project.getBuildscript().getRepositories().maven(new ArtifactoryAction(contextUrl + RaizLibraryPlugin.ARTIFACTORY_REPO_ENDPOINT, user, pass));
                project.getBuildscript().getRepositories().maven(new Action<MavenArtifactRepository>() {
                    @Override
                    public void execute(MavenArtifactRepository mavenArtifactRepository) {
                        mavenArtifactRepository.setUrl(RaizLibraryPlugin.JFROG_URL);
                    }
                });
            }
        });
    }
}
