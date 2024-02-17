package dev.heliosclient.system;

public record Friend(String playerName) {
    // Override the equals method to compare friends by their player names
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Friend other) {
            return this.playerName.equals(other.playerName);
        }
        return false;
    }

    @Override
    public String playerName() {
        return playerName;
    }

    @Override
    public int hashCode() {
        return this.playerName.hashCode();
    }

    @Override
    public String toString() {
        return playerName;
    }
}