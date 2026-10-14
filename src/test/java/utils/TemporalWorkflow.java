package utils;

import io.temporal.client.WorkflowClient;
import io.temporal.common.WorkflowExecutionHistory;
import test_data.enums.OrderType;
import static connections.TemporalConnection.createClient;


public class TemporalWorkflow {

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
}
