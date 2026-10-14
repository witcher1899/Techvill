package connections;

import config.TestsConfig;
import config.TestsConfigReader;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import test_data.enums.Services;

import java.util.Objects;

public class ChannelConnection {

    public ManagedChannel createGRPCChannel(Services service) {
        TestsConfig config = TestsConfigReader.testsConfig();
        int port = Integer.parseInt(config.grpcPort());

        switch (service) {
            case GENRY:
                return ManagedChannelBuilder
                        .forAddress(
                                requireHost(config.genryHost(), "genry_host"),
                                port)
                        .useTransportSecurity()
                        .build();
            case HARDCORE:
                return ManagedChannelBuilder
                        .forAddress(
                                requireHost(config.hardcoreHost(), "hardcore_host"),
                                port)
                        .useTransportSecurity()
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported GRPC service: " + service);
        }
    }

    private static String requireHost(String host, String key) {
        return Objects.requireNonNull(
                host,
                "Секрет '" + key + "' не найден. Проверьте VAULT_TOKEN и путь sandbox-qa/grpc_uz2"
        );
    }
}
