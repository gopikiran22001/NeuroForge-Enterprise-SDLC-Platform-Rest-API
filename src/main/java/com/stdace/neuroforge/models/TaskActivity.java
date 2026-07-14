package com.stdace.neuroforge.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskActivity {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String actorName;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Instant createdAt;
}
