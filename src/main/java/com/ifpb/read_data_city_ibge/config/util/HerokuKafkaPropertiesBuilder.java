package com.ifpb.read_data_city_ibge.config.util;

import com.github.jkutner.EnvKeyStore;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
public class HerokuKafkaPropertiesBuilder {

    public static Properties build() throws IOException {
        Properties properties = new Properties();
        List<String> hostPorts = new ArrayList<>();

        String kafkaUrl = requireNonNull(getenv("KAFKA_URL"), "KAFKA_URL environment variable must be set for Heroku Kafka.");

        String[] urls = kafkaUrl.split(",");

        for (String url : urls) {
            try {
                URI uri = new URI(url);
                hostPorts.add(format("%s:%d", uri.getHost(), uri.getPort()));

                switch (uri.getScheme()) {
                    case "kafka":
                        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
                        break;
                    case "kafka+ssl":
                        configureSslProperties(properties);
                        break;
                    default:
                        throw new IllegalArgumentException(format("unknown scheme; %s", uri.getScheme()));
                }
            } catch (URISyntaxException e) {
                throw new IOException("Failed to parse KAFKA_URL component: " + url, e);
            }
        }

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", hostPorts));

        return properties;
    }

    private static void configureSslProperties(Properties properties) throws IOException {
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        // Desativa a verificação de hostname (necessário em alguns ambientes Heroku)
        properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        try {
            EnvKeyStore envTrustStore = EnvKeyStore.createWithRandomPassword("KAFKA_TRUSTED_CERT");
            EnvKeyStore envKeyStore = EnvKeyStore.createWithRandomPassword("KAFKA_CLIENT_CERT_KEY", "KAFKA_CLIENT_CERT");

            File trustStore = envTrustStore.storeTemp();
            File keyStore = envKeyStore.storeTemp();

            properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, envTrustStore.type());
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStore.getAbsolutePath());
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, envTrustStore.password());

            properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, envKeyStore.type());
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStore.getAbsolutePath());
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, envKeyStore.password());
        } catch (Exception e) {
            throw new IOException("Problem creating Kafka key stores for Heroku SSL", e);
        }
    }
}