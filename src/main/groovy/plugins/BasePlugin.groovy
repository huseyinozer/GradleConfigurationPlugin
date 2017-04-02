package plugins

import constants.ConfigurationPluginConstants
import extensions.ConfigurationPluginExtension
import infrastructure.httpclient.HttpClient
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by hozer on 2.4.2017.
 */
class BasePlugin implements Plugin<Project> {

    Project project;
    HttpClient httpClient;

    @Override
    void apply(Project project) {

        this.project = project;

        // Initialize httpClient
        httpClient = HttpClient.getInstance(project)

        // Check and create extensions
        if (project.extensions.findByName(ConfigurationPluginConstants.CONFIGURATION_PLUGIN_EXTENSION_NAME) == null) {
            project.extensions.create(ConfigurationPluginConstants.CONFIGURATION_PLUGIN_EXTENSION_NAME, ConfigurationPluginExtension, project)
        }
    }

    // Get current flavor
    String getCurrentFlavor(Project project) {
        Gradle gradle = project.getGradle()
        String tskReqStr = gradle.getStartParameter().getTaskRequests().toString()

        Pattern pattern;

        if (tskReqStr.contains("assemble"))
            pattern = Pattern.compile("assemble(\\w+)(Release|Debug)")
        else if (tskReqStr.contains(ConfigurationPluginConstants.CONFIGURATION_GENERATOR_TASK_NAME_PREFIX))
            pattern = Pattern.compile("${ConfigurationPluginConstants.CONFIGURATION_GENERATOR_TASK_NAME_PREFIX}(\\w+)")
        else
            pattern = Pattern.compile("generate(\\w+)(Release|Debug)")

        Matcher matcher = pattern.matcher(tskReqStr)

        if (matcher.find()) {
            println matcher.group(1).toLowerCase()
            return matcher.group(1).toLowerCase()
        } else {
            return "";
        }
    }

    // Get package name from manifest
    String getPackageNameFromManifest(Project project) {
        def manifest = new XmlSlurper().parse(new File("${project.projectDir}\\src\\main\\AndroidManifest.xml"))
        return manifest.@package.text()
    }
}
