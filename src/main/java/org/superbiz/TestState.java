package org.superbiz;

public class TestState {

    private final long startTime;
    private final long completionTime;
    private final long totalMessages;
    private final long messagesProcessed;

    private TestState(final long startTime, final long completionTime, final long totalMessages, final long messagesProcessed) {
        this.startTime = startTime;
        this.completionTime = completionTime;
        this.totalMessages = totalMessages;
        this.messagesProcessed = messagesProcessed;
    }

    public static TestState start(final long totalMessages) {
        return new TestState(System.nanoTime(), 0, totalMessages, 0);
    }

    public TestState incrementMessagesProcessed() {
        return new TestState(this.startTime, this.completionTime, this.totalMessages, this.messagesProcessed + 1);
    }

    public TestState stop() {
        return new TestState(this.startTime, System.nanoTime(), this.totalMessages, this.messagesProcessed);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }

    public long remaining() {
        return totalMessages - messagesProcessed;
    }

    public boolean complete() {
        return remaining() == 0;
    }

    public long getTimeElapsed() {
        return (this.getCompletionTime() != 0) ? this.getCompletionTime() - this.getStartTime() : System.nanoTime() - this.getStartTime();
    }
}
