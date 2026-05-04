package com.encounter.util;

import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.Attributes;

import java.util.EnumMap;
import java.util.Map;

/**
 * Core attribute math: snap, HP calculation, and distributed adjustment.
 * All methods are pure functions (stateless).
 */
public final class AttributeMath {

    private AttributeMath() {}

    /**
     * Snap a value: if < 1, round to 0; otherwise round to 1 decimal.
     */
    public static double snapVal(double val) {
        if (val < 1.0) return 0.0;
        return Math.round(val * 10.0) / 10.0;
    }

    /**
     * Snap all values in the attributes map.
     */
    public static Map<AttributeType, Double> snapAll(Map<AttributeType, Double> attrs) {
        Map<AttributeType, Double> result = new EnumMap<>(AttributeType.class);
        attrs.forEach((k, v) -> result.put(k, snapVal(v)));
        return result;
    }

    /**
     * Apply snapAll to an Attributes object in-place.
     */
    public static void snapAllInPlace(Attributes attrs) {
        for (AttributeType type : AttributeType.values()) {
            attrs.set(type, snapVal(attrs.get(type)));
        }
    }

    /**
     * Calculate total HP = sum of all attributes.
     */
    public static double calcHP(Map<AttributeType, Double> attrs) {
        return attrs.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Calculate total HP from an Attributes object.
     */
    public static double calcHP(Attributes attrs) {
        double sum = 0;
        for (AttributeType type : AttributeType.values()) {
            sum += attrs.get(type);
        }
        return Math.round(sum * 10.0) / 10.0;
    }

    /**
     * Adjust one attribute by a delta, redistributing from/to other attributes
     * to keep total HP constant. Returns a new map with the adjusted values.
     *
     * @param current  Current attribute values
     * @param targetKey The attribute to adjust
     * @param targetDelta The amount to add (positive) or subtract (negative)
     * @return A new map with adjusted values
     */
    public static Map<AttributeType, Double> adjustDistributed(
            Map<AttributeType, Double> current,
            AttributeType targetKey,
            double targetDelta) {

        // Convert to integer tenths for precise integer arithmetic
        Map<AttributeType, Integer> currInt = new EnumMap<>(AttributeType.class);
        for (AttributeType t : AttributeType.values()) {
            currInt.put(t, (int) Math.round(current.getOrDefault(t, 0.0) * 10.0));
        }

        int deltaInt = (int) Math.round(targetDelta * 10.0);

        int maxSubtract = currInt.get(targetKey);
        int maxAdd = 0;
        for (AttributeType t : AttributeType.values()) {
            if (t != targetKey) maxAdd += currInt.get(t);
        }

        if (deltaInt < -maxSubtract) deltaInt = -maxSubtract;
        if (deltaInt > maxAdd) deltaInt = maxAdd;

        if (deltaInt == 0) {
            return new EnumMap<>(current);
        }

        currInt.put(targetKey, currInt.get(targetKey) + deltaInt);

        int balance = -deltaInt;
        java.util.List<AttributeType> activeOthers = new java.util.ArrayList<>();
        for (AttributeType t : AttributeType.values()) {
            if (t != targetKey) activeOthers.add(t);
        }

        while (balance != 0 && !activeOthers.isEmpty()) {
            int split = balance / activeOthers.size();
            if (split == 0) {
                split = balance > 0 ? 1 : -1;
            }

            java.util.List<AttributeType> nextActive = new java.util.ArrayList<>();
            for (AttributeType k : activeOthers) {
                if (balance == 0) break;

                int applied = split;
                if (currInt.get(k) + applied < 0) {
                    applied = -currInt.get(k);
                }

                if ((balance > 0 && applied > balance) || (balance < 0 && applied < balance)) {
                    applied = balance;
                }

                currInt.put(k, currInt.get(k) + applied);
                balance -= applied;

                if (currInt.get(k) > 0) {
                    nextActive.add(k);
                }
            }
            activeOthers = nextActive;
        }

        Map<AttributeType, Double> result = new EnumMap<>(AttributeType.class);
        for (AttributeType t : AttributeType.values()) {
            result.put(t, currInt.getOrDefault(t, 0) / 10.0);
        }
        return result;
    }
}
