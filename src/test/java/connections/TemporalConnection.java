package connections;





import config.TestsConfigReader;

import io.temporal.client.WorkflowClient;

import io.temporal.client.WorkflowClientOptions;

import io.temporal.common.WorkflowExecutionHistory;

import io.temporal.serviceclient.WorkflowServiceStubs;

import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import test_data.enums.OrderType;



import java.util.Objects;





public class TemporalConnection {


    public static WorkflowExecutionHistory getGeneralWorkflowHistory(String order_id, OrderType orderType) {

        WorkflowClient client = createClient();
        WorkflowExecutionHistory history = null;


        switch (orderType) {

            case CERTIFICATE:
                history = client.fetchHistory("create-certificate-order-v2/" + order_id);
                break;
            case WATER:
                history = client.fetchHistory("create-water-order/" + order_id);
                break;
            case DARKKITCHEN:
                history = client.fetchHistory("create-internal-order-darkkitchen/" + order_id);
                break;
            case VKUSVILCHIK:
                history = client.fetchHistory("create-internal-order-with-delivery/" + order_id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported order type: " + orderType);
        }

        return history;
    }


    public static WorkflowExecutionHistory getAnticorruptionWorkflowHistory(String order_id) {
        return createClient().fetchHistory("anti-corruption-v2/" + order_id);

    }


    public static WorkflowClient createClient() {
        String target = Objects.requireNonNull(
                TestsConfigReader.testsConfig().temporalHost(),
                "Секрет 'temporal_host' не найден. Проверьте VAULT_TOKEN и путь sandbox-qa/temporal"
        );


        WorkflowServiceStubsOptions stubsOptions = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(target)
                .setEnableHttps(true)
                .build();
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(stubsOptions);

        return WorkflowClient.newInstance(service, WorkflowClientOptions.newBuilder()
                .setNamespace("default")
                .build());
    }

}

