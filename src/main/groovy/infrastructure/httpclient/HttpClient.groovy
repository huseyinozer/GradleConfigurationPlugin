package infrastructure.httpclient

import org.gradle.api.Project

/**
 * Created by hozer on 2.4.2017.
 */
class HttpClient {

    //region Singleton Management

    private static HttpClient httpClient

    public static HttpClient getInstance(Project project){
        if (httpClient == null) {
            httpClient = new HttpClient(project)
        }
        return httpClient
    }

    //endregion

    private final Project project;

    public HttpClient(Project project){
        this.project = project
    }

    public String getRequest(String path){
        return new URL(path).text
    }
}
