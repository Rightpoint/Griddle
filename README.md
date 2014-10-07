The RaizLibraryPlugin for Gradle
================================

## Enables easy swapping between remote and local dependencies within a build.gradle file by determining which is actually intended to be used in a project. It will scan the top-level **settings.gradle** for projects included locally in the build. It it as easy as taking out the ```include: ":Libraries:Submodule"``` in the settings.gradle to reference the remote version of the submodule.

## Usage

### Including in your project

  1. Add the following block to your ```buildscript.repositories{}``` block in the project-level **build.gradle** (for now)

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
        classpath 'com.raizlabs.android-modules:libraryplugin:1.x.x'
    }

}

// this should come after 'com.android.library' or 'com.android.application
apply plugin: 'com.raizlabs.libraryplugin'

```

  2. Add these variables to your ```~/.gradle/gradle.properties``` file:

```

rlp_default_group=                                                      # The group to resolve dependencies without a specified artifact equivalent
rlp_default_library_directory=                                      # The default directory to resolve local submodules in (optional, default is "Libraries"
rlp_default_library_extension=                                    # The default extension on dependencies without a specified artifact equivalent (optional, default is the empty string)

```

3
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