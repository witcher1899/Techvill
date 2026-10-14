package config;

import org.aeonbits.owner.ConfigFactory;

public final class ConfigReader {

    public static final VaultSecretConfig vaultSecretConfig =
            ConfigFactory.create(
                    VaultSecretConfig.class,
                    System.getenv(),
                    System.getProperties()
            );

    private ConfigReader() {
    }
}