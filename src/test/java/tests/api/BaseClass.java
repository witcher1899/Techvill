package tests.api;

import config.TestsConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.temporal.common.WorkflowExecutionHistory;
import ru.vkusvill.generated.api.GatezillaApi;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderRequest;
import ru.vkusvill.generated.model.CustomerorderCreateInternalOrderResponse;
import ru.vkusvill.generated.model.HardcoreCreateWaterOrderV1Request;
import ru.vkusvill.generated.model.HardcoreCreateWaterOrderV1Response;
import ru.vkusvill.generated.model.MsgCreateCertificateV1Request;
import ru.vkusvill.generated.model.MsgCreateCertificateV1Response;
import test_data.db.OmsCore;
import test_data.enums.OrderType;
import utils.TemporalWorkflow;
import java.util.UUID;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static ru.vkusvill.generated.JacksonObjectMapper.jackson;


public class BaseClass {
    protected final OmsCore omsCore = new OmsCore();
    protected String suborder_id;

    protected GatezillaApi gatezillaApi() {
        return GatezillaApi.gatezilla(() -> new RequestSpecBuilder()
                .setBaseUri(TestsConfigReader.testsConfig().gatezillaHost())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(jackson())))
                .addHeader("Idempotency-Key", UUID.randomUUID().toString())
                .addHeader("Idempotency-TTL", "5s"));
    }

    protected MsgCreateCertificateV1Response getCertificateBody(MsgCreateCertificateV1Request request) {
        return gatezillaApi()
                .gatezillaCreateCertificateV2()
                .body(request)
                .execute(response -> response
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(MsgCreateCertificateV1Response.class));
    }

    protected HardcoreCreateWaterOrderV1Response getWaterBody(HardcoreCreateWaterOrderV1Request request) {
        return gatezillaApi()
                .gatezillaOrderWaterV3()
                .body(request)
                .execute(response -> response
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(HardcoreCreateWaterOrderV1Response.class));
    }

    protected CustomerorderCreateInternalOrderResponse getInternalOrderBody(CustomerorderCreateInternalOrderRequest request) {
        return gatezillaApi()
                .gatezillaCreateInternalOrder()
                .body(request)
                .execute(response -> response
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(CustomerorderCreateInternalOrderResponse.class));
    }

    protected WorkflowExecutionHistory getGeneralWorkflowHistory(String suborder_id, OrderType orderType) {
        return TemporalWorkflow.getGeneralWorkflowHistory(suborder_id, orderType);
    }

    protected WorkflowExecutionHistory getAnticorruptionWorkflowHistory(String suborder_id) {
        return TemporalWorkflow.getAnticorruptionWorkflowHistory(suborder_id);
    }
}
