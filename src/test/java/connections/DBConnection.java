package connections;


import config.TestsConfig;
import config.TestsConfigReader;
import test_data.enums.DBSName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class DBConnection {

    private DBConnection() {
    }

    public static Connection createConnection(DBSName dbsName) throws SQLException {
        TestsConfig config = TestsConfigReader.testsConfig();

        switch (dbsName) {
            case MSSQL:
                return DriverManager.getConnection(
                        config.urlMssql(),
                        config.userMssql(),
                        config.passwordMssql());
            case OMS_CORE:
                return DriverManager.getConnection(
                        config.urlOmsCore(),
                        config.userOmsCore(),
                        config.passwordOmsCore());
            default:
                throw new IllegalArgumentException("Unsupported DB name: " + dbsName);
        }
    }
}
