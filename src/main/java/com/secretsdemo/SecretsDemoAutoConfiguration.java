package com.secretsdemo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * Ponto de entrada do pacote com.secretsdemo.
 * <p>
 * Registrado em META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports,
 * para que a CryptographyApplication carregue este pacote sem nenhuma alteração em com.cryptography.
 */
@AutoConfiguration
@ComponentScan("com.secretsdemo")
@PropertySource("classpath:secretsdemo.properties")
public class SecretsDemoAutoConfiguration {
}
