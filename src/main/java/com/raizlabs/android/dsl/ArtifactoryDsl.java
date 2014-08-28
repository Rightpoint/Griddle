package com.raizlabs.android.dsl;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ArtifactoryDsl {

    private String contextUrl;

    private RepositoryContainerDsl publish;

    private RepositoryContainerDsl resolve;

    public ArtifactoryDsl(String contextUrl, RepositoryContainerDsl publish, RepositoryContainerDsl resolve) {
        this.contextUrl = contextUrl;
        this.publish = publish;
        this.resolve = resolve;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    public RepositoryContainerDsl getPublish() {
        return publish;
    }

    public void setPublish(RepositoryContainerDsl publish) {
        this.publish = publish;
    }

    public RepositoryContainerDsl getResolve() {
        return resolve;
    }

    public void setResolve(RepositoryContainerDsl resolve) {
        this.resolve = resolve;
    }
}
