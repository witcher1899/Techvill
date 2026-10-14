package config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:env",
        "classpath:vault.properties"
})
public interface VaultSecretConfig extends Reloadable {

    @Key("VAULT_ADDR")
    String address();

    @Key("VAULT_TOKEN")
    String token();

    @Key("SECRET_PATH")
    String secretPath();
}