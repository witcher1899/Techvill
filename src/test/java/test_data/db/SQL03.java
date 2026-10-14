package test_data.db;


import connections.DBConnection;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import java.sql.Connection;
import java.sql.SQLException;
import static org.jooq.impl.DSL.*;
import static test_data.enums.DBSName.MSSQL;


public class SQL03 {


    public boolean isOrderExist(String id_order) throws SQLException {

        try (Connection dbConnection = DBConnection.createConnection(MSSQL)) {
            DSLContext dsl = using(dbConnection, SQLDialect.DEFAULT);

            var orders_header = table(name("orders_header"));
            var orders_header_row = dsl.select(field(name("id_order"), String.class))
                    .from(orders_header)
                    .where(field(name("id_order"), String.class).eq(id_order))
                    .fetchOne();

            return orders_header_row != null;
        }
    }
}
