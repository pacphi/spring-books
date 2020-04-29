package io.pivotal.cfapp.books.springbooks;

import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.DRIVER_NAME;
import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.INSTANCE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

/**
 * Driver application showing Cloud Spanner R2DBC use with Spring Data.
 */
@SpringBootApplication
@EnableR2dbcRepositories
public class AppInit {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppInit.class);

  private static final String SPANNER_INSTANCE = System.getProperty("spanner.instance");

  private static final String GCP_PROJECT = System.getProperty("gcp.project");

  @Autowired
  private DatabaseClient r2dbcClient;

  public static void main(String[] args) {
    SpringApplication.run(AppInit.class, args);
  }

  @Configuration
  @Profile("!cloud")
  public static class DefaultConfig {

	@Bean
	ConnectionFactory spannerConnectionFactory(@Value("${spanner.database}") String database) {
		Assert.notNull(INSTANCE, "Please provide spanner.instance property");
		Assert.notNull(GCP_PROJECT, "Please provide gcp.project property");
		ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
			.option(Option.valueOf("project"), GCP_PROJECT)
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
	ConnectionFactory spannerConnectionFactory(@Value("${spanner.database}") String database) {
		CfEnv cfEnv = new CfEnv();
		List<CfService> spannerServiceInstances = cfEnv.findServicesByTag("gcp","spanner");
		Assert.isTrue(spannerServiceInstances.size() == 1, "Only one spanner.instance may exist in the same space");
		CfService spannerServiceInstance = spannerServiceInstances.get(0);
		ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
			.option(Option.valueOf("project"), spannerServiceInstance.getString("ProjectId"))
			.option(DRIVER, DRIVER_NAME)
			.option(INSTANCE, spannerServiceInstance.getString("instance_id"))
			.option(DATABASE, database)
			.build());

		return connectionFactory;
	}

  }

  @EventListener(ApplicationReadyEvent.class)
  public void setUpData() {
    LOGGER.info("Setting up test table BOOK...");
    try {
      r2dbcClient.execute("CREATE TABLE BOOK ("
          + "  ID STRING(36) NOT NULL,"
          + "  TITLE STRING(MAX) NOT NULL"
          + ") PRIMARY KEY (ID)")
          .fetch().rowsUpdated().block();
    } catch (Exception e) {
      LOGGER.info("Failed to set up test table BOOK", e);
      return;
    }
    LOGGER.info("Finished setting up test table BOOK");
  }

  @EventListener({ContextClosedEvent.class})
  public void tearDownData() {
    LOGGER.info("Deleting test table BOOK...");
    try {
      r2dbcClient.execute("DROP TABLE BOOK")
          .fetch().rowsUpdated().block();
    } catch (Exception e) {
      LOGGER.info("Failed to delete test table BOOK", e);
      return;
    }

    LOGGER.info("Finished deleting test table BOOK.");
  }


  @Bean
  public RouterFunction<ServerResponse> indexRouter() {
    // Serve static index.html at root.
    return route(
        GET("/"),
        req -> ServerResponse.permanentRedirect(URI.create("/index.html")).build());
  }
}