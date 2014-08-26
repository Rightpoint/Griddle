The RaizLibraryPlugin for Gradle
================================

## Enables easy swapping between remote and local dependencies within a build.gradle file by determining which is actually intended to be used in a project. It will scan the top-level **settings.gradle** for projects included in the build. This way we include submodules in a project is by adding one line in the file, or by choosing the remote version by removing that line. 

## Usage

### Including in your project

1. Add this block to your ```buildscript.repositories{}``` block in the project-level **build.gradle**
2. Add ```'classpath 'com.raizlabs.android-modules:raiz-library-plugin:1.x.x'``` under ```dependencies```
3. In the application's **build.gradle** add ```apply plugin: 'RaizLibraryPlugin'``` to gain access to its methods

### Methods

#### RaizCompile

This is the main method for determining whether to use the local or remote version of a repository.

```groovy
dependencies {
    // can specify local folder name (assuming it's in :Libraries) and artifact to reference if missing
    raizCompile 'AndroidSupport', 'com.android.support:support-v4:20.+'
    
    // can compile a single module. This will essentially call:
    // raizCompile 'Connector', 'com.raizlabs.android-modules:Connector:+@aar'
    raizCompile 'Connector'
  
    // works for many modules as well for simplicity
    raizCompile 'Parser', 'FastParser', 'Request',
            'VolleyExecutor', 'PressStateViews', 'EventFactory', 'Core'
}

```

#### RaizJarCompile

This will do the same action as ```raizCompile()``` except for jar files. This will traverse the libs/ directory of the root project for the jar to reference locally. 

**Coming Soon** This feature will enable you to override all submodule references to reference either the project-level jar, or artifactory.
```groovy
dependencies {
    // Will search in the libs/ for 'project-lombok.jar'
    // If not found, it will use maven to locate it
    raizJarCompile 'project-lombok', 'org.projectlombok:lombok:1.14.4'

}
```