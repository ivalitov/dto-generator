package org.laoruga.dtogenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Loads properties from files, overriding properties from file with prefix: 'default-'.
 *
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */

@Slf4j
public class DtoGeneratorFileConfig extends DtoGeneratorConfig {

    private final Properties properties;
    private final String propsFileName;
    private static final String DEFAULT_PREFIX = "default-";

    public DtoGeneratorFileConfig(String propsFile) {
        this.propsFileName = propsFile;
        this.properties = loadPropertiesFromFiles();
        initConfigDto();
    }

    private Properties loadPropertiesFromFiles() {

        Optional<Properties> defaultProps = loadPropertiesFromFile(
                DtoGeneratorFileConfig.class.getClassLoader(), DEFAULT_PREFIX + propsFileName);

        Optional<Properties> customProps = loadPropertiesFromFile(
                Thread.currentThread().getContextClassLoader(), propsFileName);

        Properties props = defaultProps.orElseGet(Properties::new);
        customProps.ifPresent(props::putAll);
        return props;
    }

    private void initConfigDto() {
        setMaxDependentGenerationCycles(Integer.parseInt(getProperty("maxDependentGenerationCycles")));
        setMaxCollectionGenerationCycles(Integer.parseInt(getProperty("maxCollectionGenerationCycles")));
        setGenerateAllKnownTypes(Boolean.parseBoolean(getProperty("generateAllKnownTypes")));
    }

    private String getProperty(String name) {
        return Objects.requireNonNull(
                properties.getProperty(name),
                "Property '" + name + "' not found in the: '" + DEFAULT_PREFIX + propsFileName + "' ()"
        );
    }

    private static Optional<Properties> loadPropertiesFromFile(ClassLoader classLoader, String fileName) {
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
