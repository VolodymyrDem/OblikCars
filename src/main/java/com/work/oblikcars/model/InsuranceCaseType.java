package com.work.oblikcars.model;

public enum InsuranceCaseType {
    TOTALCRUSH(1, "тотал"),
    RETURN(2, "відшкодування");

    private final int code;
    private final String displayName;

    InsuranceCaseType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static InsuranceCaseType fromCode(int code) {
        for (InsuranceCaseType type : InsuranceCaseType.values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown InsuranceCaseType code: " + code);
    }
}
