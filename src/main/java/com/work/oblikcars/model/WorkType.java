package com.work.oblikcars.model;

public enum WorkType {
    SERVICE(1, "сервісне обслуговування"),
    WHEELS(2, "колеса"),
    OTHER(3, "інші роботи");

    private final int code;
    private final String displayName;

    WorkType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WorkType fromCode(int code) {
        for (WorkType type : WorkType.values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown WorkType code: " + code);
    }
}

