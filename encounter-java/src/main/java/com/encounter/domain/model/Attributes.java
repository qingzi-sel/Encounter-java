package com.encounter.domain.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Value object holding the five player/NPC attributes.
 * Immutable — mutations return new instances via AttributeMath.
 */
public class Attributes {

    private final Map<AttributeType, Double> values;

    public Attributes() {
        this(20.0, 20.0, 20.0, 20.0, 20.0);
    }

    public Attributes(double stamina, double strength, double patience, double intelligence, double focus) {
        this.values = new EnumMap<>(AttributeType.class);
        this.values.put(AttributeType.STAMINA, stamina);
        this.values.put(AttributeType.STRENGTH, strength);
        this.values.put(AttributeType.PATIENCE, patience);
        this.values.put(AttributeType.INTELLIGENCE, intelligence);
        this.values.put(AttributeType.FOCUS, focus);
    }

    public Attributes(Map<AttributeType, Double> values) {
        this.values = new EnumMap<>(values);
    }

    public double get(AttributeType type) {
        return values.getOrDefault(type, 0.0);
    }

    public void set(AttributeType type, double value) {
        values.put(type, value);
    }

    public Map<AttributeType, Double> asMap() {
        return new EnumMap<>(values);
    }

    public Attributes copy() {
        return new Attributes(new EnumMap<>(values));
    }

    public double getHP() {
        return values.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public String toString() {
        return "Attributes{" + values + '}';
    }
}
