package org.superbiz;

public class Stats {

    private Long timeElapsed;
    private Long messagesProcessed;
    private Long queueDepth;
    private Integer instanceCount;
    private Integer instanceLimit;
    private Double rate;

    public Stats() {

    }

    public Stats(final Long timeElapsed, final Long messagesProcessed, final Long queueDepth, final Integer instanceCount, final Integer instanceLimit, final Double rate) {
        this.timeElapsed = timeElapsed;
        this.messagesProcessed = messagesProcessed;
        this.queueDepth = queueDepth;
        this.instanceCount = instanceCount;
        this.instanceLimit = instanceLimit;
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

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Integer getInstanceLimit() {
        return instanceLimit;
    }

    public void setInstanceLimit(Integer instanceLimit) {
        this.instanceLimit = instanceLimit;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
