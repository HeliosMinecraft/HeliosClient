package dev.heliosclient.event;

import dev.heliosclient.event.Cancelable;

// This abstract class is the base class for all events
public abstract class Event {
    // This field indicates whether the event is canceled or not
    private boolean canceled = false;

    // This method returns true if the event is cancelable, false otherwise
    public boolean isCancelable() {
        return this.getClass().isAnnotationPresent(Cancelable.class);
    }

    // This method returns true if the event is canceled, false otherwise
    public boolean isCanceled() {
        return canceled;
    }

    // This method sets the canceled state of the event
    public void setCanceled(boolean canceled) {
        if (!isCancelable()) {
            throw new IllegalStateException("This event cannot be canceled");
        }
        this.canceled = canceled;
    }
}
