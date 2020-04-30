package io.pivotal.cfapp.books.springbooks;

import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.DRIVER_NAME;
import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.GOOGLE_CREDENTIALS;
import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.INSTANCE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import lombok.extern.slf4j.Slf4j;

/**
 * Driver application showing Cloud Spanner R2DBC use with Spring Data.
 */
@SpringBootApplication
@EnableR2dbcRepositories
@ConfigurationPropertiesScan
@Slf4j
public class AppInit {

  private static final String SPANNER_INSTANCE = System.getProperty("spanner.instance");

  private static final String GCP_PROJECT = System.getProperty("gcp.project");

  private static final String GCP_SERVICE_ACCOUNT_KEY_JSON_FILE = System.getProperty("gcp.service_account_key_json_file");


  @Autowired
  private DatabaseClient r2dbcClient;

  public static void main(String[] args) {
    SpringApplication.run(AppInit.class, args);
  }

  @Configuration
  @Profile("!cloud")
  public static class DefaultConfig {

    @Bean
    ConnectionFactory spannerConnectionFactory(
      @Value("${spanner.database}") String database,
      @Value("${gcp.service_account_json}") String serviceAccountKeyJsonFile) throws IOException {

      Assert.notNull(INSTANCE, "Please provide spanner.instance property");
      Assert.notNull(GCP_PROJECT, "Please provide gcp.project property");
      Assert.notNull(GCP_SERVICE_ACCOUNT_KEY_JSON_FILE, "Please provide gcp.service_account_key_json_file property");

      GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyJsonFile));
      ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(Option.valueOf("project"), GCP_PROJECT)
        .option(GOOGLE_CREDENTIALS, credentials)
        .option(DRIVER, DRIVER_NAME)
        .option(INSTANCE, SPANNER_INSTANCE)
        .option(DATABASE, database)
        .build());

      return connectionFactory;
    }

  }

  @Configuration
  @Profile("cloud")
  public static class CloudConfig {

    @Bean
    ConnectionFactory spannerConnectionFactory(
      SpannerSettings settings,
      @Value("${gcp.service_account_key_json}") String serviceAccountKeyJson) throws IOException {

      GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccountKeyJson.getBytes()));
      ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(Option.valueOf("project"), settings.getProject())
        .option(GOOGLE_CREDENTIALS, credentials)
        .option(DRIVER, DRIVER_NAME)
        .option(INSTANCE, settings.getInstance())
        .option(DATABASE, settings.getDatabase())
        .build());

      return connectionFactory;
    }

  }

  @EventListener(ApplicationReadyEvent.class)
  public void setUpData() {
    log.trace("Setting up test table BOOK...");
    try {
      r2dbcClient.execute("CREATE TABLE BOOK ("
          + "  ID STRING(36) NOT NULL,"
          + "  TITLE STRING(MAX) NOT NULL"
          + ") PRIMARY KEY (ID)")
          .fetch().rowsUpdated().block();
    } catch (Exception e) {
      log.trace("Failed to set up test table BOOK", e);
      return;
    }
    log.trace("Finished setting up test table BOOK");
  }

  @EventListener({ContextClosedEvent.class})
  public void tearDownData() {
    log.trace("Deleting test table BOOK...");
    try {
      r2dbcClient.execute("DROP TABLE BOOK")
          .fetch().rowsUpdated().block();
    } catch (Exception e) {
      log.trace("Failed to delete test table BOOK", e);
      return;
    }

    log.trace("Finished deleting test table BOOK");
  }


  @Bean
  public RouterFunction<ServerResponse> indexRouter() {
    // Serve static index.html at root.
    return route(
        GET("/"),
        req -> ServerResponse.permanentRedirect(URI.create("/index.html")).build());
  }
}
