package com.zippt.l3l4sa.server.service;

import java.time.LocalDateTime;

public class OperationLog {
    private final String operationName;
    private final String actorId;
    private final String targetId;
    private final String result;
    private final long elapsedNanos;
    private final LocalDateTime recordedAt;

    public OperationLog(String operationName, String actorId, String targetId, String result, long elapsedNanos) {
        this.operationName = operationName;
        this.actorId = actorId;
        this.targetId = targetId;
        this.result = result;
        this.elapsedNanos = elapsedNanos;
        this.recordedAt = LocalDateTime.now();
    }

    public String getOperationName() { return operationName; }
    public String getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getResult() { return result; }
    public long getElapsedNanos() { return elapsedNanos; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
