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
public class TaskComment {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private Instant createdAt;
}
