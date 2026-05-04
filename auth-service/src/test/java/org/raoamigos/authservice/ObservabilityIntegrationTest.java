package org.raoamigos.authservice;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ObservabilityIntegrationTest {

    @Autowired
    private ObservationRegistry observationRegistry;

    @Test
    void observationRegistry_IsPresent() {
        assertThat(observationRegistry).isNotNull();
    }

    @Test
    void canCreateObservation() {
        Observation observation = Observation.start("test.observation", observationRegistry);
        try (Observation.Scope scope = observation.openScope()) {
            assertThat(scope).isNotNull();
        } finally {
            observation.stop();
        }
    }
}
