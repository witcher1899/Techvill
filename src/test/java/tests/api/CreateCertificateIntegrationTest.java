package tests.api;


import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vkusvill.generated.model.CreateCertificateV1RequestCountedOfferPrice;
import ru.vkusvill.generated.model.MsgCreateCertificateV1Request;
import ru.vkusvill.generated.model.MsgCreateCertificateV1RequestCountedOffer;
import ru.vkusvill.generated.model.MsgCreateCertificateV1Response;
import ru.vkusvill.generated.model.MsgSources;
import java.util.List;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;
import static test_data.enums.OrderType.CERTIFICATE;
import static utils.TypeParsing.parse;


@Epic("Сертификаты")
@DisplayName("Интеграционные тесты для заказов типа «Сертификаты»")
@Feature("Создание заказа типа «Сертификаты» через gatezilla")
@Severity(SeverityLevel.CRITICAL)
public class CreateCertificateIntegrationTest extends BaseClass {
    private MsgCreateCertificateV1Response response;


    @AllureId("84840")
    @DisplayName("Создание заказа типа 'Сертификаты' с одним товаром")
    @Test
    public void createOneCert() {
        MsgCreateCertificateV1Request request = new MsgCreateCertificateV1Request()
                .loyaltyCardNumber("TEST0001")
                .paymentCardId("106")
                .bonuses(34.34)
                .email("test@example.com")
                .creationSource(MsgSources.WEB)
                .countedOffer(List.of(
                        new MsgCreateCertificateV1RequestCountedOffer()
                                .productId("23169")
                                .quantity("1")
                                .price(new CreateCertificateV1RequestCountedOfferPrice()
                                        .total(1000.0)
                                        .basePrice(1000.0))
                ));

        step("Вызов ручки /v2/orders/certificate", () -> {
            response = getCertificateBody(request);
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
                    () -> assertEquals("certificate", suborderFields.get(2), "Вместо 'certificate' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getLoyaltyCardNumber(), suborderFields.get(3), "Вместо " + request.getLoyaltyCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(2519, suborderFields.get(4), "Вместо 2519 записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals("", suborderFields.get(6), "Записалось значение " + suborderFields.get(10)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertNull(suborderFields.get(8), "Записалось значение " + suborderFields.get(8)),
                    () -> assertNull(suborderFields.get(9), "Записалось значение " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(CERTIFICATE.getValue(), suborderFields.get(11), "Вместо " + CERTIFICATE.getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), suborderFields.get(12), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + suborderFields.get(12)),
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
                    () -> assertEquals(request.getCountedOffer().get(0).getProductId(), orderedItem.get(0), "Вместо " + request.getCountedOffer().get(0).getProductId() + " записано " + orderedItem.get(0)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getQuantity()), orderedItem.get(1), "Вместо " + parse(request.getCountedOffer().get(0).getQuantity()) + " записано " + orderedItem.get(1)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItem.get(2), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItem.get(2)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItem.get(3), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItem.get(3)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItem.get(4), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItem.get(4)),
                    () -> assertNotNull(orderedItem.get(5), "Записалось пустое значение "),
                    () -> assertEquals(0, orderedItem.get(6), "Вместо 0 записалось значение " + orderedItem.get(6)),
                    () -> assertFalse((Boolean) orderedItem.get(7), "Записалось значение " + orderedItem.get(7)),
                    () -> assertNotNull(orderedItem.get(8), "Записалось пустое значение "),
                    () -> assertNull(orderedItem.get(9), "Записалось значение " + orderedItem.get(9)),
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
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItem.get(20), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItem.get(20))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, CERTIFICATE), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }


    @AllureId("84911")
    @DisplayName("Создание заказа типа 'Сертификаты' с двумя товарами")
    @Test
    public void createMultipleCert() {
        MsgCreateCertificateV1Request request = new MsgCreateCertificateV1Request()
                .loyaltyCardNumber("TEST0001")
                .paymentCardId("106")
                .bonuses(34.34)
                .email("test@example.com")
                .creationSource(MsgSources.WEB)
                .countedOffer(List.of(
                        new MsgCreateCertificateV1RequestCountedOffer()
                                .productId("23169")
                                .quantity("3")
                                .price(new CreateCertificateV1RequestCountedOfferPrice()
                                        .total(1000.0)
                                        .basePrice(1000.0)),
                        new MsgCreateCertificateV1RequestCountedOffer()
                                .productId("44617")
                                .quantity("2")
                                .price(new CreateCertificateV1RequestCountedOfferPrice()
                                        .total(1500.0)
                                        .basePrice(1500.0))
                ));

        step("Вызов ручки /v2/orders/certificate", () -> {
            response = getCertificateBody(request);
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
                    () -> assertEquals("certificate", suborderFields.get(2), "Вместо 'certificate' записано " + suborderFields.get(2)),
                    () -> assertEquals(request.getLoyaltyCardNumber(), suborderFields.get(3), "Вместо " + request.getLoyaltyCardNumber() + " записано " + suborderFields.get(3)),
                    () -> assertEquals(2519, suborderFields.get(4), "Вместо 2519 записано " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals("", suborderFields.get(6), "Записалось значение " + suborderFields.get(10)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение " + suborderFields.get(7)),
                    () -> assertNull(suborderFields.get(8), "Записалось значение " + suborderFields.get(8)),
                    () -> assertNull(suborderFields.get(9), "Записалось значение " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(CERTIFICATE.getValue(), suborderFields.get(11), "Вместо " + CERTIFICATE.getValue() + " записано " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal() + request.getCountedOffer().get(1).getPrice().getTotal()), suborderFields.get(12), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal() + request.getCountedOffer().get(1).getPrice().getTotal()) + " записано " + suborderFields.get(12)),
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
            assertAll("Проверка таблицы oms.ordered_item",
                    () -> assertEquals(request.getCountedOffer().get(0).getProductId(), orderedItemFirst.get(0), "Вместо " + request.getCountedOffer().get(0).getProductId() + " записано " + orderedItemFirst.get(0)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getQuantity()), orderedItemFirst.get(1), "Вместо " + parse(request.getCountedOffer().get(0).getQuantity()) + " записано " + orderedItemFirst.get(1)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItemFirst.get(2), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItemFirst.get(2)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItemFirst.get(3), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItemFirst.get(3)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItemFirst.get(4), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItemFirst.get(4)),
                    () -> assertNotNull(orderedItemFirst.get(5), "Записалось пустое значение "),
                    () -> assertEquals(0, orderedItemFirst.get(6), "Вместо 0 записалось значение " + orderedItemFirst.get(6)),
                    () -> assertFalse((Boolean) orderedItemFirst.get(7), "Записалось значение " + orderedItemFirst.get(7)),
                    () -> assertNotNull(orderedItemFirst.get(8), "Записалось пустое значение "),
                    () -> assertNull(orderedItemFirst.get(9), "Записалось значение " + orderedItemFirst.get(9)),
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
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItemFirst.get(20), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItemFirst.get(20))
            );

            List<Object> orderedItemSecond = omsCore.getOrderedItemSecondItem(suborder_id);
            assertAll("Проверка таблицы oms.ordered_item для второго товара",
                    () -> assertEquals(request.getCountedOffer().get(1).getProductId(), orderedItemSecond.get(0), "Вместо " + request.getCountedOffer().get(1).getProductId() + " записано " + orderedItemSecond.get(0)),
                    () -> assertEquals(parse(request.getCountedOffer().get(1).getQuantity()), orderedItemSecond.get(1), "Вместо " + parse(request.getCountedOffer().get(1).getQuantity()) + " записано " + orderedItemSecond.get(1)),
                    () -> assertEquals(parse(request.getCountedOffer().get(1).getPrice().getBasePrice()), orderedItemSecond.get(2), "Вместо " + parse(request.getCountedOffer().get(1).getPrice().getBasePrice()) + " записано " + orderedItemSecond.get(2)),
                    () -> assertEquals(parse(request.getCountedOffer().get(1).getPrice().getTotal()), orderedItemSecond.get(3), "Вместо " + parse(request.getCountedOffer().get(1).getPrice().getTotal()) + " записано " + orderedItemSecond.get(3)),
                    () -> assertEquals(parse(request.getCountedOffer().get(1).getPrice().getTotal()), orderedItemSecond.get(4), "Вместо " + parse(request.getCountedOffer().get(1).getPrice().getTotal()) + " записано " + orderedItemSecond.get(4)),
                    () -> assertNotNull(orderedItemSecond.get(5), "Записалось пустое значение "),
                    () -> assertEquals(0, orderedItemSecond.get(6), "Вместо 0 записалось значение " + orderedItemSecond.get(6)),
                    () -> assertFalse((Boolean) orderedItemSecond.get(7), "Записалось значение " + orderedItemSecond.get(7)),
                    () -> assertNotNull(orderedItemSecond.get(8), "Записалось пустое значение "),
                    () -> assertNull(orderedItemSecond.get(9), "Записалось значение " + orderedItemSecond.get(9)),
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
                    () -> assertEquals(parse(request.getCountedOffer().get(1).getPrice().getBasePrice()), orderedItemSecond.get(20), "Вместо " + parse(request.getCountedOffer().get(1).getPrice().getBasePrice()) + " записано " + orderedItemSecond.get(20))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, CERTIFICATE), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }


    @AllureId("84912")
    @DisplayName("Создание заказа типа 'Сертификаты' без необязательного параметра")
    @Test
    public void createCertWithoutUnnecessaryFields() {
        MsgCreateCertificateV1Request request = new MsgCreateCertificateV1Request()
                .loyaltyCardNumber("TEST0001")
                .paymentCardId("106")
                .email("test@example.com")
                .creationSource(MsgSources.WEB)
                .countedOffer(List.of(
                        new MsgCreateCertificateV1RequestCountedOffer()
                                .productId("23169")
                                .quantity("1")
                                .price(new CreateCertificateV1RequestCountedOfferPrice()
                                        .total(1000.0)
                                        .basePrice(1000.0))
                ));

        step("Вызов ручки /v2/orders/certificate", () -> {
            response = getCertificateBody(request);
        });

        step("Проверка HTTP code и наличие тела ответа", () -> {
            assertNotNull(response, "В ответе должно быть тело");
            assertNotNull(response.getSuborderId(), "В ответе должен быть suborder_id");
            suborder_id = response.getSuborderId();
        });

        step("Проверка записи в таблице oms.suborder_state", () -> {
            assertEquals("ORDER_POSTED", omsCore.getSuborderStateFields(suborder_id),
                    "Вместо ORDER_POSTED статус заказа " + omsCore.getSuborderStateFields(suborder_id));
        });

        step("Проверка записи в таблице oms.suborder", () -> {
            List<Object> suborderFields = omsCore.getSuborderFields(suborder_id);
            assertAll("Проверка таблицы oms.suborder",
                    () -> assertEquals(suborder_id, suborderFields.get(0), "Вместо " + suborder_id + " записалось " + suborderFields.get(0)),
                    () -> assertEquals(suborder_id, suborderFields.get(1), "Вместо " + suborder_id + " записалось " + suborderFields.get(1)),
                    () -> assertEquals("certificate", suborderFields.get(2), "Вместо 'certificate' записалось " + suborderFields.get(2)),
                    () -> assertEquals(request.getLoyaltyCardNumber(), suborderFields.get(3), "Вместо " + request.getLoyaltyCardNumber() + " записалось " + suborderFields.get(3)),
                    () -> assertEquals(2519, suborderFields.get(4), "Вместо 2519 записалось " + suborderFields.get(4)),
                    () -> assertNotNull(suborderFields.get(5), "Поле не заполнено"),
                    () -> assertEquals("", suborderFields.get(6), "Записалось значение " + suborderFields.get(10)),
                    () -> assertNull(suborderFields.get(7), "Записалось значение вместо " + suborderFields.get(7)),
                    () -> assertNull(suborderFields.get(8), "Записалось значение " + suborderFields.get(8)),
                    () -> assertNull(suborderFields.get(9), "Записалось значение " + suborderFields.get(9)),
                    () -> assertNull(suborderFields.get(10), "Записалось значение " + suborderFields.get(10)),
                    () -> assertEquals(CERTIFICATE.getValue(), suborderFields.get(11), "Вместо " + CERTIFICATE.getValue() + " записалось " + suborderFields.get(11)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), suborderFields.get(12), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записалось " + suborderFields.get(12)),
                    () -> assertEquals(suborder_id + "-1", suborderFields.get(13), "Вместо " + suborder_id + "-1" + " записалось " + suborderFields.get(13)),
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
                    () -> assertEquals(request.getCountedOffer().get(0).getProductId(), orderedItem.get(0), "Вместо " + request.getCountedOffer().get(0).getProductId() + " записано " + orderedItem.get(0)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getQuantity()), orderedItem.get(1), "Вместо " + parse(request.getCountedOffer().get(0).getQuantity()) + " записано " + orderedItem.get(1)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItem.get(2), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItem.get(2)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItem.get(3), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItem.get(3)),
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getTotal()), orderedItem.get(4), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getTotal()) + " записано " + orderedItem.get(4)),
                    () -> assertNotNull(orderedItem.get(5), "Записалось пустое значение "),
                    () -> assertEquals(0, orderedItem.get(6), "Вместо 0 записалось значение " + orderedItem.get(6)),
                    () -> assertFalse((Boolean) orderedItem.get(7), "Записалось значение " + orderedItem.get(7)),
                    () -> assertNotNull(orderedItem.get(8), "Записалось пустое значение "),
                    () -> assertNull(orderedItem.get(9), "Записалось значение " + orderedItem.get(9)),
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
                    () -> assertEquals(parse(request.getCountedOffer().get(0).getPrice().getBasePrice()), orderedItem.get(20), "Вместо " + parse(request.getCountedOffer().get(0).getPrice().getBasePrice()) + " записано " + orderedItem.get(20))
            );
        });

        step("Проверка создания Temporal workFlows", () -> {
            assertNotNull(getGeneralWorkflowHistory(suborder_id, CERTIFICATE), "Workflow создания заказа отсутствует");
            assertNotNull(getAnticorruptionWorkflowHistory(suborder_id), "Workflow anticorruption отсутствует");
        });
    }
}
