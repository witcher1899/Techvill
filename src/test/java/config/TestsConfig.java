package config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:env",
        "classpath:tests.properties"
})
public interface TestsConfig extends Config {

    @Key("gatezilla_host")
    String gatezillaHost();

    @Key("grpc_port")
    @Config.DefaultValue("443")
    String grpcPort();

    @Key("genry_host")
    String genryHost();

    @Key("hardcore_host")
    String hardcoreHost();

    @Key("url_mssql")
    String urlMssql();

    @Key("user_mssql")
    String userMssql();

    @Key("password_mssql")
    String passwordMssql();

    @Key("url_oms_core")
    String urlOmsCore();

    @Key("user_oms_core")
    String userOmsCore();

    @Key("password_oms_core")
    String passwordOmsCore();

    @Key("temporal_host")
    String temporalHost();
}
