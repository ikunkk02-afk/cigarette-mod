package com.example.examplemod;

import net.minecraft.util.StringRepresentable;

public enum DryingRackState implements StringRepresentable {
    EMPTY("empty"),
    DRYING("drying"),
    DRIED("dried");

    private final String serializedName;

    DryingRackState(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
