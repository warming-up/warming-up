package com.warmingup.backend.routine;

import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByUser(User user);
}
