package com.encounter.domain.model;

/**
 * Result of an observatory divination.
 */
public class DivinationResult {

    private TarotCard card;
    private double timer;

    public DivinationResult() {}

    public DivinationResult(TarotCard card) {
        this.card = card;
        this.timer = 0.0;
    }

    public TarotCard getCard() { return card; }
    public void setCard(TarotCard card) { this.card = card; }

    public double getTimer() { return timer; }
    public void setTimer(double timer) { this.timer = timer; }
}
