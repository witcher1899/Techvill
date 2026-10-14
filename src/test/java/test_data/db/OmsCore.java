package test_data.db;


import connections.DBConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.SQLDataType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.jooq.impl.DSL.*;
import static test_data.enums.DBSName.OMS_CORE;


public class OmsCore {

    public String getSuborderStateFields(String order_id) throws SQLException, InterruptedException {
        try (Connection dbConnection = DBConnection.createConnection(OMS_CORE)) {
            DSLContext dsl = using(dbConnection, SQLDialect.POSTGRES);

            var ss = table(name("oms", "suborder_state")).as("ss");
            var s = table(name("oms", "suborder")).as("s");

            var suborder_state_rows = dsl.select(field(name("ss", "status"), String.class))
                    .from(ss)
                    .join(s).on(field(name("ss", "suborder_id"))
                            .eq(field(name("s", "id"))))
                    .where(field(name("s", "external_id"), String.class).eq(order_id))
                    .orderBy(field(name("ss", "created_at")))
                    .fetch();

            return suborder_state_rows.get(0).get("status", String.class);
        }

    }


    public List<Object> getSuborderFields(String order_id) throws SQLException {
        try (Connection dbConnection = DBConnection.createConnection(OMS_CORE)) {
            DSLContext dsl = using(dbConnection, SQLDialect.POSTGRES);
            List<Object> suborder_result = new ArrayList<>();

            var s = table(name("oms", "suborder")).as("s");
            var suborder_row = dsl.selectFrom(s)
                    .where(field(name("s", "external_id"), String.class).eq(order_id))
                    .orderBy(field(name("s", "created_at")))
                    .fetchOne();

            suborder_result.add(suborder_row.get("external_id", String.class));
            suborder_result.add(suborder_row.get("legacy_id", String.class));
            suborder_result.add(suborder_row.get("order_type", String.class));
            suborder_result.add(suborder_row.get("loyalty_card_number", String.class));
            suborder_result.add(suborder_row.get("shop_number", Integer.class));
            suborder_result.add(suborder_row.get("created_at", OffsetDateTime.class));
            suborder_result.add(suborder_row.get("assembly_comment", String.class));
            suborder_result.add(suborder_row.get("assembly_tech_comment", String.class));
            suborder_result.add(suborder_row.get("is_replace_null_prices", Boolean.class));
            suborder_result.add(suborder_row.get("pay_way", Integer.class));
            suborder_result.add(suborder_row.get("partner_shop_number", Integer.class));
            suborder_result.add(suborder_row.get("app_name", String.class));
            suborder_result.add(suborder_row.get("order_sum", BigDecimal.class));
            suborder_result.add(suborder_row.get("user_view", String.class));
            suborder_result.add(suborder_row.get("cancel_comment", String.class));

            return suborder_result;
        }
    }


    public List<Object> getDeliveryDetailFields(String order_id) throws SQLException {
        try (Connection dbConnection = DBConnection.createConnection(OMS_CORE)) {
            DSLContext dsl = using(dbConnection, SQLDialect.POSTGRES);
            List<Object> delivery_detail_result = new ArrayList<>();

            var dd = table(name("oms", "delivery_detail")).as("dd");
            var s = table(name("oms", "suborder")).as("s");
            Field<Object> details = field(name("dd", "details"));

            var row = dsl.select(
                            field("({0}::jsonb)->>{1}", String.class, details, inline("flat")).as("flat"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("floor")).as("floor"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("entrance")).as("entrance"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("latitude")).as("latitude"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("doorphone")).as("doorphone"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("longitude")).as("longitude"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("supply_date")).as("supply_date"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("full_address")).as("full_address"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("delivery_comment")).as("delivery_comment"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("client_service_id")).as("client_service_id"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("supply_until_date")).as("supply_until_date"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("target_shop_number")).as("target_shop_number"),
                            field("({0}::jsonb)->>{1}", String.class, details, inline("delivery_tech_comment")).as("delivery_tech_comment"))
                    .from(dd)
                    .join(s).on(field(name("dd", "suborder_id"))
                            .eq(field(name("s", "id"))))
                    .where(field(name("s", "external_id"), String.class).eq(order_id))
                    .orderBy(field(name("dd", "suborder_id")))
                    .fetchOne();

            delivery_detail_result.add(row.get("flat", String.class));
            delivery_detail_result.add(row.get("floor", String.class));
            delivery_detail_result.add(row.get("entrance", String.class));
            delivery_detail_result.add(row.get("latitude", String.class));
            delivery_detail_result.add(row.get("doorphone", String.class));
            delivery_detail_result.add(row.get("longitude", String.class));
            delivery_detail_result.add(row.get("supply_date", String.class));
            delivery_detail_result.add(row.get("full_address", String.class));
            delivery_detail_result.add(row.get("delivery_comment", String.class));
            delivery_detail_result.add(row.get("client_service_id", String.class));
            delivery_detail_result.add(row.get("supply_until_date", String.class));
            delivery_detail_result.add(row.get("target_shop_number", String.class));
            delivery_detail_result.add(row.get("delivery_tech_comment", String.class));

            return delivery_detail_result;
        }
    }


    public List<Object> getPaymentDetailFields(String order_id) throws SQLException {
        try (Connection dbConnection = DBConnection.createConnection(OMS_CORE)) {
            DSLContext dsl = using(dbConnection, SQLDialect.POSTGRES);
            List<Object> payment_detail_result = new ArrayList<>();

            var pd = table(name("oms", "payment_detail")).as("pd");
            var s = table(name("oms", "suborder")).as("s");

            var payment_detail_row = dsl.select(pd.asterisk())
                    .from(pd)
                    .join(s).on(field(name("pd", "suborder_id"))
                            .eq(field(name("s", "id"))))
                    .where(field(name("s", "external_id"), String.class).eq(order_id))
                    .fetchOne();

            payment_detail_result.add(payment_detail_row.get("payment_card", String.class));
            payment_detail_result.add(payment_detail_row.get("email", String.class));
            payment_detail_result.add(payment_detail_row.get("phone", String.class));
            payment_detail_result.add(payment_detail_row.get("created_at", OffsetDateTime.class));

            return payment_detail_result;
        }
    }


    public Map<String, List<Object>> getOrderedItemFields(String order_id) throws SQLException {
        try (Connection dbConnection = DBConnection.createConnection(OMS_CORE)) {
            DSLContext dsl = using(dbConnection, SQLDialect.POSTGRES);
            List<Object> first_item = new ArrayList<>();
            List<Object> second_item = new ArrayList<>();
            Map<String, List<Object>> ordered_item_result = new HashMap<>();

            var oi = table(name("oms", "ordered_item")).as("oi");
            var s = table(name("oms", "suborder")).as("s");

            var ordered_item_rows = dsl.select(oi.asterisk())
                    .from(oi)
                    .join(s).on(field(name("oi", "suborder_id"))
                            .eq(field(name("s", "id"))))
                    .where(field(name("s", "legacy_id"))
                            .eq(cast(val(order_id), SQLDataType.BIGINT)))
                    .fetch();

            int count_row = 0;

            for (var ordered_item_row : ordered_item_rows) {
                count_row++;

                List<Object> item;
                if (count_row == 1) {
                    item = first_item;
                } else if (count_row == 2) {
                    item = second_item;
                } else {
                    continue;
                }

                item.add(ordered_item_row.get("product_id", String.class));
                item.add(ordered_item_row.get("quantity", BigDecimal.class));
                item.add(ordered_item_row.get("base_price", BigDecimal.class));
                item.add(ordered_item_row.get("base_sum", BigDecimal.class));
                item.add(ordered_item_row.get("total", BigDecimal.class));
                item.add(ordered_item_row.get("created_at", OffsetDateTime.class));
                item.add(ordered_item_row.get("quantity_units", Integer.class));
                item.add(ordered_item_row.get("is_green_price", Boolean.class));
                item.add(ordered_item_row.get("min_production_date", OffsetDateTime.class));
                item.add(ordered_item_row.get("manufacturer_id", Integer.class));
                item.add(ordered_item_row.get("contract_type_id", Integer.class));
                item.add(ordered_item_row.get("partner_shop_number", Integer.class));
                item.add(ordered_item_row.get("quantity_ordered", BigDecimal.class));
                item.add(ordered_item_row.get("ordered_sum", BigDecimal.class));
                item.add(ordered_item_row.get("warehouse_id", Integer.class));
                item.add(ordered_item_row.get("discount_type_id", Integer.class));
                item.add(ordered_item_row.get("discount_percentage", BigDecimal.class));
                item.add(ordered_item_row.get("discount_sum", BigDecimal.class));
                item.add(ordered_item_row.get("discount_value", BigDecimal.class));
                item.add(ordered_item_row.get("marking_data_matrix", String.class));
                item.add(ordered_item_row.get("final_price", BigDecimal.class));
            }

            ordered_item_result.put("first_item", first_item);
            ordered_item_result.put("second_item", second_item);

            return ordered_item_result;
        }
    }

    public List<Object> getOrderedItemFirstItem(String order_id) throws SQLException {
        return getOrderedItemFields(order_id).get("first_item");
    }

    public List<Object> getOrderedItemSecondItem(String order_id) throws SQLException {
        return getOrderedItemFields(order_id).get("second_item");
    }
}
