package extensions

import org.gradle.api.Project

/**
 * Created by hozer on 2.4.2017.
 */
public class ConfigurationPluginExtension {
    public String configurationUrl = null
    public boolean autoRun = false

    public ConfigurationPluginExtension(Project project){

    }
}
