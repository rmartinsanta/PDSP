package es.urjc.etsii.grafo.PDSP.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "custom")
public class PDSPConfig {
    // Define any configuration property to automatically set from any source,
    // including the command line, the application.yml or the environment
    // See: https://mork-optimization.readthedocs.io/en/latest/features/config/ for more details

    /**
     * Value from: custom.my-property, for example from the application.yml file,
     * could be overridden as a command line parameter: --custom.my-property=value
     */
    private boolean cache;

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }
}
