package dev.heliosclient.system;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FriendManager {
    // Declare a list of friends
    private static HashSet<Friend> friends = new HashSet<>();

    // Create a static method that returns true if the friend is in the set, false otherwise
    public static boolean isFriend(Friend friend) {
        return friends.contains(friend);
    }

    public static void setFriends(HashSet<Friend> friends) {
        FriendManager.friends = friends;
    }
    public static void addFriend(Friend friend){
        friends.add(friend);
    }
    public static void removeFriend(Friend friend){
        friends.remove(friend);
    }
}
