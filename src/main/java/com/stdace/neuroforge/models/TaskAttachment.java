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
public class TaskAttachment {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Instant createdAt;
}
