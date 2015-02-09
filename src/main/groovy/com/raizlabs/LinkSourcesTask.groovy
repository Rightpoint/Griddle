package com.raizlabs

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Description: Responsible for linking the sources jar files to the dependency files for the whole project.
 */
class LinkSourcesTask extends DefaultTask {

    boolean debug = false;

    /**
     * The files looked at
     */
    private Set<String> processedFiles = new HashSet<String>()

    /**
     * Print if debug
     * @param log The output to write
     */
    void printLog(String log) {
        if (debug) {
            println log
        }
    }

    void linkSources(File file) {
        link(file, 'sources')
    }

    /**
     * Loops through evaluated projects and will look for sources to attach to specific repository.
     */
    void linkSourcesFromProject() {
        project.rootProject.gradle.projectsEvaluated {
            project.rootProject.allprojects.each {
                if (it.configurations.hasProperty('linkSources')) {
                    it.configurations.linkSources.each { File file ->
                        linkSources(file)
                    }
                }
            }

            executeWithoutThrowingTaskFailure();
        }
    }

    @TaskAction
    def linkSources() {
        printLog "Linking Sources"
        if (inputs.getProperties() != null) {
            outputs.files.each { File xml ->
                def root = new XmlParser().parse(xml)

                def path = null;
                if ((path = inputs.getProperties().get("${xml.name}:sources".toString()))) {
                    appendPath(root.library[0].SOURCES[0], path)
                }

                new XmlNodePrinter(new PrintWriter(new FileWriter(xml))).print(root)

                printLog "Link succeeded: ${xml.name}"
            }
        }
    }

    /**
     * Links a file with the specified type
     * @param file The file to link with a corresponding type
     * @param type
     */
    private void link(File file, String type) {
        String name = file.name

        if (!processedFiles.contains(name)) {
            printLog "Link ${type}: ${name}"

            processedFiles.add(name)

            int index;

            if ((index = name.lastIndexOf('.')) != -1) {
                name = name.substring 0, index
            }

            if ((index = name.lastIndexOf('-')) != -1) {
                name = name.substring 0, index
            }

            if (name) {
                name = name.replace('.', '_').replace('-', '_')

                File xml = project.file("./.idea/libraries/${name}.xml")
                if (xml.exists() && xml.isFile()) {
                    inputs.property "${xml.name}:${type}".toString(), generatePath(file)
                    outputs.file xml
                } else {
                    printLog "No such file: ${xml.absolutePath}"
                }
            }
        }
    }

    /**
     * @param file The file we want to link to sources.
     * @return A path appending user home or project directory to the file location so we can link name to this path.
     */
    private String generatePath(File file) {
        String path = file.absolutePath

        String root = null;
        if ((root = project.rootDir) && path.startsWith(root)) {
            path = '$PROJECT_DIR$' + path.substring(root.length())
        } else if ((root = System.getProperty("user.home")) && path.startsWith(root)) {
            path = '$USER_HOME$' + path.substring(root.length())
        }

        path.replaceAll(/\\+/, '/')
    }

    /**
     * Appends the root url as a path to the sources JAR
     * @param node The node
     * @param path The path to the jar
     * @return
     */
    static def appendPath(Object node, Object path) {
        if (node) {
            node.children().clear()
            node.appendNode('root', [url: "jar://${path}!/"])
        }
    }
}