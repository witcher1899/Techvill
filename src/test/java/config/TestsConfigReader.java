package config;

import org.aeonbits.owner.ConfigFactory;
import utils.VaultSecretUtil;


public final class TestsConfigReader {

    private static volatile TestsConfig config;

    private TestsConfigReader() {
    }

    public static TestsConfig testsConfig() {
        TestsConfig local = config;
        if (local == null) {
            synchronized (TestsConfigReader.class) {
                local = config;
                if (local == null) {
                    config = local = ConfigFactory.create(
                            TestsConfig.class,
                            System.getProperties(),
                            System.getenv(),
                            VaultSecretUtil.getTestSecrets()
                    );
                }
            }
        }
        return local;
    }
}
