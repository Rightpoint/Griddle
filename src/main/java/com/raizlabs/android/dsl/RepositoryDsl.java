package com.raizlabs.android.dsl;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class RepositoryDsl {

    private String repoKey;

    private String username;

    private String password;

    private boolean maven = true;

    public RepositoryDsl(String repoKey, String username, String password) {
        this.repoKey = repoKey;
        this.username = username;
        this.password = password;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public boolean maven() {
        return maven;
    }

    public void setMaven(boolean maven) {
        this.maven = maven;
    }
}
