package io.pivotal.cfapp.books.springbooks;

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpannerCfEnvProcessor implements CfEnvProcessor {

    private static final String SERVICE_NAME = "google-spanner-instance";

    @Override
    public boolean accept(CfService service) {
        boolean found =
            service.existsByTagIgnoreCase("gcp", "spanner") ||
            service.existsByLabelStartsWith("google-spanner");
        if (found)
            log.trace("Spanner instance with name " + service.getName() + " found.");
        return found;
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        addPropertyValue("spanner.instance", cfCredentials.getString("instance_id"), properties);
        addPropertyValue("spanner.project", cfCredentials.getString("ProjectId"), properties);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return
            CfEnvProcessorProperties
                .builder()
			        .serviceName(SERVICE_NAME)
			        .build();
    }

    private static void addPropertyValue(String propertyName, Object propertyValue, Map<String, Object> properties) {
        properties.put(propertyName, propertyValue);
    }

}