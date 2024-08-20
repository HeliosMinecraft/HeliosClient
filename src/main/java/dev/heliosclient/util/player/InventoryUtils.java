package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.Option;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.function.Predicate;

public class InventoryUtils {
    private static int previousHotbarSlot = -1;

    // Swap back to the previous slot in the hotbar
    public static void swapBackHotbar() {
        if (previousHotbarSlot != -1) {
            HeliosClient.MC.player.getInventory().selectedSlot = previousHotbarSlot;
            HeliosClient.MC.interactionManager.syncSelectedSlot();
        }
    }



    public static boolean swapToSlot(int hotbarSlot, boolean swapBack) {
        if (hotbarSlot == PlayerInventory.OFF_HAND_SLOT) return true;
        if (HeliosClient.MC.player.getInventory().selectedSlot == hotbarSlot) return true;
        if (hotbarSlot < 0 || hotbarSlot > 8) return false;
        if (swapBack) {
            previousHotbarSlot = HeliosClient.MC.player.getInventory().selectedSlot;
        }

        HeliosClient.MC.player.getInventory().selectedSlot = hotbarSlot;
        HeliosClient.MC.interactionManager.syncSelectedSlot();

        return true;
    }

    // Check if HeliosClient.MC.player can swap the specified item
    public static boolean canSwapItem(ItemStack itemStack) {
        // Check if the HeliosClient.MC.player's inventory contains the itemStack
        return HeliosClient.MC.player.getInventory().contains(itemStack);
    }

    public static boolean doesAnyHandStackHas(Predicate<ItemStack> stackFil){
        return stackFil.test(HeliosClient.MC.player.getMainHandStack()) || stackFil.test(HeliosClient.MC.player.getOffHandStack());
    }

    // Get the count of the specified item in the player's inventory
    public static int getItemCount(ItemStack itemStack) {
        if(itemStack == null){
            return 0;
        }

        return HeliosClient.MC.player.getInventory().count(itemStack.getItem());
    }
    public static int getItemStackCountSafe(ItemStack itemStack) {
        if(itemStack == null){
            return 0;
        }

        return itemStack.getCount();
    }

    // Drop the specified item from the HeliosClient.MC.player's inventory
    public static void dropItem(ItemStack itemStack) {
        HeliosClient.MC.player.dropStack(itemStack);
    }

    // Drop all items of the same type as the specified item from the player's inventory
    public static void dropAllItems(Item item) {
        swapToSlot(HeliosClient.MC.player.getInventory().selectedSlot, true);
        for (int i = 0; i < 9; i++) {
            if (HeliosClient.MC.player.getInventory().getStack(i).getItem() == item) {
                swapToSlot(i, false);
                HeliosClient.MC.player.dropSelectedItem(true);
            }
        }
        swapBackHotbar();

        for (int i = 9; i < HeliosClient.MC.player.getInventory().size(); i++) {
            if (HeliosClient.MC.player.getInventory().getStack(i).getItem() == item) {
                HeliosClient.MC.interactionManager.clickSlot(HeliosClient.MC.player.currentScreenHandler.syncId, i, 0, SlotActionType.THROW, HeliosClient.MC.player);
            }
        }
    }

    // Move the specified item from the HeliosClient.MC.player's inventory to their hotbar
    public static void moveItemToHotbar(ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            if (HeliosClient.MC.player.getInventory().getStack(i).isEmpty()) {
                for (int j = 9; j < HeliosClient.MC.player.getInventory().size(); j++) {
                    if (HeliosClient.MC.player.getInventory().getStack(j) == itemStack) {
                        moveItem(i, j - 9, SlotActionType.PICKUP_ALL,SlotActionType.PICKUP_ALL);
                        break;
                    }
                }
                break;
            }
        }
    }

    // Find an item stack in the inventory
    public static int findItemStackInInventory(ItemStack itemStack) {
        for (int i = 0; i < HeliosClient.MC.player.getInventory().size(); i++) {
            if (HeliosClient.MC.player.getInventory().getStack(i) == itemStack) {
                return i;
            }
        }
        return -1; // Return -1 if the item was not found
    }

    // Find an item in the inventory
    public static int findItemInInventory(Item item) {
        for (int i = 0; i < HeliosClient.MC.player.getInventory().size(); i++) {
            if (HeliosClient.MC.player.getInventory().getStack(i) != null && HeliosClient.MC.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1; // Return -1 if the item was not found
    }

    // Find an item in the hotbar
    public static int findItemInHotbar(Item item) {
        if (item == null)
            return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = HeliosClient.MC.player.getInventory().getStack(i);

            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        //Offhand
        ItemStack stack = HeliosClient.MC.player.getInventory().getStack(PlayerInventory.OFF_HAND_SLOT);

        if (stack != null &&  stack.getItem() == item) {
            return PlayerInventory.OFF_HAND_SLOT;
        }
        return -1; // Return -1 if the item was not found
    }

    // Find an item in the hotbar
    public static int findInHotbar(Predicate<Item> predicate) {
        if (predicate == null)
            return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = HeliosClient.MC.player.getInventory().getStack(i);
            if (stack != null && predicate.test(stack.getItem())) {
                return i;
            }
        }
        //Offhand
        ItemStack stack = HeliosClient.MC.player.getInventory().getStack(PlayerInventory.OFF_HAND_SLOT);

        if (stack != null && predicate.test(stack.getItem())) {
            return PlayerInventory.OFF_HAND_SLOT;
        }
        return -1; // Return -1 if the item was not found
    }

    public static int getEmptySlot(Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1; // Return -1 if no empty slot was found
    }

    public static int getFastestTool(BlockState blockState, boolean antibreak) {
        int bestToolSlot = -1;
        float fastestSpeed = 0.0F;

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = HeliosClient.MC.player.getInventory().getStack(i);

            if ((blockState.getBlock() == Blocks.BAMBOO || blockState.getBlock() == Blocks.BAMBOO_SAPLING) && itemStack.getItem() instanceof SwordItem) {
                return i;
            }
            if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof ShearsItem) {

                //Check if the tool is effective for the block
                if (!itemStack.isSuitableFor(blockState)) continue;

                //Check if item damage is less than 4
                if (antibreak && itemStack.getMaxDamage() - itemStack.getDamage() < 4) continue;

                float speed = itemStack.getMiningSpeedMultiplier(blockState);

                //Account for efficiency enchantment
                if (speed > 1) {
                    int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                    if (efficiency > 0 && !itemStack.isEmpty())
                        speed += efficiency * efficiency + 1;
                }

                if (speed > fastestSpeed) {
                    fastestSpeed = speed;
                    bestToolSlot = i;
                }
            }
        }

        return bestToolSlot; // Return the fastest tool slot, or -1 if no tools were found
    }

    public static ArrayList<Option<Item>> getItemNames() {
        ArrayList<Option<Item>> options = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            String itemName = Text.translatable(item.getTranslationKey()).getLiteralString();
            //Why is this a thing?????????
            //Why did I add this. When did I add this. Do I have dementia? I think I am going to keep this for now.
            options.add(new Option<>(itemName, item, false));
        }
        return options;
    }

    /**
     * Moves an item from a slot using quick move.
     *
     * @param fromSlot The index of the slot to move the item from.
     */
    public static void moveItemQuickMove(int fromSlot) {
        ClientPlayerEntity player = HeliosClient.MC.player;
        if (player != null && HeliosClient.MC.currentScreen != null) {
            HeliosClient.MC.interactionManager.clickSlot(HeliosClient.MC.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.QUICK_MOVE, player);
        }
    }

    /**
     * Moves an item from a slot using quick move.
     *
     * @param fromSlot The index of the slot to move the item from.
     */
    public static void moveItemQuickMove(ScreenHandler handler, int fromSlot) {
        ClientPlayerEntity player = HeliosClient.MC.player;
        if (player != null && HeliosClient.MC.currentScreen != null) {
            HeliosClient.MC.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.QUICK_MOVE, player);
        }
    }

    /**
     * Moves an item from one slot to another using the specified action
     *
     * @param fromSlot The index of the slot to move the item from.
     * @param toSlot   The index of the slot to move the item to.
     */
    public static void moveItem(int fromSlot, int toSlot, SlotActionType fromAction,SlotActionType toAction) {
        ClientPlayerEntity player = HeliosClient.MC.player;
        if (player != null && HeliosClient.MC.currentScreen != null) {
            HeliosClient.MC.interactionManager.clickSlot(HeliosClient.MC.player.currentScreenHandler.syncId, fromSlot, 0, fromAction, player);
            HeliosClient.MC.interactionManager.clickSlot(HeliosClient.MC.player.currentScreenHandler.syncId, toSlot, 1, toAction, player);
        }
    }
}
