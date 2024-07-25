package dev.heliosclient.event;

// This abstract class is the base class for all events
public abstract class Event {
    // This field indicates whether the event is canceled or not
    private boolean canceled = false;

    /**
     * @return True if the event is cancelable, false otherwise
     */
    public boolean isCancelable() {
        return this.getClass().isAnnotationPresent(Cancelable.class);
    }

    /**
     * @return Returns true if the event is canceled, false otherwise
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * This method sets the canceled state of the event
     *
     * @param canceled Target state.
     */
    public void setCanceled(boolean canceled) {
        if (!isCancelable()) {
            throw new IllegalStateException("This event cannot be canceled");
        }
        this.canceled = canceled;
    }
    /**
     * This method cancels the event
     */
    public void cancel() {
        this.setCanceled(true);
    }
}
