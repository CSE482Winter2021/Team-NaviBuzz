package com.navisens.demo.android_app_helloworld.database_obj;

import java.util.UUID;

public class Landmark {
    private String name;
    private UUID uuid;

    public Landmark(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
