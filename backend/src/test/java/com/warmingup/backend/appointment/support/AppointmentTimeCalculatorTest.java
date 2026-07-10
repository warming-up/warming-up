package com.warmingup.backend.appointment.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTimeCalculatorTest {

    private final AppointmentTimeCalculator calculator = new AppointmentTimeCalculator();

    @Test
    void calculatesDeparturePreparationStartAndStepTimeline() {
        LocalDateTime arrivalTime = LocalDateTime.of(2026, 7, 10, 18, 0);

        AppointmentTimeCalculation result = calculator.calculate(
                arrivalTime,
                40,
                10,
                List.of(20, 30)
        );

        assertThat(result.departureTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10));
        assertThat(result.preparationStartTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 20));
        assertThat(result.stepTimelines()).containsExactly(
                new StepTimeline(0, LocalDateTime.of(2026, 7, 10, 16, 20), LocalDateTime.of(2026, 7, 10, 16, 40)),
                new StepTimeline(1, LocalDateTime.of(2026, 7, 10, 16, 40), LocalDateTime.of(2026, 7, 10, 17, 10))
        );
    }

    @Test
    void usesDepartureTimeAsPreparationStartWhenNoStepExists() {
        LocalDateTime arrivalTime = LocalDateTime.of(2026, 7, 10, 18, 0);

        AppointmentTimeCalculation result = calculator.calculate(arrivalTime, 40, 10, List.of());

        assertThat(result.departureTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10));
        assertThat(result.preparationStartTime()).isEqualTo(result.departureTime());
        assertThat(result.stepTimelines()).isEmpty();
    }

    @Test
    void rejectsNegativeDuration() {
        LocalDateTime arrivalTime = LocalDateTime.of(2026, 7, 10, 18, 0);

        assertThatThrownBy(() -> calculator.calculate(arrivalTime, 40, 10, List.of(20, -1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stepDurations[1]");
    }

    @Test
    void rejectsNegativeTravelAndBufferMinutes() {
        LocalDateTime arrivalTime = LocalDateTime.of(2026, 7, 10, 18, 0);

        assertThatThrownBy(() -> calculator.calculate(arrivalTime, -1, 10, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("travelMinutes");
        assertThatThrownBy(() -> calculator.calculate(arrivalTime, 40, -1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bufferMinutes");
    }
}
