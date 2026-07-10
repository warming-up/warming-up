package com.warmingup.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    private int durationMinutes;

    private int itemOrder;

    public void update(String name, int durationMinutes, int itemOrder) {
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.itemOrder = itemOrder;
    }
}
