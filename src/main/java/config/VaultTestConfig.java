package config;


public final class VaultTestConfig {

    private VaultTestConfig() {
    }

    public static final class SecretPath {
        public static final String DB = "sandbox-qa/db_uz2";
        public static final String GATEZILLA = "sandbox-qa/gatezilla";
        public static final String GRPC = "sandbox-qa/grpc_uz2";
        public static final String TEMPORAL = "sandbox-qa/temporal";

        private SecretPath() {
        }
    }
}
