package io.pivotal.cfapp.books.springbooks;

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

public class SpannerCfEnvProcessor implements CfEnvProcessor {


    @Override
    public boolean accept(CfService service) {
        return
            service.existsByTagIgnoreCase("gcp", "spanner") ||
            service.existsByLabelStartsWith("google-spanner");
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
			        .serviceName("google-spanner-instance")
			        .build();
    }

    private static void addPropertyValue(String propertyName, Object propertyValue, Map<String, Object> properties) {
        properties.put(propertyName, propertyValue);
    }

}