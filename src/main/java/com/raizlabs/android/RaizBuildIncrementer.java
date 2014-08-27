package com.raizlabs.android;

import org.gradle.api.Project;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This will increment the build number every time the project is built by modifying the file.
 */
public class RaizBuildIncrementer {

    public static final String PROP_VERSION = "version";

    public RaizBuildIncrementer(Project project) {


        File gradleProperties = project.file("gradle.properties");

        // If exists, we will increment version number
        if (gradleProperties.exists()) {

            try {
                Scanner scanner = new Scanner(gradleProperties);
                int lineNumber = -1;
                int version = 0;
                ArrayList<String> lines = new ArrayList<String>();
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    System.out.println("****Gradle Prop Line: " + line + " *****");
                    lineNumber++;
                    if (line.startsWith(PROP_VERSION)) {
                        line = line.replaceFirst(PROP_VERSION, "").trim().replaceFirst("=", "").trim();
                        Pattern pattern = Pattern.compile("[0-9][0-9]?\\.[0-9][0-9]?\\.[0-9][0-9]*");
                        if (pattern.matcher(line).find()) {
                            String[] versionArray = line.split(Pattern.quote("."));
                            System.out.println("Found module version: " + line + " with: " + Arrays.toString(versionArray));
                            version = Integer.valueOf(versionArray[2]) + 1;
                            String newVersion = versionArray[0].concat(".")
                                    .concat(versionArray[1]).concat(".").concat(String.valueOf(version));
                            project.setVersion(newVersion);
                            lines.add("version = ".concat(newVersion));
                        } else {
                            // We throw the exception so that the version number is correct
                            scanner.close();
                            throw new RuntimeException("****Version Number is NOT in the xx?.xx?.xx* format");
                        }

                    } else {
                        lines.add(line);
                    }
                }
                scanner.close();

                if (lineNumber != -1) {
                    System.out.println("Writing lines: " + lines.toString());
                    BufferedWriter fileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gradleProperties)));
                    for (String line : lines) {
                        fileOutputStream.write(line);
                        fileOutputStream.newLine();
                    }

                    fileOutputStream.close();
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Does not exist, we will generate the file for you!
            String buildName = project.getProjectDir().getName();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gradleProperties)));
                writer.write("version = 1.0.0");
                writer.write("buildName = " + buildName);
                writer.newLine();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Writing gradle.properties FAILED");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
