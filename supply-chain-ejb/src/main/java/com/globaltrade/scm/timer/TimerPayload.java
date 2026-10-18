package com.globaltrade.scm.timer;

import java.io.Serializable;

public class TimerPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TimerType {
        CUSTOMS_DEADLINE,
        ROUTE_OPTIMIZATION_SWEEP
    }

    private final TimerType type;
    private final Long entityId;

    public TimerPayload(TimerType type, Long entityId) {
        this.type = type;
        this.entityId = entityId;
    }

    public TimerType getType() {
        return type;
    }

    public Long getEntityId() {
        return entityId;
    }

    @Override
    public String toString() {
        return "TimerPayload{type=" + type + ", entityId=" + entityId + '}';
    }
}

