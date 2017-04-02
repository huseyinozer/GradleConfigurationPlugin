package plugins

import constants.ConfigurationPluginConstants
import groovy.json.JsonSlurper
import helpers.TextCaseHelper
import org.gradle.api.Project
import org.gradle.util.GFileUtils

/**
 * Created by hozer on 2.4.2017.
 */
public class ConfigurationPlugin extends BasePlugin {

    private static final String CONFIGURATION_CLASS_NAME = "Configuration"

    private String configurationBuildPath;
    private File configurationBuildPathDirectory;

    void apply(Project project) {
        super.apply(project)

        // Path for generating configuration class file
        configurationBuildPath = "${project.getBuildDir().absolutePath}\\generated\\configuration\\"

        // Directory for generating configuration class file
        configurationBuildPathDirectory = new File(configurationBuildPath)

        // Set generated configuration path to sourceset for android
        project.android.sourceSets {
            main {
                java {
                    srcDirs += configurationBuildPathDirectory
                }
            }
        }

        project.afterEvaluate {

            boolean autoRun = project[ConfigurationPluginConstants.CONFIGURATION_PLUGIN_EXTENSION_NAME].autoRun

            if (autoRun) {
                init(project);
                return;
            }

            project.android.applicationVariants.collect { it.getFlavorName() }.unique().each { flavorName ->

                String taskNamePrefix = ConfigurationPluginConstants.CONFIGURATION_GENERATOR_TASK_NAME_PREFIX

                project.task("${taskNamePrefix}${flavorName.capitalize()}") {
                    group = ConfigurationPluginConstants.CONFIGURATION_PLUGIN_GROUP_NAME
                    doFirst {
                        init(project)
                    }
                }
            }
        }

    }

    void init(Project project) {

        // Current flavor
        def currentFlavor = getCurrentFlavor(project)

        String jsonText = null;

        // Get configuration plugin extensions
        def configurationPluginExtensions = project[ConfigurationPluginConstants.CONFIGURATION_PLUGIN_EXTENSION_NAME];

        // Check configuration extension
        String configurationUrl = configurationPluginExtensions.configurationUrl

        if (configurationUrl == null) {
            // JSON Configuration file in project
            def configurationJSONFile = new File("${project.getProjectDir().absolutePath}\\configurations.json")
            jsonText = configurationJSONFile.text;
        } else {
            jsonText = httpClient.getRequest(configurationUrl)
        }

        def packageName = getPackageNameFromManifest(project);

        // Generate class content
        String classContent = generateClassContent(currentFlavor, packageName, CONFIGURATION_CLASS_NAME, jsonText)

        // Directory management
        GFileUtils.deleteDirectory(configurationBuildPathDirectory);
        GFileUtils.mkdirs(configurationBuildPathDirectory)

        // Creating java file and writing content
        def newConfigurationFile = new File("${configurationBuildPath}\\${CONFIGURATION_CLASS_NAME}.java")
        newConfigurationFile.text = classContent.toString()
    }

    String generateClassContent(String currentFlavor, String packageName, String className, String jsonText) {

        // JSON deserialize
        def jsonScheme = new JsonSlurper().parseText(jsonText)

        // Class content
        def classContent = StringBuilder.newInstance();

        // Append package
        classContent.append("package ${packageName};")

        // Append new line
        classContent.append("\n\n")

        // Append class name
        classContent.append("public final class ${className} {")

        // Append new line
        classContent.append("\n")

        // Get default configurations
        def defaultConfigurationValues = jsonScheme["default"] as Map
        def flavorConfigurationValues = jsonScheme[currentFlavor] as Map

        // Looping list
        defaultConfigurationValues.each {

            // Get key and value
            def key = it.key as String
            def value = it.value as Object

            // Convert key camelCase to UPPER_UNDERSCORE
            key = TextCaseHelper.camelCaseToUpperUnderscore(key, Locale.ENGLISH)

            // Generate line
            def line = "\tpublic static final " + it.value.class.simpleName + " " + key + " = "
            classContent.append(line)

            // Check if key is exists, override default value
            if (flavorConfigurationValues != null && flavorConfigurationValues.containsKey(it.key)) {
                value = flavorConfigurationValues[it.key].value
            }

            // If value is string type, add " "
            if (it.value.class == String.class) {
                classContent.append("\"${value}\"")
                classContent.append(";")
            } else {
                classContent.append(it.value)
                classContent.append(";")
            }

            // Append new line
            classContent.append("\n")
        }

        classContent.append("}")

        return classContent.toString()
    }

}

