package com.raizlabs;

import org.gradle.api.Project;

/**
 * Description:
 */
public class BaseContainer {

    private Project mProject;

    private final boolean printLogs;

    public BaseContainer(Project project) {
        this.mProject = project;
        printLogs = project.hasProperty(GriddlePlugin.PRINT_LOGS);
    }

    public Project getProject() {
        return mProject;
    }

    public boolean isPrintingLogs() {
        return printLogs;
    }

    void printLog(String text) {
        if(printLogs) {
            System.out.println(text);
        }
    }
}
