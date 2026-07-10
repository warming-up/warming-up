package com.warmingup.backend.appointment.repository;

import com.warmingup.backend.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.items
            where a.id = :appointmentId and a.user.id = :userId
            """)
    Optional<Appointment> findByIdAndUserIdWithItems(
            @Param("appointmentId") Long appointmentId,
            @Param("userId") Long userId
    );
}
