package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ChatUtils;

import java.util.HashSet;
import java.util.Objects;

public class FriendManager {
    private static final HashSet<String> friendsName = new HashSet<>();
    private static HashSet<Friend> friends = new HashSet<>();

    public FriendManager() {
        for (Friend friend : friends) {
            friendsName.add(friend.playerName());
        }
    }

    public static boolean isFriend(Friend friend) {
        return friends.contains(friend);
    }

    public static boolean isFriend(String userName) {
        return friendsName.contains(userName);
    }

    public static void addFriend(Friend friend) {
        if (Objects.equals(friend.playerName(), HeliosClient.MC.getSession().getUsername())) {
            ChatUtils.sendHeliosMsg("You can't befriend yourself.");
        } else {
            friends.add(friend);
            friendsName.add(friend.playerName());
        }
    }

    public static void removeFriend(Friend friend) {
        friends.remove(friend);
        friendsName.remove(friend.playerName());
    }

    public static HashSet<Friend> getFriends() {
        return friends;
    }

    public static void setFriends(HashSet<Friend> friends) {
        FriendManager.friends = friends;
    }

    public static HashSet<String> getFriendsName() {
        return friendsName;
    }
}
