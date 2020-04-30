package io.pivotal.cfapp.books.springbooks;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "spanner")
public class SpannerSettings {

    private final String database;
    private final String instance;
    private final String project;

    public SpannerSettings(String database, String instance, String project) {
        this.database = database;
        this.instance = instance;
        this.project = project;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getInstance() {
        return this.instance;
    }

    public String getProject() {
        return this.project;
    }

    @Override
    public String toString() {
        return "{" +
            " database='" + database + "'" +
            ", instance='" + instance + "'" +
            ", project='" + project + "'" +
            "}";
    }

}