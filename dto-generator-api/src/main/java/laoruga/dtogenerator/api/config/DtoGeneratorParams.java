package laoruga.dtogenerator.api.config;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
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
        return loadProperties(Thread.currentThread().getContextClassLoader(), "dtogenerator.properties");
    }

    private static Properties loadProperties(ClassLoader classLoader, String fileName) {
        try (InputStream propertiesStream = classLoader.getResourceAsStream(fileName)) {
            log.debug("Reading properties from {}", classLoader.getResource(fileName));
            Properties properties = new Properties();
            properties.load(propertiesStream);
            return properties;
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to read properties file '" + fileName + "' from classpath", e);
        }
    }
}
