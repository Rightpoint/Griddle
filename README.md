The RaizLibraryPlugin for Gradle
================================

## Enables easy swapping between remote and local dependencies within a build.gradle file by determining which is actually intended to be used in a project. It will scan the top-level **settings.gradle** for projects included locally in the build. It it as easy as taking out the ```include: ":Libraries:Submodule"``` in the settings.gradle to reference the remote version of the submodule.

## Usage

### Including in your project

1. Add the following block to your ```buildscript.repositories{}``` block in the project-level **build.gradle**

```groovy

buildscript {
    repositories {
        ....
        maven {
            url "${artifactory_contextUrl}/android-dev"
            credentials {
                username = "${artifactory_user}"
                password = "${artifactory_password}"
            }
        }
    }
    dependencies {
        ....
        classpath 'com.raizlabs.android-modules:RaizLibraryPlugin:1.x.x'

        // only use when we want to publish a module to artifactory
        classpath "org.jfrog.buildinfo:build-info-extractor-gradle:2.2.5"
    }

}

// this should come after 'com.android.library' or 'com.android.application
apply plugin: 'RaizLibraryPlugin'

```

2. Your ```~/.gradle/gradle.properties``` file should look like this:

```

artifactory_user={userNameHere}                                # The given artifactory username
artifactory_contextUrl=http://c3po.rz:8081/artifactory   # The given artifactory URL
artifactory_password={encryptedPasswordHere}        # The given artifactory encrypted passcode
org.gradle.daemon=true                                               # Runs gradle continuously, results in much faster build-start times
org.gradle.parallel=true                                                # Compiles submodules in parallel
org.gradle.jvmargs=-Xmx1g -XX:MaxPermSize=2g    # Gives gradle a ton of breathing room
org.gradle.configureondemand=true                           # Only attempts to build subprojects included by the main project
rlp_default_group=com.raizlabs.android-modules      # The group to resolve dependencies without a specified artifact equivalent
rlp_default_library_directory=:Libraries                       # The default directory to resolve local submodules in
rlp_default_library_extension=@aar                           # The default extension on dependencies without a specified artifact equivalent

```
### Methods

#### ```dependency()```

This is the main method for determining whether to use the local or remote version of a repository.

```groovy

dependencies {
    // can specify local folder name (assuming it's in :Libraries) and artifact to reference if missing
    dependency 'AndroidSupport', 'com.android.support:support-v4:20.+'
    
    // can compile a single module. This will essentially call:
    // dependency 'Connector', 'com.raizlabs.android-modules:Connector:+@aar'
    dependency 'Connector'
  
    // works for many modules as well for simplicity
    dependencies 'Parser', 'FastParser', 'Request',
            'VolleyExecutor', 'PressStateViews', 'EventFactory', 'Core'
}

```

#### ```jarDependency()```

This will do the same action as ```dependency()``` except for jar files. This will traverse the libs/ directory of the root project for the jar to reference locally. 

```groovy

dependencies {
    // Will search in the libs/ for 'project-lombok.jar'
    // If not found, it will use maven to locate it
    jarDependency 'project-lombok', 'org.projectlombok:lombok:1.14.4'

   // equivalent as writing compile files('libs/volley.jar'), just provides a much cleaner syntax
    jarDependency 'volley'
 
    // list of jars instead of writing:
    // compile files('libs/volley.jar', 'libs/project-lombok.jar', 'libs/android-support-v4.jar')
    jarDependencies 'volley', 'project-lombok', 'android-support-v4'
}

```