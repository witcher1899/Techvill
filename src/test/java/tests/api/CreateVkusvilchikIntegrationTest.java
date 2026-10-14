package tests.api;


import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequest;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestDeliveryParams;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestOrderLine;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestOrderLineDiscount;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequestOrderLinePrice;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;
import static test_data.enums.OrderType.VKUSVILCHIK;
import static test_data.enums.OrderType.getCreationSource;
import static utils.TypeParsing.parse;


@Epic("Вкусвильчик")
@DisplayName("Интеграционные тесты для заказов типа «Вкусвильчик»")
@Feature("Создание заказа типа «Вкусвильчик» через gatezilla")
@Severity(SeverityLevel.CRITICAL)
public class CreateVkusvilchikIntegrationTest extends BaseClass {
    private CustomerorderCreateInternalOrderResponse response;


    @AllureId("81348")
    @DisplayName("Создание заказа 'Вкусвильчик' всех подтипов с одним товаром")
    @ParameterizedTest(name = "Источник: {0}")
    @ValueSource(ints = {78, 79, 96, 24})
    void createVkusvilchikWithOneItem(int creation_source) {
        CustomerorderCreateInternalOrderRequest request = new CustomerorderCreateInternalOrderRequest()
                .creationSource(creation_source)
                .assemblyComment("ФРОВ и полочные позиции: выбирать самые свежие. ДК: собирать целыми коробками, соблюдать ротацию")
                .phone("79990000000")
                .cardNumber("TEST0001")
                .sourceShopno(99888)
                .targetShopno(999)
                .deliveryParams(new CustomerorderCreateInternalOrderRequestDeliveryParams()
                        .comment("Somebody once told me")
                        .courierTechComment("Доставить до подъезда")
                        .supplyDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond()))
                        .supplyUntilDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS).getEpochSecond())))
                .isReplaceNullPrices(true)
                .orderType(14)
                .clientServiceId(3)
                .payWay(2)
                .assemblyTechComment("Собрать в отдельный пакет")
                .orderLines(List.of(
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .prodSinceDate(String.valueOf(Instant.now().getEpochSecond()))
                                .productId(647)
                                .quantity((double)1)
                                .sumOrdered((double)1)
                                .orderNum(1)
                                .warehouse(9882)
                                .isGreenPrice(false)
                                .manufacturerId(777)
                                .contractTypeId(22)
                                .partnerShopno(88777)
                                .discount(new CustomerorderCreateInternalOrderRequestOrderLineDiscount()
                                        .sum((double)100)
                                        .value((double)10))
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(100.01)
                                        .basePrice((double)100)
                                        .finalPrice((double)1000))
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
                    () -> assertEquals("internal_order_with_delivery", suborderFields.get(2), "Вместо 'internal_order_with_delivery' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getCardNumber(), suborderFields.get(3), "Вместо " + request.getCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(request.getSourceShopno(), suborderFields.get(4), "Вместо " + request.getSourceShopno() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals(request.getAssemblyComment(), suborderFields.get(6), "Вместо " + request.getAssemblyComment() + " записано " + suborderFields.get(6)),
                    () -> assertEquals(request.getAssemblyTechComment(), suborderFields.get(7), "Вместо " + request.getAssemblyTechComment() + " записано " + suborderFields.get(7)),
                    () -> assertEquals(request.getIsReplaceNullPrices(), suborderFields.get(8), "Вместо " + request.getIsReplaceNullPrices() + " записано " + suborderFields.get(8)),
                    () -> assertEquals(request.getPayWay(), suborderFields.get(9), "Вместо " + request.getPayWay() + " записано " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(getCreationSource(creation_source).getValue(), suborderFields.get(11), "Вместо " + getCreationSource(creation_source).getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderLines().get(0).getPrice().getFinalPrice()), suborderFields.get(12), "Вместо " + parse(request.getOrderLines().get(0).getPrice().getFinalPrice()) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, VKUSVILCHIK), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }


    @AllureId("81341")
    @DisplayName("Создание заказа 'Вкусвильчик' всех подтипов с двумя товарами")
    @ParameterizedTest(name = "Источник: {0}")
    @ValueSource(ints = {78, 79, 96, 24})
    void createVkusvilchikWithMultipleItems(int creation_source) {
        CustomerorderCreateInternalOrderRequest request = new CustomerorderCreateInternalOrderRequest()
                .creationSource(creation_source)
                .assemblyComment("ФРОВ и полочные позиции: выбирать самые свежие. ДК: собирать целыми коробками, соблюдать ротацию")
                .phone("79990000000")
                .cardNumber("TEST0001")
                .sourceShopno(99888)
                .targetShopno(999)
                .deliveryParams(new CustomerorderCreateInternalOrderRequestDeliveryParams()
                        .comment("Somebody once told me")
                        .courierTechComment("Доставить до подъезда")
                        .supplyDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond()))
                        .supplyUntilDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS).getEpochSecond())))
                .isReplaceNullPrices(true)
                .orderType(14)
                .clientServiceId(3)
                .payWay(2)
                .assemblyTechComment("Собрать в отдельный пакет")
                .orderLines(List.of(
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .prodSinceDate(String.valueOf(Instant.now().getEpochSecond()))
                                .productId(647)
                                .quantity((double)1)
                                .sumOrdered((double)1)
                                .orderNum(1)
                                .warehouse(9882)
                                .isGreenPrice(false)
                                .manufacturerId(777)
                                .contractTypeId(22)
                                .partnerShopno(88777)
                                .discount(new CustomerorderCreateInternalOrderRequestOrderLineDiscount()
                                        .sum((double)100)
                                        .value((double)10))
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(100.01)
                                        .basePrice((double)100)
                                        .finalPrice((double)1000)),
                        new CustomerorderCreateInternalOrderRequestOrderLine()
                                .amount(1)
                                .prodSinceDate(String.valueOf(Instant.now().getEpochSecond()))
                                .productId(647)
                                .quantity((double)1)
                                .sumOrdered((double)1)
                                .orderNum(1)
                                .warehouse(9882)
                                .isGreenPrice(false)
                                .manufacturerId(777)
                                .contractTypeId(22)
                                .partnerShopno(88777)
                                .discount(new CustomerorderCreateInternalOrderRequestOrderLineDiscount()
                                        .sum((double)0)
                                        .value((double)0))
                                .price(new CustomerorderCreateInternalOrderRequestOrderLinePrice()
                                        .baseSum(500.0)
                                        .basePrice((double)300)
                                        .finalPrice((double)900))
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
                    () -> assertEquals("internal_order_with_delivery", suborderFields.get(2), "Вместо 'internal_order_with_delivery' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getCardNumber(), suborderFields.get(3), "Вместо " + request.getCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(request.getSourceShopno(), suborderFields.get(4), "Вместо " + request.getSourceShopno() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals(request.getAssemblyComment(), suborderFields.get(6), "Вместо " + request.getAssemblyComment() + " записано " + suborderFields.get(6)),
                    () -> assertEquals(request.getAssemblyTechComment(), suborderFields.get(7), "Вместо " + request.getAssemblyTechComment() + " записано " + suborderFields.get(7)),
                    () -> assertEquals(request.getIsReplaceNullPrices(), suborderFields.get(8), "Вместо " + request.getIsReplaceNullPrices() + " записано " + suborderFields.get(8)),
                    () -> assertEquals(request.getPayWay(), suborderFields.get(9), "Вместо " + request.getPayWay() + " записано " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(getCreationSource(creation_source).getValue(), suborderFields.get(11), "Вместо " + getCreationSource(creation_source).getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderLines().get(0).getPrice().getFinalPrice()
                            * request.getOrderLines().get(0).getQuantity()
                            + request.getOrderLines().get(1).getPrice().getFinalPrice()
                            * request.getOrderLines().get(1).getQuantity()), suborderFields.get(12), "Вместо " + parse(request.getOrderLines().get(0).getPrice().getFinalPrice()
                            * request.getOrderLines().get(0).getQuantity()
                            + request.getOrderLines().get(1).getPrice().getFinalPrice()
                            * request.getOrderLines().get(1).getQuantity()) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, VKUSVILCHIK), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }
}
