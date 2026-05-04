package com.encounter.domain;

import com.encounter.domain.model.AttributeType;
import com.encounter.util.AttributeMath;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeMathTest {

    @Test
    void snapVal_roundsToOneDecimal() {
        assertEquals(5.5, AttributeMath.snapVal(5.52));
        assertEquals(5.6, AttributeMath.snapVal(5.55));
        assertEquals(0.0, AttributeMath.snapVal(0.5));
        assertEquals(1.0, AttributeMath.snapVal(1.0));
    }

    @Test
    void calcHP_sumsAllAttributes() {
        Map<AttributeType, Double> attrs = new EnumMap<>(AttributeType.class);
        attrs.put(AttributeType.STAMINA, 20.0);
        attrs.put(AttributeType.STRENGTH, 15.0);
        attrs.put(AttributeType.PATIENCE, 10.0);
        attrs.put(AttributeType.INTELLIGENCE, 25.0);
        attrs.put(AttributeType.FOCUS, 30.0);

        assertEquals(100.0, AttributeMath.calcHP(attrs));
    }

    @Test
    void adjustDistributed_shiftsPointsCorrectly() {
        Map<AttributeType, Double> current = new EnumMap<>(AttributeType.class);
        current.put(AttributeType.STAMINA, 20.0);
        current.put(AttributeType.STRENGTH, 20.0);
        current.put(AttributeType.PATIENCE, 20.0);
        current.put(AttributeType.INTELLIGENCE, 20.0);
        current.put(AttributeType.FOCUS, 20.0);

        Map<AttributeType, Double> result = AttributeMath.adjustDistributed(current, AttributeType.STRENGTH, 10.0);

        // Total HP should stay the same (100)
        assertEquals(100.0,
                result.values().stream().mapToDouble(Double::doubleValue).sum(),
                0.1);

        // Strength should have increased
        assertTrue(result.get(AttributeType.STRENGTH) > 20.0);

        // Other attributes should have decreased
        double othersSum = result.get(AttributeType.STAMINA)
                + result.get(AttributeType.PATIENCE)
                + result.get(AttributeType.INTELLIGENCE)
                + result.get(AttributeType.FOCUS);
        assertTrue(othersSum < 80.0);
    }

    @Test
    void adjustDistributed_cannotExceedLimits() {
        Map<AttributeType, Double> current = new EnumMap<>(AttributeType.class);
        current.put(AttributeType.STAMINA, 100.0);
        current.put(AttributeType.STRENGTH, 0.0);
        current.put(AttributeType.PATIENCE, 0.0);
        current.put(AttributeType.INTELLIGENCE, 0.0);
        current.put(AttributeType.FOCUS, 0.0);

        // Try to increase strength by more than available
        Map<AttributeType, Double> result = AttributeMath.adjustDistributed(current, AttributeType.STRENGTH, 200.0);

        // Should be capped at 100
        assertTrue(result.get(AttributeType.STRENGTH) <= 100.0 + 0.1);
    }
}
