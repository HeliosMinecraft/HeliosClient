package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.Option;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class InventoryUtils {
    private static final PlayerEntity player = HeliosClient.MC.player;
    private static int previousHotbarSlot = -1;
    private static int previousInventorySlot = -1;
    private static ItemStack previousHotbarItem = ItemStack.EMPTY;
    private static ItemStack previousInventoryItem = ItemStack.EMPTY;

    // Swap back to the previous slot in the hotbar
    public static void swapBackHotbar() {
        if (previousHotbarSlot != -1 && !previousHotbarItem.isEmpty()) {
            ItemStack currentItem = player.getInventory().getStack(previousHotbarSlot);
            player.getInventory().setStack(previousHotbarSlot, previousHotbarItem);
            previousHotbarItem = currentItem;
        }
    }

    // Swap back to the previous slot in the inventory
    public static void swapBackInventory() {
        if (previousInventorySlot != -1 && !previousInventoryItem.isEmpty()) {
            ItemStack currentItem = player.getInventory().getStack(previousInventorySlot);
            player.getInventory().setStack(previousInventorySlot, previousInventoryItem);
            previousInventoryItem = currentItem;
        }
    }

    // Swap items between hotbar and inventory and remember the previous state old
    public static void swapHotbarAndInventoryPrvO(int hotbarSlot, int inventorySlot) {
        // Remember the current state
        previousHotbarSlot = hotbarSlot;
        previousHotbarItem = player.getInventory().getStack(hotbarSlot);
        previousInventorySlot = inventorySlot;
        previousInventoryItem = player.getInventory().getStack(inventorySlot + 9);

        // Swap the items
        player.getInventory().setStack(hotbarSlot, previousInventoryItem);
        player.getInventory().setStack(inventorySlot + 9, previousHotbarItem);
    }

    // Swap items between hotbar and inventory
    public static void swapHotbarAndInventory(int hotbarSlot, int inventorySlot) {
        // Get the ItemStack in the hotbar slot
        ItemStack hotbarItem = player.getInventory().getStack(hotbarSlot);

        // Get the ItemStack in the inventory slot
        ItemStack inventoryItem = player.getInventory().getStack(inventorySlot + 9); // The player's inventory starts at slot 9

        // Swap the ItemStacks
        player.getInventory().setStack(hotbarSlot, inventoryItem);
        player.getInventory().setStack(inventorySlot + 9, hotbarItem);
    }

    // Swap items between inventory and container
    public static void swapInventoryAndContainer(int inventorySlot, Inventory container, int containerSlot) {
        // Get the ItemStack in the inventory slot
        ItemStack inventoryItem = player.getInventory().getStack(inventorySlot);

        // Get the ItemStack in the container slot
        ItemStack containerItem = container.getStack(containerSlot);

        // Swap the ItemStacks
        player.getInventory().setStack(inventorySlot, containerItem);
        container.setStack(containerSlot, inventoryItem);
    }

    // Check if player can swap the specified item
    public static boolean canSwapItem(ItemStack itemStack) {
        // Check if the player's inventory contains the itemStack
        return player.getInventory().contains(itemStack);
    }

    // Get the count of the specified item in the player's inventory
    public static int getItemCount(ItemStack itemStack) {
        return player.getInventory().count(itemStack.getItem());
    }

    // Drop the specified item from the player's inventory
    public static void dropItem(ItemStack itemStack) {
        player.dropItem(itemStack, false);
    }

    // Drop all items of the same type as the specified item from the player's inventory
    public static void dropAllItems(ItemStack itemStack) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i) == itemStack) {
                dropItem(player.getInventory().removeStack(i));
            }
        }
    }

    // Move the specified item from the player's inventory to their hotbar
    public static void moveItemToHotbar(ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                for (int j = 9; j < player.getInventory().size(); j++) {
                    if (player.getInventory().getStack(j) == itemStack) {
                        swapHotbarAndInventory(i, j - 9);
                        break;
                    }
                }
                break;
            }
        }
    }

    // Find an item in the inventory
    public static int findItemInInventory(ItemStack itemStack) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i) == itemStack) {
                return i;
            }
        }
        return -1; // Return -1 if the item was not found
    }

    // Find an item in the hotbar
    public static int findItemInHotbar(ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i) == itemStack) {
                return i;
            }
        }
        return -1; // Return -1 if the item was not found
    }

    // Get an empty slot in the inventory
    public static int getEmptySlot() {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1; // Return -1 if no empty slot was found
    }

    public static ItemStack getFastestTool(BlockState blockState) {
        ItemStack fastestTool = ItemStack.EMPTY;
        float fastestSpeed = 0.0F;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack itemStack = player.getInventory().getStack(i);
            if (itemStack.getItem() instanceof ToolItem) {
                float speed = itemStack.getMiningSpeedMultiplier(blockState);
                if (speed > fastestSpeed) {
                    fastestSpeed = speed;
                    fastestTool = itemStack;
                }
            }
        }

        return fastestTool; // Return the fastest tool, or an empty ItemStack if no tools were found
    }

    public static ArrayList<Option<Item>> getItemNames() {
        ArrayList<Option<Item>> options = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            String itemName = Text.translatable(item.getTranslationKey()).getLiteralString();
            options.add(new Option<>(itemName, item, false));
        }
        return options;
    }
}
