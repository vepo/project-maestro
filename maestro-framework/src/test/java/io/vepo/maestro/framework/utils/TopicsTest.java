package io.vepo.maestro.framework.utils;

import static io.vepo.maestro.framework.utils.Topics.camelCaseToTopic;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TopicsTest {
    @Test
    void testCamelCaseToTopic() {
        assertThat(camelCaseToTopic("consumeData")).isEqualTo("consume-data");
        assertThat(camelCaseToTopic("consumeDataFromTopic")).isEqualTo("consume-data-from-topic");
    }
}
