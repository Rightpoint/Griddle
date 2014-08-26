package main.com.raizlabs.android;

import org.gradle.api.Action;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class will align the correct credentials with the {@link org.gradle.api.artifacts.repositories.MavenArtifactRepository}
 */
public class ArtifactoryAction implements Action<MavenArtifactRepository> {

    private final String mUrl;
    private final String mUser;
    private final String mPassword;

    public ArtifactoryAction(String url, String user, String password){

        this.mUrl = url;
        this.mUser = user;
        this.mPassword = password;
    }

    @Override
    public void execute(MavenArtifactRepository mavenArtifactRepository) {
        mavenArtifactRepository.setUrl(mUrl);

        PasswordCredentials credentials = mavenArtifactRepository.getCredentials();
        credentials.setUsername(mUser);
        credentials.setPassword(mPassword);
    }
}
