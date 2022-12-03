package org.laoruga.dtogenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */

@Slf4j
public class DtoGeneratorFileConfig extends DtoGeneratorConfig {

    Properties properties;

    public DtoGeneratorFileConfig(String propsFile) {
        this.properties = loadProperties(propsFile);
        initPropsDto();
    }

    protected Properties loadProperties(String propsFile) {
        Optional<Properties> defaultProps = loadProperties(DtoGeneratorFileConfig.class.getClassLoader(), "default-" + propsFile);
        Optional<Properties> customProps = loadProperties(Thread.currentThread().getContextClassLoader(), propsFile);
        Properties props = defaultProps.orElseGet(Properties::new);
        customProps.ifPresent(props::putAll);
        return props;
    }

    private void initPropsDto() {
        setMaxDependentGenerationCycles(Integer.parseInt(getProperty("maxDependentGenerationCycles", "100")));
        setMaxCollectionGenerationCycles(Integer.parseInt(getProperty("maxCollectionGenerationCycles", "100")));
        setGenerateAllKnownTypes(Boolean.parseBoolean(getProperty("generateAllKnownTypes", "false")));
    }

    private String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
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
