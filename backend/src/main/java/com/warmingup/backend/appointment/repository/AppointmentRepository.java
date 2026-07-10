package com.warmingup.backend.appointment.repository;

import com.warmingup.backend.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
