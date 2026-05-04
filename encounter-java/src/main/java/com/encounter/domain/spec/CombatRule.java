package com.encounter.domain.spec;

import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.Attributes;

import java.util.List;
import java.util.Map;

/**
 * Extension point: defines how combat is resolved.
 * Implement to create custom combat behavior.
 */
public interface CombatRule {

    /**
     * Determine the winner and how much is stolen.
     *
     * @param activeAttrs the attributes compared in this room
     * @param playerEffectiveAttrs player's effective attribute values (post-debuff)
     * @param npcAttrs NPC's current attribute values
     * @param npcHP NPC's current total HP
     * @return the outcome of the comparison
     */
    CombatOutcome resolve(
            List<AttributeType> activeAttrs,
            Attributes playerEffectiveAttrs,
            Attributes npcAttrs,
            double npcHP);

    record CombatOutcome(
            Winner winner,
            double stealTotal,
            Map<AttributeType, Double> stolenValues,
            boolean isExecution) {}

    enum Winner { PLAYER, NPC, DRAW }
}
