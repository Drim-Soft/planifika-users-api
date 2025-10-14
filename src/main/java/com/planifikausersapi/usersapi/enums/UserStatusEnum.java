package com.planifikausersapi.usersapi.enums;

public enum UserStatusEnum {
    ACTIVE(1),
    DELETED(2);

    private final Integer id;

    UserStatusEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
