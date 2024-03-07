package dev.heliosclient.scripting.libraries;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.Option;
import dev.heliosclient.util.InventoryUtils;
import dev.heliosclient.util.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;

public class PlayerLib extends TwoArgFunction {
    private PlayerEntity player;

    public PlayerLib() {
        this.player = HeliosClient.MC.player;
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("rotate", new rotate());
        library.set("placeBlock", new placeBlock());
        library.set("breakBlock", new breakBlock());
        library.set("move", new move());
        library.set("dropAllItems", new dropAllItems());
        library.set("dropAllItemStack", new dropAllItemStack());
        library.set("moveItemToHotbar", new moveItemToHotbar());
        library.set("findItemInInventory", new findItemInInventory());
        library.set("findItemInHotbar", new findItemInHotbar());
        library.set("getEmptySlot", new getEmptySlot());
        library.set("getFastestTool", new getFastestTool());
        library.set("getItemNames", new getItemNames());
        library.set("getItemCount", new getItemCount());
        library.set("canSwapItem", new canSwapItem());
        library.set("swapHotbarAndInventory", new swapHotbarAndInventory());
        library.set("swapBackHotbar", new swapBackHotbar());
        library.set("swapBackInventory", new swapBackInventory());
        library.set("lookAtCoords", new lookAtCoords());


        env.set("PlayerLib", library);
        return library;
    }
    static class getItemCount extends OneArgFunction {
        public LuaValue call(LuaValue itemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack stack = (ItemStack) itemStack.touserdata(ItemStack.class);

            return LuaValue.valueOf(InventoryUtils.getItemCount(stack));
        }
    }
    static class canSwapItem extends OneArgFunction {
        public LuaValue call(LuaValue itemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack stack = (ItemStack) itemStack.touserdata(ItemStack.class);

            return LuaValue.valueOf(InventoryUtils.canSwapItem(stack));
        }
    }
    static class swapHotbarAndInventory extends TwoArgFunction {
        public LuaValue call(LuaValue hotbarSlot, LuaValue inventorySlot) {
            InventoryUtils.swapHotbarAndInventory(hotbarSlot.toint(),inventorySlot.toint());
            return NIL;
        }
    }
    static class swapBackHotbar extends ZeroArgFunction {
        public LuaValue call() {
            InventoryUtils.swapBackHotbar();
            return NIL;
        }
    }
    static class swapBackInventory extends ZeroArgFunction {
        public LuaValue call() {
            InventoryUtils.swapBackInventory();
            return NIL;
        }
    }

    static class dropAllItems extends OneArgFunction {
        public LuaValue call(LuaValue luaItem) {
            // Convert the LuaValue to a Java Item object
            Item item = (Item) luaItem.touserdata(Item.class);
            ItemStack itemStack = new ItemStack(item);
            InventoryUtils.dropAllItems(itemStack);
            return NIL;
        }
    }

    static class dropAllItemStack extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.touserdata(ItemStack.class);
            InventoryUtils.dropAllItems(itemStack);
            return NIL;
        }
    }
    static class moveItemToHotbar extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.touserdata(ItemStack.class);
            InventoryUtils.moveItemToHotbar(itemStack);
            return NIL;
        }
    }
    static class findItemInHotbar extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.touserdata(ItemStack.class);
            InventoryUtils.findItemInHotbar(itemStack);
            return NIL;
        }
    }
    static class findItemInInventory extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.touserdata(ItemStack.class);
            InventoryUtils.findItemInInventory(itemStack);
            return NIL;
        }
    }
    static class getEmptySlot extends ZeroArgFunction {
        public LuaValue call() {
            return LuaInteger.valueOf(InventoryUtils.getEmptySlot());
        }
    }
    static class getFastestTool extends OneArgFunction {
        public LuaValue call(LuaValue blockState) {
            // Convert the LuaValue to a Java blockstate object
            BlockState state = (BlockState) blockState.touserdata(BlockState.class);
            LuaValue value = CoerceJavaToLua.coerce(InventoryUtils.getFastestTool(state));

            return value;
        }
    }
    static class getItemNames extends ZeroArgFunction {
        public LuaValue call() {
            ArrayList<Option<Item>> itemNames = InventoryUtils.getItemNames();

            // Create a new Lua table
            LuaTable luaItemNames = new LuaTable();

            // Populate the Lua table with the item names
            for (int i = 0; i < itemNames.size(); i++) {
                Option<Item> option = itemNames.get(i);
                String itemName = option.getName();
                luaItemNames.set(i + 1, LuaValue.valueOf(itemName));
            }

            // Return the Lua table
            return luaItemNames;
        }
    }


    class rotate extends TwoArgFunction {
        public LuaValue call(LuaValue yaw, LuaValue pitch) {
            player.setYaw(yaw.tofloat());
            player.setPitch(pitch.tofloat());
            return NIL;
        }
    }
    class lookAtCoords extends ThreeArgFunction {
        public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
            RotationUtils.lookAt(x.todouble(),y.todouble(),z.todouble());
            return NIL;
        }
    }

    class placeBlock extends ThreeArgFunction {
        public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
            BlockPos pos = new BlockPos(x.toint(), y.toint(), z.toint());
            player.getWorld().setBlockState(pos, Blocks.STONE.getDefaultState());
            return NIL;
        }
    }

    class breakBlock extends ThreeArgFunction {
        public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
            BlockPos pos = new BlockPos(x.toint(), y.toint(), z.toint());
            player.getWorld().breakBlock(pos, true);
            return NIL;
        }
    }

    class move extends ThreeArgFunction {
        public LuaValue call(LuaValue dx, LuaValue dy, LuaValue dz) {
            player.updatePosition(player.getX() + dx.todouble(), player.getY() + dy.todouble(), player.getZ() + dz.todouble());
            return NIL;
        }
    }
}
