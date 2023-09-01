package dev.heliosclient.system;

import java.util.HashSet;

import dev.heliosclient.util.ChatUtils;
import net.minecraft.client.MinecraftClient;

public class FriendManager {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    // Declare a list of friends
    private static HashSet<Friend> friends = new HashSet<>();
    private static HashSet<String> friendsName = new HashSet<>();

    public static boolean isFriend(Friend friend) {
        return friends.contains(friend);
    }

    public static void setFriends(HashSet<Friend> friends) {
        FriendManager.friends = friends;
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
    public FriendManager(){
        for (Friend friend: friends){
            friendsName.add(friend.getPlayerName());
        }
    }

    public HashSet<String> getFriendsName() {
        return friendsName;
    }
}
