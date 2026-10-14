package tests.api;


import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vkusvill.generated.model.CreateWaterOrderV1RequestDeliveryDetails;
import ru.vkusvill.generated.model.DeliveryDetailsTargetAddress;
import ru.vkusvill.generated.model.HardcoreCreateWaterOrderV1Request;
import ru.vkusvill.generated.model.HardcoreCreateWaterOrderV1RequestOrderedItems;
import ru.vkusvill.generated.model.HardcoreCreateWaterOrderV1Response;
import ru.vkusvill.generated.model.MsgAppName;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;
import static test_data.enums.OrderType.WATER;
import static utils.TypeParsing.parse;


@Epic("Вода")
@DisplayName("Интеграционные тесты для заказов типа «Вода»")
@Feature("Создание заказа типа «Вода» через gatezilla")
@Severity(SeverityLevel.CRITICAL)
public class CreateWaterIntegrationTest extends BaseClass {
    private HardcoreCreateWaterOrderV1Response response;


    @AllureId("84840")
    @DisplayName("Создание заказа типа 'Вода' с одним товаром")
    @Test
    void createOneWaterItem() {
        HardcoreCreateWaterOrderV1Request request = new HardcoreCreateWaterOrderV1Request()
                .loyaltyCardNumber("TEST0001")
                .paymentCardId("106")
                .shopNumber("999")
                .partnerShopNumber("99888")
                .email("test@mail.ru")
                .appName(MsgAppName.WEBSITE)
                .comment("Доставить до двери")
                .deliveryDetails(new CreateWaterOrderV1RequestDeliveryDetails()
                        .supplyDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond()))
                        .supplyUntilDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS).getEpochSecond()))
                        .targetAddress(new DeliveryDetailsTargetAddress()
                                .fullAddress("г. Озерск, ул. Октябрьская, д. 10, кв. 42")
                                .latitude(23.344)
                                .longitude(22.444)
                                .flat("42")
                                .doorphone("42")
                                .entrance("3")
                                .floor("4")))
                .orderedItems(List.of(
                        new HardcoreCreateWaterOrderV1RequestOrderedItems()
                                .productId("647")
                                .quantity("1")
                                .manufacturerId("4")
                                .price(100.0)
                ));

        step("Вызов ручки /v3/orders/water", () -> {
            response = getWaterBody(request);
        });

        step("Проверка HTTP code и наличие тела ответа", () -> {
            assertNotNull(response, "В ответе должно быть тело");
            assertNotNull(response.getSuborderId(), "В ответе должен быть suborder_id");
            suborder_id = response.getSuborderId();
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
                    () -> assertEquals("water", suborderFields.get(2), "Вместо 'water' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getLoyaltyCardNumber(), suborderFields.get(3), "Вместо " + request.getLoyaltyCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(Integer.valueOf(request.getShopNumber()), suborderFields.get(4), "Вместо " + request.getShopNumber() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals("", suborderFields.get(6), "Записалось значение " + suborderFields.get(10)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertNull(suborderFields.get(8), "Записалось значение " + suborderFields.get(8)),
                    () -> assertNull(suborderFields.get(9), "Записалось значение " + suborderFields.get(9)),
                    () -> assertEquals(Integer.valueOf(request.getPartnerShopNumber()), suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(WATER.getValue(), suborderFields.get(11), "Вместо " + WATER.getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())), suborderFields.get(12), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка записи в таблице oms.payment_detail", () -> {
            List<Object> paymentDetailFields = omsCore.getPaymentDetailFields(suborder_id);
            assertAll("Проверка таблицы oms.payment_detail",
                    () -> assertEquals(request.getPaymentCardId(), paymentDetailFields.get(0), "Вместо " + request.getPaymentCardId() + " записано " + paymentDetailFields.get(0)),
                    () -> assertEquals(request.getEmail(), paymentDetailFields.get(1), "Вместо " + request.getEmail() + " записано " + paymentDetailFields.get(1)),
                    () -> assertEquals("", paymentDetailFields.get(2), "Записалось значение " + paymentDetailFields.get(2)),
                    () -> assertNotNull(paymentDetailFields.get(3), "Записалось пустое значение")
            );
        });

        step("Проверка записи в таблице oms.ordered_item", () -> {
            List<Object> orderedItem = omsCore.getOrderedItemFirstItem(suborder_id);
            assertAll("Проверка таблицы oms.ordered_item",
                    () -> assertEquals(request.getOrderedItems().get(0).getProductId(), orderedItem.get(0), "Вместо " + request.getOrderedItems().get(0).getProductId() + " записано " + orderedItem.get(0)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getQuantity()), orderedItem.get(1), "Вместо " + parse(request.getOrderedItems().get(0).getQuantity()) + " записано " + orderedItem.get(1)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice()), orderedItem.get(2), "Вместо " + parse(request.getOrderedItems().get(0).getPrice()) + " записано " + orderedItem.get(2)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())), orderedItem.get(3), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())) + " записано " + orderedItem.get(3)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())), orderedItem.get(4), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())) + " записано " + orderedItem.get(4)),
                    () -> assertNotNull(orderedItem.get(5), "Записалось пустое значение"),
                    () -> assertEquals(0, orderedItem.get(6), "Записалось значение " + orderedItem.get(6) + " вместо 0"),
                    () -> assertNotNull(orderedItem.get(8), "Записалось пустое значение"),
                    () -> assertEquals(request.getOrderedItems().get(0).getManufacturerId(), String.valueOf(orderedItem.get(9)), "Вместо " + request.getOrderedItems().get(0).getManufacturerId() + " записано " + orderedItem.get(9)),
                    () -> assertNull(orderedItem.get(10), "Записалось значение " + orderedItem.get(10)),
                    () -> assertNull(orderedItem.get(11), "Записалось значение " + orderedItem.get(11)),
                    () -> assertNull(orderedItem.get(12), "Записалось значение " + orderedItem.get(12)),
                    () -> assertNull(orderedItem.get(13), "Записалось значение " + orderedItem.get(13)),
                    () -> assertNull(orderedItem.get(14), "Записалось значение " + orderedItem.get(14)),
                    () -> assertNull(orderedItem.get(15), "Записалось значение " + orderedItem.get(15)),
                    () -> assertNull(orderedItem.get(16), "Записалось значение " + orderedItem.get(16)),
                    () -> assertNull(orderedItem.get(17), "Записалось значение " + orderedItem.get(17)),
                    () -> assertNull(orderedItem.get(18), "Записалось значение " + orderedItem.get(18)),
                    () -> assertNull(orderedItem.get(19), "Записалось значение " + orderedItem.get(19)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice()), orderedItem.get(20), "Вместо " + parse(request.getOrderedItems().get(0).getPrice()) + " записано " + orderedItem.get(20))
            );
        });

        step("Проверка записи в таблице oms.delivery_detail", () -> {
            List<Object> deliveryDetailFields = omsCore.getDeliveryDetailFields(suborder_id);
            assertAll("Проверка таблицы oms.delivery_detail",
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFlat(), deliveryDetailFields.get(0), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFlat() + " записано " + deliveryDetailFields.get(0)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFloor(), deliveryDetailFields.get(1), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFloor() + " записано " + deliveryDetailFields.get(1)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getEntrance(), deliveryDetailFields.get(2), "Вместо " + request.getDeliveryDetails().getTargetAddress().getEntrance() + " записано " + deliveryDetailFields.get(2)),
                    () -> assertEquals(String.valueOf(request.getDeliveryDetails().getTargetAddress().getLatitude()), deliveryDetailFields.get(3), "Вместо " + request.getDeliveryDetails().getTargetAddress().getLatitude() + " записано " + deliveryDetailFields.get(3)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getDoorphone(), deliveryDetailFields.get(4), "Вместо " + request.getDeliveryDetails().getTargetAddress().getDoorphone() + " записано " + deliveryDetailFields.get(4)),
                    () -> assertEquals(String.valueOf(request.getDeliveryDetails().getTargetAddress().getLongitude()), deliveryDetailFields.get(5), "Вместо " + request.getDeliveryDetails().getTargetAddress().getLongitude() + " записано " + deliveryDetailFields.get(5)),
                    () -> assertEquals(Instant.ofEpochSecond(Long.parseLong(request.getDeliveryDetails().getSupplyDate())).toString(), deliveryDetailFields.get(6),
                            "Вместо " + request.getDeliveryDetails().getSupplyDate() + " записано " + deliveryDetailFields.get(6)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFullAddress(), deliveryDetailFields.get(7), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFullAddress() + " записано " + deliveryDetailFields.get(7)),
                    () -> assertEquals(request.getComment(), deliveryDetailFields.get(8), "Вместо " + request.getComment() + " записано " + deliveryDetailFields.get(8)),
                    () -> assertEquals("0", deliveryDetailFields.get(9), "Вместо 0 записано " + deliveryDetailFields.get(9)),
                    () -> assertEquals(Instant.ofEpochSecond(Long.parseLong(request.getDeliveryDetails().getSupplyUntilDate())).toString(), deliveryDetailFields.get(10),
                            "Вместо " + request.getDeliveryDetails().getSupplyUntilDate() + " записано " + deliveryDetailFields.get(10)),
                    () -> assertEquals("0", deliveryDetailFields.get(11), "Вместо 0 записано " + deliveryDetailFields.get(11)),
                    () -> assertNull(deliveryDetailFields.get(12), "Записалось значение " + deliveryDetailFields.get(12))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, WATER), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }


    @Test
    @DisplayName("Создание заказа типа 'Вода' с двумя товарами")
    void createMultipleWaterItem() {
        HardcoreCreateWaterOrderV1Request request = new HardcoreCreateWaterOrderV1Request()
                .loyaltyCardNumber("TEST0001")
                .paymentCardId("106")
                .shopNumber("999")
                .partnerShopNumber("99888")
                .email("test@mail.ru")
                .appName(MsgAppName.WEBSITE)
                .comment("Доставить до двери")
                .deliveryDetails(new CreateWaterOrderV1RequestDeliveryDetails()
                        .supplyDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond()))
                        .supplyUntilDate(String.valueOf(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS).getEpochSecond()))
                        .targetAddress(new DeliveryDetailsTargetAddress()
                                .fullAddress("г. Озерск, ул. Октябрьская, д. 10, кв. 42")
                                .latitude(23.344)
                                .longitude(22.444)
                                .flat("42")
                                .doorphone("42")
                                .entrance("3")
                                .floor("4")))
                .orderedItems(List.of(
                        new HardcoreCreateWaterOrderV1RequestOrderedItems()
                                .productId("647")
                                .quantity("1")
                                .manufacturerId("4")
                                .price(100.0),
                        new HardcoreCreateWaterOrderV1RequestOrderedItems()
                                .productId("222")
                                .quantity("3")
                                .manufacturerId("6")
                                .price(3000.0)
                ));

        step("Вызов ручки /v3/orders/water", () -> {
            response = getWaterBody(request);
        });

        step("Проверка HTTP code и наличие тела ответа", () -> {
            assertNotNull(response, "В ответе должно быть тело");
            assertNotNull(response.getSuborderId(), "В ответе должен быть suborder_id");
            suborder_id = response.getSuborderId();
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
                    () -> assertEquals("water", suborderFields.get(2), "Вместо 'water' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getLoyaltyCardNumber(), suborderFields.get(3), "Вместо " + request.getLoyaltyCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(Integer.valueOf(request.getShopNumber()), suborderFields.get(4), "Вместо " + request.getShopNumber() + " записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals("", suborderFields.get(6), "Записалось значение " + suborderFields.get(10)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertNull(suborderFields.get(8), "Записалось значение " + suborderFields.get(8)),
                    () -> assertNull(suborderFields.get(9), "Записалось значение " + suborderFields.get(9)),
                    () -> assertEquals(Integer.valueOf(request.getPartnerShopNumber()), suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(WATER.getValue(), suborderFields.get(11), "Вместо " + WATER.getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())
                            + request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())), suborderFields.get(12), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())
                            + request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())) + " записано " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записано " + suborderFields.get(13)),
                    () -> assertEquals("", suborderFields.get(14), "Записалось значение: " + suborderFields.get(14))
            );
        });

        step("Проверка записи в таблице oms.payment_detail", () -> {
            List<Object> paymentDetailFields = omsCore.getPaymentDetailFields(suborder_id);
            assertAll("Проверка таблицы oms.payment_detail",
                    () -> assertEquals(request.getPaymentCardId(), paymentDetailFields.get(0), "Вместо " + request.getPaymentCardId() + " записано " + paymentDetailFields.get(0)),
                    () -> assertEquals(request.getEmail(), paymentDetailFields.get(1), "Вместо " + request.getEmail() + " записано " + paymentDetailFields.get(1)),
                    () -> assertEquals("", paymentDetailFields.get(2), "Записалось значение " + paymentDetailFields.get(2)),
                    () -> assertNotNull(paymentDetailFields.get(3), "Записалось пустое значение")
            );
        });

        step("Проверка записи в таблице oms.ordered_item", () -> {
            List<Object> orderedItemFirst = omsCore.getOrderedItemFirstItem(suborder_id);
            assertAll("Проверка таблицы oms.ordered_item для первого товара",
                    () -> assertEquals(request.getOrderedItems().get(0).getProductId(), orderedItemFirst.get(0), "Вместо " + request.getOrderedItems().get(0).getProductId() + " записано " + orderedItemFirst.get(0)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getQuantity()), orderedItemFirst.get(1), "Вместо " + parse(request.getOrderedItems().get(0).getQuantity()) + " записано " + orderedItemFirst.get(1)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice()), orderedItemFirst.get(2), "Вместо " + parse(request.getOrderedItems().get(0).getPrice()) + " записано " + orderedItemFirst.get(2)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())), orderedItemFirst.get(3), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())) + " записано " + orderedItemFirst.get(3)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())), orderedItemFirst.get(4), "Вместо " + parse(request.getOrderedItems().get(0).getPrice() * Double.parseDouble(request.getOrderedItems().get(0).getQuantity())) + " записано " + orderedItemFirst.get(4)),
                    () -> assertNotNull(orderedItemFirst.get(5), "Записалось пустое значение"),
                    () -> assertEquals(0, orderedItemFirst.get(6), "Записалось значение " + orderedItemFirst.get(6) + " вместо 0"),
                    () -> assertNotNull(orderedItemFirst.get(8), "Записалось пустое значение"),
                    () -> assertEquals(request.getOrderedItems().get(0).getManufacturerId(), String.valueOf(orderedItemFirst.get(9)), "Вместо " + request.getOrderedItems().get(0).getManufacturerId() + " записано " + orderedItemFirst.get(9)),
                    () -> assertNull(orderedItemFirst.get(10), "Записалось значение " + orderedItemFirst.get(10)),
                    () -> assertNull(orderedItemFirst.get(11), "Записалось значение " + orderedItemFirst.get(11)),
                    () -> assertNull(orderedItemFirst.get(12), "Записалось значение " + orderedItemFirst.get(12)),
                    () -> assertNull(orderedItemFirst.get(13), "Записалось значение " + orderedItemFirst.get(13)),
                    () -> assertNull(orderedItemFirst.get(14), "Записалось значение " + orderedItemFirst.get(14)),
                    () -> assertNull(orderedItemFirst.get(15), "Записалось значение " + orderedItemFirst.get(15)),
                    () -> assertNull(orderedItemFirst.get(16), "Записалось значение " + orderedItemFirst.get(16)),
                    () -> assertNull(orderedItemFirst.get(17), "Записалось значение " + orderedItemFirst.get(17)),
                    () -> assertNull(orderedItemFirst.get(18), "Записалось значение " + orderedItemFirst.get(18)),
                    () -> assertNull(orderedItemFirst.get(19), "Записалось значение " + orderedItemFirst.get(19)),
                    () -> assertEquals(parse(request.getOrderedItems().get(0).getPrice()), orderedItemFirst.get(20), "Вместо " + parse(request.getOrderedItems().get(0).getPrice()) + " записано " + orderedItemFirst.get(20))
            );

            List<Object> orderedItemSecond = omsCore.getOrderedItemSecondItem(suborder_id);
            assertAll("Проверка таблицы oms.ordered_item для второго товара",
                    () -> assertEquals(request.getOrderedItems().get(1).getProductId(), orderedItemSecond.get(0), "Вместо " + request.getOrderedItems().get(1).getProductId() + " записано " + orderedItemSecond.get(0)),
                    () -> assertEquals(parse(request.getOrderedItems().get(1).getQuantity()), orderedItemSecond.get(1), "Вместо " + parse(request.getOrderedItems().get(1).getQuantity()) + " записано " + orderedItemSecond.get(1)),
                    () -> assertEquals(parse(request.getOrderedItems().get(1).getPrice()), orderedItemSecond.get(2), "Вместо " + parse(request.getOrderedItems().get(1).getPrice()) + " записано " + orderedItemSecond.get(2)),
                    () -> assertEquals(parse(request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())), orderedItemSecond.get(3), "Вместо " + parse(request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())) + " записано " + orderedItemSecond.get(3)),
                    () -> assertEquals(parse(request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())), orderedItemSecond.get(4), "Вместо " + parse(request.getOrderedItems().get(1).getPrice() * Double.parseDouble(request.getOrderedItems().get(1).getQuantity())) + " записано " + orderedItemSecond.get(4)),
                    () -> assertNotNull(orderedItemSecond.get(5), "Записалось пустое значение"),
                    () -> assertEquals(0, orderedItemSecond.get(6), "Записалось значение " + orderedItemSecond.get(6) + " вместо 0"),
                    () -> assertNotNull(orderedItemSecond.get(8), "Записалось пустое значение"),
                    () -> assertEquals(request.getOrderedItems().get(1).getManufacturerId(), String.valueOf(orderedItemSecond.get(9)), "Вместо " + request.getOrderedItems().get(1).getManufacturerId() + " записано " + orderedItemSecond.get(9)),
                    () -> assertNull(orderedItemSecond.get(10), "Записалось значение " + orderedItemSecond.get(10)),
                    () -> assertNull(orderedItemSecond.get(11), "Записалось значение " + orderedItemSecond.get(11)),
                    () -> assertNull(orderedItemSecond.get(12), "Записалось значение " + orderedItemSecond.get(12)),
                    () -> assertNull(orderedItemSecond.get(13), "Записалось значение " + orderedItemSecond.get(13)),
                    () -> assertNull(orderedItemSecond.get(14), "Записалось значение " + orderedItemSecond.get(14)),
                    () -> assertNull(orderedItemSecond.get(15), "Записалось значение " + orderedItemSecond.get(15)),
                    () -> assertNull(orderedItemSecond.get(16), "Записалось значение " + orderedItemSecond.get(16)),
                    () -> assertNull(orderedItemSecond.get(17), "Записалось значение " + orderedItemSecond.get(17)),
                    () -> assertNull(orderedItemSecond.get(18), "Записалось значение " + orderedItemSecond.get(18)),
                    () -> assertNull(orderedItemSecond.get(19), "Записалось значение " + orderedItemSecond.get(19)),
                    () -> assertEquals(parse(request.getOrderedItems().get(1).getPrice()), orderedItemSecond.get(20), "Вместо " + parse(request.getOrderedItems().get(1).getPrice()) + " записано " + orderedItemSecond.get(20))
            );
        });

        step("Проверка записи в таблице oms.delivery_detail", () -> {
            List<Object> deliveryDetailFields = omsCore.getDeliveryDetailFields(suborder_id);
            assertAll("Проверка таблицы oms.delivery_detail",
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFlat(), deliveryDetailFields.get(0), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFlat() + " записано " + deliveryDetailFields.get(0)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFloor(), deliveryDetailFields.get(1), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFloor() + " записано " + deliveryDetailFields.get(1)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getEntrance(), deliveryDetailFields.get(2), "Вместо " + request.getDeliveryDetails().getTargetAddress().getEntrance() + " записано " + deliveryDetailFields.get(2)),
                    () -> assertEquals(String.valueOf(request.getDeliveryDetails().getTargetAddress().getLatitude()), deliveryDetailFields.get(3), "Вместо " + request.getDeliveryDetails().getTargetAddress().getLatitude() + " записано " + deliveryDetailFields.get(3)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getDoorphone(), deliveryDetailFields.get(4), "Вместо " + request.getDeliveryDetails().getTargetAddress().getDoorphone() + " записано " + deliveryDetailFields.get(4)),
                    () -> assertEquals(String.valueOf(request.getDeliveryDetails().getTargetAddress().getLongitude()), deliveryDetailFields.get(5), "Вместо " + request.getDeliveryDetails().getTargetAddress().getLongitude() + " записано " + deliveryDetailFields.get(5)),
                    () -> assertEquals(Instant.ofEpochSecond(Long.parseLong(request.getDeliveryDetails().getSupplyDate())).toString(), deliveryDetailFields.get(6),
                            "Вместо " + request.getDeliveryDetails().getSupplyDate() + " записано " + deliveryDetailFields.get(6)),
                    () -> assertEquals(request.getDeliveryDetails().getTargetAddress().getFullAddress(), deliveryDetailFields.get(7), "Вместо " + request.getDeliveryDetails().getTargetAddress().getFullAddress() + " записано " + deliveryDetailFields.get(7)),
                    () -> assertEquals(request.getComment(), deliveryDetailFields.get(8), "Вместо " + request.getComment() + " записано " + deliveryDetailFields.get(8)),
                    () -> assertEquals("0", deliveryDetailFields.get(9), "Вместо 0 записано " + deliveryDetailFields.get(9)),
                    () -> assertEquals(Instant.ofEpochSecond(Long.parseLong(request.getDeliveryDetails().getSupplyUntilDate())).toString(), deliveryDetailFields.get(10),
                            "Вместо " + request.getDeliveryDetails().getSupplyUntilDate() + " записано " + deliveryDetailFields.get(10)),
                    () -> assertEquals("0", deliveryDetailFields.get(11), "Вместо 0 записано " + deliveryDetailFields.get(11)),
                    () -> assertNull(deliveryDetailFields.get(12), "Записалось значение " + deliveryDetailFields.get(12))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, WATER), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }
}
