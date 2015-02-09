# Griddle

A powerful dependency management solution for Android/Java-based gradle build environments. It drastically reduces the complexity of ```build.gradle``` files while providing powerful and flexible dependency resolution on top of normal **Gradle**.

## Usage

### Including in your project

Add the following block to your ```buildscript.repositories{}``` block in the project-level **build.gradle** 

```groovy

buildscript {
    repositories {
        mavenCentral()
        maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
    }
    dependencies {
        ....
        classpath 'com.raizlabs:griddle:1.0.0'
    }

}

// this should come after 'com.android.library' or 'com.android.application
apply plugin: 'com.raizlabs.griddle'

```

#### Optional Properties

This library uses gradle properties to determine where to search for the dependencies. For example:

```griddle_default_group```: The group to resolve dependencies without a specified artifact equivalent when simply specifying a name for the dependency.

```griddle_default_library_directory```: The default directory to resolve local submodules in. Default is ":Libraries"

```griddle_default_library_extension```: The default extension on dependencies when simply specifying a name for the dependency and the local version is not found. Default is empty. You can specify something like ```@aar``` or ```@jar``` if needed. 

Add these variables to your global ```~/.gradle/gradle.properties``` file, or in project-level gradle.properties file. 

## Methods

  1. ```mod()``` + ```nsMod()```
  2. ```jar()```

### mod + nsMod

The ```mod()``` and ```nsMod()``` functions are a wrapper around a ```compile``` statement and provides the following:

  1. Automatic determination if there is a local dependency of a dependency
  2. **only mod()** Automatically links a ```sources.jar``` to a remote dependency 
  3. Custom artifact notation to "swizzle" in multiple dependencies from the same repo

#### Remote vs. Local

Many times in projects we have a list of dependencies that we utilize and they either specify a remote or local dependency. To simplify this process and enable us to __dynamically__ switch between local and remote as needed, this plugin provides you with a very simple and powerful way such that you will never need to modify your projects ```build.gradle```. 

Dependency resolution for local vs. remote is determined if a project exists in the ```settings.gradle```. Aiding in that aspect is the ```griddle_default_library_directory``` that will help specify its location when we omit the directory prefix in a ```mod()``` or ```nsMod()``` statement. 

#### Linking Sources

Surprisingly enough, the standard ```compile``` statement in Gradle does not enable attaching sources to the remote dependency easily. Also, to note, we do not want to have to manually attach sources every time we update a dependency and ensure that those sources are the correct one.

By using the ```mod()``` function, sources will automatically get attached. **Note**: not all dependencies have sources, so if it fails to discover a ```sources.jar``` use ```nsMod()``` (no-source module) instead.

#### Custom Artifact Notation for build.gradle dependency crushing

##### Example

For multiple dependencies coming from the same repo that all have to share the same version, previously we had to do something like this:

```groovy

dependencies {

  compile 'com.google.android.gms:play-services-maps:6.5.87'
  compile 'com.google.android.gms:play-services-location:6.5.87'
  compile 'com.google.android.gms:play-services-plus:6.5.87'
  compile 'com.google.android.gms:play-services-fitness:6.5.87'
}

```

Every time we want to update, we have to painstakingly change each one. Using the ```mod()``` or ```nsMod()``` (for this example), we can move this declaration to **one** line.

```groovy

dependencies {
  nsMod 'com.google.android.gms:{play-services-maps, play-services-location, play-services-plus, play-services-fitness}:6.5.87'
}

```

##### Custom Notation Features

 Using the ```mod``` or ```nsMod``` in this format: ```mod 'groupId:artifactName:artifactVersion'``` you can:
 
   1. Place ```{}``` around the ```artifactName``` and add more similar artifacts separated by commas
   2. If (1) is used, you can place ```{}``` around the ```artifactVersion``` and specify a per-dependency version. **Note** the length of comma-separated ```artifactNames``` must match the versions specified.
   3. Add a configuration (such as ```compileDebug```) name to the end of the function: ```mod 'groupId:artifactName:artifactVersion', 'compileDebug'```

**Note** any artifact specified this way will automatically utilize the next feature of this library. If there is a local version available in the ```settings.gradle``` by combining these properties:  

 Specifying in this format: ```mod 'artifactName'```:
  1. Utilizes the ```griddle_default_group``` if the local version is missing
  2. Utilizes ```griddle_default_library_directory``` to find the local version of this repo
  3. Uses the ```griddle_default_library_extension``` if local version is missing by appending it to create the ```compile 'groupId:artifactName:artifactVersion{extension}'```
  

### jar()

For specifying a jar dependency, we usually specify it this way:

```groovy

dependencies {
  compile files('libs/android-support-v4.jar', 'libs/http-master-1.0.6.jar')
}

```

For a much cleaner approach, with this plugin we can use:

```groovy

dependencies {
  jar 'android-support-v4'
  jar 'http-master-1.0.6'
}

```

The ```jar()``` method will automatically append ```libs``` and ```.jar``` to the dependency to clean up the look of the file. If you need all jars, use the method ```jars()``` to mimic the ```compile fileTree("libs", include: "*.jar")```
