package com.raizlabs.android.dsl;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class RepositoryContainerDsl {

    private RepositoryDsl repository;

    public RepositoryContainerDsl(RepositoryDsl repository) {
        this.repository = repository;
    }

    public RepositoryDsl getRepository() {
        return repository;
    }

    public void setRepository(RepositoryDsl repository) {
        this.repository = repository;
    }
}
