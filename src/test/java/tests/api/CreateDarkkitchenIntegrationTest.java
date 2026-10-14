package tests.api;


import io.qameta.allure.AllureId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequest;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestDeliveryParams;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestOrderLine;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestOrderLinePrice;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderResponse;
import test_data.enums.OrderType;
import java.time.Instant;
import java.util.List;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;
import static test_data.enums.OrderType.DARKKITCHEN;
import static utils.TypeParsing.parse;


public class CreateDarkkitchenIntegrationTest extends BaseClass {
    private CustomerorderCreateInternalOrderResponse response;


    @AllureId("81348")
    @DisplayName("Создание заказа 'Дарккитчены' с одним товаром")
    @Test
    void createDarkkitchenWithOneItem() {
        CustomerorderCreateInternalOrderRequest request = new CustomerorderCreateInternalOrderRequest()
                .creationSource(80)
                .assemblyComment("ФРОВ и полочные позиции: выбирать самые свежие. ДК: собирать целыми коробками, соблюдать ротацию")
                .phone("79990000000")
                .cardNumber("TEST0001")
                .sourceShopno(99888)
                .targetShopno(999)
                .deliveryParams(new CustomerorderCreateInternalOrderRequestDeliveryParams()
                        .comment("Somebody once told me"))
                .isReplaceNullPrices(true)
                .orderType(14)
                .clientServiceId(3)
                .orderLines(List.of(
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .amountOrdered(1)
                                .amountDecimal((double) 10)
                                .prodSinceDate(String.valueOf(Instant.now().getEpochSecond()))
                                .productId(647)
                                .quantity((double) 1)
                                .quantityOrdered((double) 1)
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(0.01))
                ));

        step("Вызов ручки /v2/orders/internal", () -> {
            response = getInternalOrderBody(request);
        });

        step("Проверка HTTP code и наличие тела ответа", () -> {
            assertNotNull(response, "В ответе должно быть тело");
            assertNotNull(response.getOrderId(), "В ответе должен быть order_id");
            suborder_id = response.getOrderId().toString();
        });

        step("Проверка записи в таблице oms.suborder_state", () -> {
            assertEquals("ORDER_POSTED", omsCore.getSuborderStateFields(suborder_id),
                    "Вместо ORDER_POSTED статус заказа равен " + omsCore.getSuborderStateFields(suborder_id));
        });

        step("Проверка записи в таблице oms.suborder", () -> {
            List<Object> suborderFields = omsCore.getSuborderFields(suborder_id);
            assertAll("Проверка таблицы oms.suborder",
                    () -> assertEquals(suborder_id, suborderFields.get(0), "Вместо " + suborder_id + " записано " + suborderFields.get(0)),
                    () -> assertEquals(suborder_id, suborderFields.get(1), "Вместо " + suborder_id + " записано " + suborderFields.get(1)),
                    () -> assertEquals("darkkitchen", suborderFields.get(2), "Вместо 'darkkitchen' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getCardNumber(), suborderFields.get(3), "Вместо " + request.getCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(request.getSourceShopno(), suborderFields.get(4), "Вместо " + request.getSourceShopno() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals(request.getAssemblyComment(), suborderFields.get(6), "Вместо " + request.getAssemblyComment() + " записано " + suborderFields.get(6)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertEquals(request.getIsReplaceNullPrices(), suborderFields.get(8), "Вместо " + request.getIsReplaceNullPrices() + " записано " + suborderFields.get(8)),
                    () -> assertEquals(2, suborderFields.get(9), "Вместо 2 записано " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(OrderType.getCreationSource(80).getValue(), suborderFields.get(11), "Вместо " + OrderType.getCreationSource(80).getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderLines().get(0).getPrice().getBaseSum()), suborderFields.get(12), "Вместо " + parse(request.getOrderLines().get(0).getPrice().getBaseSum()) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, DARKKITCHEN), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }


    @AllureId("81341")
    @DisplayName("Создание заказа 'Дарккитчены' с двумя товарами")
    @Test
    void createDarkkitchenWithTwoItems() {
        CustomerorderCreateInternalOrderRequest request = new CustomerorderCreateInternalOrderRequest()
                .creationSource(80)
                .assemblyComment("ФРОВ и полочные позиции: выбирать самые свежие. ДК: собирать целыми коробками, соблюдать ротацию")
                .phone("79990000000")
                .cardNumber("TEST0001")
                .sourceShopno(99888)
                .targetShopno(999)
                .deliveryParams(new CustomerorderCreateInternalOrderRequestDeliveryParams()
                        .comment("Somebody once told me"))
                .isReplaceNullPrices(true)
                .orderType(14)
                .clientServiceId(3)
                .orderLines(List.of(
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .amountOrdered(1)
                                .amountDecimal((double) 10)
                                .prodSinceDate(String.valueOf(Instant.now().getEpochSecond()))
                                .productId(647)
                                .quantity((double) 1)
                                .quantityOrdered((double) 1)
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(0.01)),
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .amountOrdered(1)
                                .amountDecimal((double) 0)
                                .productId(39098)
                                .quantity(2.5)
                                .quantityOrdered(2.5)
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(0.01))
                ));

        step("Вызов ручки /v2/orders/internal", () -> {
            response = getInternalOrderBody(request);
        });

        step("Проверка HTTP code и наличие тела ответа", () -> {
            assertNotNull(response, "В ответе должно быть тело");
            assertNotNull(response.getOrderId(), "В ответе должен быть order_id");
            suborder_id = response.getOrderId().toString();
        });

        step("Проверка записи в таблице oms.suborder_state", () -> {
            assertEquals("ORDER_POSTED", omsCore.getSuborderStateFields(suborder_id),
                    "Вместо ORDER_POSTED статус заказа равен " + omsCore.getSuborderStateFields(suborder_id));
        });

        step("Проверка записи в таблице oms.suborder", () -> {
            List<Object> suborderFields = omsCore.getSuborderFields(suborder_id);
            assertAll("Проверка таблицы oms.suborder",
                    () -> assertEquals(suborder_id, suborderFields.get(0), "Вместо " + suborder_id + " записано " + suborderFields.get(0)),
                    () -> assertEquals(suborder_id, suborderFields.get(1), "Вместо " + suborder_id + " записано " + suborderFields.get(1)),
                    () -> assertEquals("darkkitchen", suborderFields.get(2), "Вместо 'darkkitchen' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getCardNumber(), suborderFields.get(3), "Вместо " + request.getCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(request.getSourceShopno(), suborderFields.get(4), "Вместо " + request.getSourceShopno() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals(request.getAssemblyComment(), suborderFields.get(6), "Вместо " + request.getAssemblyComment() + " записано " + suborderFields.get(6)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertEquals(request.getIsReplaceNullPrices(), suborderFields.get(8), "Вместо " + request.getIsReplaceNullPrices() + " записано " + suborderFields.get(8)),
                    () -> assertEquals(2, suborderFields.get(9), "Вместо 2 записано " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(OrderType.getCreationSource(80).getValue(), suborderFields.get(11), "Вместо " + OrderType.getCreationSource(80).getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderLines().get(0).getPrice().getBaseSum()
                            + request.getOrderLines().get(1).getPrice().getBaseSum()), suborderFields.get(12), "Вместо " + parse(request.getOrderLines().get(0).getPrice().getBaseSum()
                            + request.getOrderLines().get(1).getPrice().getBaseSum()) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, DARKKITCHEN), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }
}
