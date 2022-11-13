package laoruga.dtogenerator.api.config;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */

@Data
@Slf4j
public class DtoGeneratorParams {

    Properties properties;

    private int maxDependentGenerationCycles = Integer.parseInt(getProperty("maxDependentGenerationCycles", "100"));
    private int maxCollectionGenerationCycles = Integer.parseInt(getProperty("maxCollectionGenerationCycles", "100"));

    private String getProperty(String name, String defaultValue) {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties.getProperty(name, defaultValue);
    }

    protected Properties loadProperties() {
        Optional<Properties> defaultProps = loadProperties(DtoGeneratorParams.class.getClassLoader(), "default-dtogenerator.properties");
        Optional<Properties> customProps = loadProperties(Thread.currentThread().getContextClassLoader(), "dtogenerator.properties");
        Properties props = defaultProps.orElseGet(Properties::new);
        customProps.ifPresent(props::putAll);
        return props;
    }

    private static Optional<Properties> loadProperties(ClassLoader classLoader, String fileName) {
        Properties properties = new Properties();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            return Optional.empty();
        }
        try (InputStream propertiesStream = classLoader.getResourceAsStream(fileName)) {
            log.debug("Reading properties from {}", resource);
            properties.load(propertiesStream);
            return Optional.of(properties);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to read properties file '" + fileName + "' from classpath", e);
        }
    }
}
