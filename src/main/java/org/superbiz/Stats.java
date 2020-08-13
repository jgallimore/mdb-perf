package org.superbiz;

public class Stats {

    private Long timeElapsed;
    private Long messagesProcessed;
    private Long queueDepth;
    private Double rate;

    public Stats() {

    }

    public Stats(final Long timeElapsed, final Long messagesProcessed, final Long queueDepth, final Double rate) {
        this.timeElapsed = timeElapsed;
        this.messagesProcessed = messagesProcessed;
        this.queueDepth = queueDepth;
        this.rate = rate;
    }

    public Long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(Long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public Long getMessagesProcessed() {
        return messagesProcessed;
    }

    public void setMessagesProcessed(Long messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }

    public Long getQueueDepth() {
        return queueDepth;
    }

    public void setQueueDepth(Long queueDepth) {
        this.queueDepth = queueDepth;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
