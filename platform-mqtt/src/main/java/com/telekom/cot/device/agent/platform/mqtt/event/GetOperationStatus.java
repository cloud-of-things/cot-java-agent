package com.telekom.cot.device.agent.platform.mqtt.event;

public class GetOperationStatus {

    private String id;
    private String status;

    public GetOperationStatus(String id, String status) {
        super();
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "GetOperationStatus [id=" + id + ", status=" + status + "]";
    }
}
