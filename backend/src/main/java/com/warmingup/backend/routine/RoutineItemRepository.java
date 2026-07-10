package com.warmingup.backend.routine;

import com.warmingup.backend.domain.RoutineItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineItemRepository extends JpaRepository<RoutineItem, Long> {
}
