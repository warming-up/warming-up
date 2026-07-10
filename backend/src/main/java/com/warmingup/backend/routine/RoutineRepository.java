package com.warmingup.backend.routine;

import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByUser(User user);

    @Query("""
            select distinct r
            from Routine r
            left join fetch r.items
            where r.id = :routineId and r.user.id = :userId
            """)
    Optional<Routine> findByIdAndUserIdWithItems(@Param("routineId") Long routineId, @Param("userId") Long userId);
}
