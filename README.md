# Griddle

Do you ever run into a scenario where you have multiple subprojects, and they point to both a local and remote version of the library your'e building? Or a subproject references a local version that does not exist, however exists in maven/ivy? Instead of having to modify the ```build.gradle``` file at all and having to worry about if the dependencies are local or remote, this plugin handles that determination by scanning the project's ```settings.gradle``` file and uses the inclusions there to determine whether to use local vs remote. 

**Note:**  instead of simply looking for the subproject existing in the default subproject directory, we use the ```settings.gradle``` to allow code to exist in parallel without affecting the build process.

## Usage

### Including in your project

#### Pre-Gradle 2.0
Add the following block to your ```buildscript.repositories{}``` block in the project-level **build.gradle** (for now until open source) 

```groovy

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        ....
        classpath 'com.raizlabs.android-modules:libraryplugin:1.x.x'
    }

}

// this should come after 'com.android.library' or 'com.android.application
apply plugin: 'com.raizlabs.libraryplugin'

```

#### Gradle 2.0+

After your ```buildscript{}``` and before applying other plugins: 

```
plugins {
  id "com.raizlabs.libraryplugin"
}
```

#### Required Properties

This library uses gradle properties to determine where to search for the dependencies. For example:

```
rlp_default_group=com.raizlabs.android   # The group to resolve dependencies without a specified artifact equivalent
rlp_default_library_directory=Libraries      # The default directory to resolve local submodules in (optional, default is "Libraries")
rlp_default_library_extension=@aar         # The default extension on dependencies without a specified artifact equivalent (optional, default is the empty string. Ex: @aar)
```
Add these variables to your global ```~/.gradle/gradle.properties``` file, or in project-level gradle.properties file. 

## Methods

  1. dependency()
  2. jarDependency()
  3. rtDependency()

### ```dependency()```

This is the main method for determining whether to use the local or remote version of a repository. It supports one, two or three parameters.

#### Example

Before:

```groovy 

dependencies {

  // what happens when we want our project to use the local version?
  compile 'com.raizlabs.android-modules:Connector:+@aar'

  // can't add this here, causes a duplicate dex file exception!
  compile ':Libraries:Connector'
}

```

After:

```groovy

dependencies {

    // If the module is in settings.gradle, we use :{defaultLibraryDir}:Connector
    // If not, we call 'com.raizlabs.android-modules:Connector:+@aar'
    dependency 'Connector'

    // can specify local folder name and artifact to reference if missing
    // should also be used if we want any kind of version control
    dependency 'AndroidSupport', 'com.android.support:support-v4:20.+'
  
    // works for many modules as well
    dependencies 'Parser', 'FastParser', 'Request',
            'VolleyExecutor', 'PressStateViews', 'EventFactory', 'Core'
}

```

#### ```jarDependency()```

This will do the same action as ```dependency()``` except for jar files. This will traverse the libs/ directory of the root project for the jar to reference locally. If the jar is not found in the libs directory, it will try to compile :{defaultLibraryDir}:JarName

```groovy

dependencies {

    // equivalent as writing compile files('libs/volley.jar'), just provides a much cleaner syntax
    jarDependency 'volley'

    // Will search in the libs/ for 'project-lombok.jar'
    // If not found, it will use maven to locate it
    jarDependency 'project-lombok', 'org.projectlombok:lombok:1.14.4'
 
    // list of jars instead of writing:
    // compile files('libs/volley.jar', 'libs/project-lombok.jar', 'libs/android-support-v4.jar')
    jarDependencies 'volley', 'project-lombok', 'android-support-v4'
}

```

#### ```rtDependency()```

Works as ```dependency()``` works except it uses the "runtime" keyword.
