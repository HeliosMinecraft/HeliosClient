package dev.heliosclient.system;

// Define a class for a friend
public class Friend {
    // Declare a field for the player name
    private final String playerName;

    // Create a constructor that takes a player name as an argument
    public Friend(String playerName) {
        this.playerName = playerName;
    }

    // Create a getter method for the player name
    public String getPlayerName() {
        return this.playerName;
    }

    // Override the equals method to compare friends by their player names
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Friend other) {
            return this.playerName.equals(other.playerName);
        }
        return false;
    }

    // Override the hashCode method to use the player name as the hash code
    @Override
    public int hashCode() {
        return this.playerName.hashCode();
    }
}