package dev.heliosclient.util;

public class TickTimer {
    private int ticks = 0;
    private boolean hasTimerStarted = false;

    public TickTimer(int defaultTicks) {
        ticks = defaultTicks;
    }

    public TickTimer() {
    }

    public void startTicking() {
        if (hasTimerStarted) return;
        ticks = 0;
        hasTimerStarted = true;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    /**
     * @return elapsed ticks since the last reset  / start
     */
    public double getElapsedTicks() {
        return ticks;
    }

    // Resets the timer
    public void resetTimer() {
        ticks = 0;
        hasTimerStarted = false;
    }

    public void increment(){
        if(!hasTimerStarted){
            throw new IllegalStateException("Timer has not started yet");
        }
        ticks++;
    }

    // Checks if ticks have passed since the last reset
    public boolean every(int ticks) {
        if (getElapsedTicks() >= ticks) {
            resetTimer();
            startTicking();
        }
        return getElapsedTicks() >= ticks;
    }


    public boolean incrementAndEvery(int ticks) {
        increment();
        if (getElapsedTicks() >= ticks) {
            resetTimer();
            startTicking();
        }
        return getElapsedTicks() >= ticks;
    }

    public boolean incrementAndEvery(int ticks, Runnable task) {
        increment();
        if (getElapsedTicks() >= ticks) {
            task.run();
            resetTimer();
            startTicking();
        }
        return getElapsedTicks() >= ticks;
    }

    public boolean every(int ticks, Runnable task) {
        if (getElapsedTicks() >= ticks) {
            task.run();
            resetTimer();
            startTicking();
        }
        return getElapsedTicks() >= ticks;
    }
}
