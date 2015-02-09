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

#### Required Properties

This library uses gradle properties to determine where to search for the dependencies. For example:

```
griddle_default_group=com.raizlabs.android   # The group to resolve dependencies without a specified artifact equivalent
griddle_default_library_directory=Libraries      # The default directory to resolve local submodules in (optional, default is "Libraries")
griddle_default_library_extension=@aar         # The default extension on dependencies without a specified artifact equivalent (optional, default is the empty string. Ex: @aar)
```
Add these variables to your global ```~/.gradle/gradle.properties``` file, or in project-level gradle.properties file. 

## Methods

  1. mod() + nsMod()
  2. jar()

### mod + nsMod

The ```mod()``` function is a wrapper around a ```compile``` statement and provides the following:
1. Automatically links a ```sources.jar``` to the dependency
2. Custom artifact notation to "swizzle" in multiple dependencies from the same repo
3. Will use the specified artifact name if a local dependency in the ```settings.gradle``` is included

#### Linking Sources

Surprisingly enough, the standard ```compile``` statement in Gradle does not enable attaching sources to the remote dependency easily. Also, to note, we do not want to have to manually attach sources every time we update a dependency and ensure that those sources are the correct one.

By using the ```mod()``` function, sources will automatically get attached. **Note**: not all dependencies have sources, so if it fails to discover a ```sources.jar``` use ```nsMod()``` (no-source module) instead.

#### Custom Artifact Notation for build.gradle dependency crushing

#### Example

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

#### Features
