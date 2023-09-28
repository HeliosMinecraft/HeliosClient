package dev.heliosclient.managers;

import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;

public class FriendManager {
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    // Declare a list of friends
    private static HashSet<Friend> friends = new HashSet<>();
    private static final HashSet<String> friendsName = new HashSet<>();

    public FriendManager() {
        for (Friend friend : friends) {
            friendsName.add(friend.getPlayerName());
        }
    }

    public static boolean isFriend(Friend friend) {
        return friends.contains(friend);
    }

    public static void addFriend(Friend friend) {
        if (friend.getPlayerName() == mc.getSession().getUsername()) {
            ChatUtils.sendHeliosMsg("You can't befriend yourself.");
        } else {
            friends.add(friend);
        }
    }

    public static void removeFriend(Friend friend) {
        friends.remove(friend);
    }

    public static HashSet<Friend> getFriends() {
        return friends;
    }

    public static void setFriends(HashSet<Friend> friends) {
        FriendManager.friends = friends;
    }

    public HashSet<String> getFriendsName() {
        return friendsName;
    }
}
