package tests.grpc.genry;


import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;
import static test_data.enums.Services.GENRY;
import connections.ChannelConnection;
import genry.GenryGrpc;
import genry.GenryOuterClass;
import io.grpc.ManagedChannel;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test_data.db.SQL03;
import java.sql.SQLException;
import java.time.Instant;


class GenerateIDsV2 {

    private ManagedChannel channel;
    private GenryGrpc.GenryBlockingStub stub;
    private GenryOuterClass.GenerateIDsV2Response response;


    @BeforeEach
    public void setUp() {
        channel = new ChannelConnection().createGRPCChannel(GENRY);
        stub = GenryGrpc.newBlockingStub(channel);
    }


    @AfterEach
    public void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }


    @AllureId("52352")
    @DisplayName("Генерация номера заказа в SQL03")
    @Test
    void createNewIdOrder() {
        GenryOuterClass.GenerateIDsV2Request request = GenryOuterClass.GenerateIDsV2Request.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .build();

        step("Вызов метода GenerateIDsV2", () -> {
            response = stub.generateIDsV2(request);

            boolean is_id_order_exist = new SQL03().isOrderExist(response.getIds(0).getId());

            assertNotNull(response);
            assertTrue(is_id_order_exist, "Номер заказа не создан на 03 сервере");
        });
    }
}
