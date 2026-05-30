package com.pk.ev.vehicle.catalog.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        onChildCreate();
    }

    protected void onChildCreate() {
        //if child wants to do something we need to override
    }


    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        onChildUpdate();
    }

    protected void onChildUpdate() {
        //if child wants to do something we need to override
    }


}
