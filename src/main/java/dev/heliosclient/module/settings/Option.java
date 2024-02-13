package dev.heliosclient.module.settings;

public class Option<T> {
    private String name;
    private T value;
    private boolean enabled;

    public Option(String name, T value, boolean enabled) {
        this.name = name;
        this.value = value;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
