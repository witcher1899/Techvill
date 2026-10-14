package utils;

import config.ConfigReader;
import config.VaultSecretConfig;
import config.VaultTestConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;

import java.util.HashMap;
import java.util.Map;

public class VaultSecretUtil {

    private static final VaultSecretConfig VAULT_CONFIG =
            ConfigReader.vaultSecretConfig;

    private static final String VAULT_TOKEN =
            ConfigReader.vaultSecretConfig.token();

    private static volatile VaultClient vaultClientInstance;

    private VaultSecretUtil() {
    }

    private static final class VaultClient {

        private final Vault vault;

        private VaultClient() {
            if (vaultConfigEnabled()) {
                VaultConfig config;

                try {
                    config = new VaultConfig()
                            .address(VAULT_CONFIG.address())
                            .token(VAULT_CONFIG.token())
                            .engineVersion(2)
                            .openTimeout(5)
                            .readTimeout(30)
                            .build();
                } catch (VaultException e) {
                    throw new RuntimeException(e);
                }

                vault = Vault.create(config);
            } else {
                vault = null;
            }
        }
    }

    private static VaultClient getVaultClientInstance() {
        if (vaultClientInstance == null) {
            synchronized (VaultSecretUtil.class) {
                if (vaultClientInstance == null) {
                    vaultClientInstance = new VaultClient();
                }
            }
        }

        return vaultClientInstance;
    }

    private static Map<String, String> getSecretsData(String secretPath) {
        try {
            Map<String, String> data = getVaultClientInstance()
                    .vault
                    .logical()
                    .read(secretPath)
                    .getData();
            if (data == null || data.isEmpty()) {
                throw new RuntimeException(
                        "Vault returned empty secret for path: " + secretPath
                );
            }
            return data;
        } catch (VaultException e) {
            throw new RuntimeException(
                    String.format(
                            "Error occurred while fetching secret by path %s",
                            secretPath
                    ),
                    e
            );
        }
    }

    private static String getSecretImpl(
            String secretPath,
            String secretKey
    ) {
        if (!vaultConfigEnabled()) {
            throw new RuntimeException("Vault token is not provided");
        }

        Map<String, String> secretData = getSecretsData(secretPath);

        return secretData.computeIfAbsent(secretKey, key -> {
            throw new RuntimeException(
                    String.format(
                            "Secret has not been found by secret path %s and key %s",
                            secretPath,
                            secretKey
                    )
            );
        });
    }

    private static boolean vaultConfigEnabled() {
        return VAULT_TOKEN != null && !VAULT_TOKEN.isBlank();
    }

    /**
     * Получает секретное значение из Vault по указанному пути и ключу.
     *
     * @param secretPath путь к секрету в Vault
     * @param secretKey ключ секрета
     * @return значение секрета
     * @throws RuntimeException если Vault не настроен или секрет не найден
     */
    public static String getSecret(
            String secretPath,
            String secretKey
    ) {
        return getSecretImpl(secretPath, secretKey);
    }

    /**
     * Получает секретное значение из Vault по указанному ключу.
     *
     * @param secretKey ключ секрета
     * @return значение секрета
     * @throws RuntimeException если Vault не настроен или секрет не найден
     */
    public static String getSecret(String secretKey) {
        return getSecretImpl(VAULT_CONFIG.secretPath(), secretKey);
    }

    /**
     * Получает все секреты из Vault по пути, указанному в конфиге.
     *
     * @return карта всех секретов по заданному пути,
     * либо пустая карта, если Vault не настроен
     */
    public static Map<String, String> getSecrets() {
        return vaultConfigEnabled()
                ? getSecretsData(VAULT_CONFIG.secretPath())
                : new HashMap<>();
    }

    /**
     * Собирает все секреты автотестов из нескольких путей Vault.
     */
    public static Map<String, String> getTestSecrets() {
        if (!vaultConfigEnabled()) {
            throw new IllegalStateException(
                    "VAULT_TOKEN не задан. Укажите токен в vault.properties, env или Run Configuration"
            );
        }

        Map<String, String> allSecrets = new HashMap<>();
        allSecrets.putAll(getSecretsData(VaultTestConfig.SecretPath.GATEZILLA));
        allSecrets.putAll(getSecretsData(VaultTestConfig.SecretPath.GRPC));
        allSecrets.putAll(getSecretsData(VaultTestConfig.SecretPath.DB));
        allSecrets.putAll(getSecretsData(VaultTestConfig.SecretPath.TEMPORAL));
        return allSecrets;
    }
}