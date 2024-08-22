package dev.heliosclient.scripting.libraries;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.Option;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;

public class PlayerLib extends TwoArgFunction {
    private final PlayerEntity player;

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
        library.set("getFastestToolAB", new getFastestToolAntibreak());
        library.set("getItemNames", new getItemNames());
        library.set("getItemCount", new getItemCount());
        library.set("canSwapItem", new canSwapItem());
        library.set("swapBackHotbar", new swapBackHotbar());
        library.set("lookAtCoords", new lookAtCoords());
        library.set("lookAtEntity", new lookAtEntity());
        library.set("lookAtVec3d", new lookAtVec3d());

        env.set("PlayerLib", library);
        return library;
    }

    static class getItemCount extends OneArgFunction {
        public LuaValue call(LuaValue itemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack stack = (ItemStack) itemStack.checkuserdata(ItemStack.class);

            return LuaValue.valueOf(InventoryUtils.getItemCount(stack));
        }
    }

    static class canSwapItem extends OneArgFunction {
        public LuaValue call(LuaValue itemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack stack = (ItemStack) itemStack.checkuserdata(ItemStack.class);

            return LuaValue.valueOf(InventoryUtils.canSwapItem(stack));
        }
    }

    static class swapBackHotbar extends ZeroArgFunction {
        public LuaValue call() {
            InventoryUtils.swapBackHotbar();
            return NIL;
        }
    }

    static class dropAllItems extends OneArgFunction {
        public LuaValue call(LuaValue luaItem) {
            // Convert the LuaValue to a Java Item object
            Item item = (Item) luaItem.checkuserdata(Item.class);
            InventoryUtils.dropAllItems(item);
            return NIL;
        }
    }

    static class dropAllItemStack extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.checkuserdata(ItemStack.class);
            InventoryUtils.dropAllItems(itemStack.getItem());
            return NIL;
        }
    }

    static class moveItemToHotbar extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            ItemStack itemStack = (ItemStack) luaItemStack.checkuserdata(ItemStack.class);
            InventoryUtils.moveItemToHotbar(itemStack);
            return NIL;
        }
    }

    static class findItemInHotbar extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            Item item = (Item) luaItemStack.checkuserdata(ItemStack.class);
            InventoryUtils.findItemInHotbar(item);
            return NIL;
        }
    }

    static class findItemInInventory extends OneArgFunction {
        public LuaValue call(LuaValue luaItemStack) {
            // Convert the LuaValue to a Java ItemStack object
            Item Item = (Item) luaItemStack.checkuserdata(ItemStack.class);
            InventoryUtils.findItemInInventory(Item);
            return NIL;
        }
    }

    static class getEmptySlot extends ZeroArgFunction {
        public LuaValue call() {
            return LuaInteger.valueOf(HeliosClient.MC.player.getInventory().getEmptySlot());
        }
    }

    static class getFastestTool extends OneArgFunction {
        public LuaValue call(LuaValue blockState) {
            // Convert the LuaValue to a Java blockstate object
            BlockState state = (BlockState) blockState.checkuserdata(BlockState.class);
            LuaValue value = CoerceJavaToLua.coerce(InventoryUtils.getFastestTool(state, false));

            return value;
        }
    }

    static class getFastestToolAntibreak extends TwoArgFunction {
        public LuaValue call(LuaValue blockState, LuaValue antibreak) {
            BlockState state = (BlockState) blockState.checkuserdata(BlockState.class);
            boolean antiBreak = antibreak.checkboolean();

            LuaValue value = CoerceJavaToLua.coerce(InventoryUtils.getFastestTool(state, antiBreak));

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

    static class lookAtCoords extends ThreeArgFunction {
        public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
            double x1 = x.todouble();
            double y1 = y.todouble();
            double z1 = z.todouble();

            RotationUtils.lookAt(x1, y1, z1);
            return NIL;
        }
    }

    static class lookAtEntity extends OneArgFunction {
        public LuaValue call(LuaValue entity) {
            Entity entity1 = (Entity) entity.checkuserdata(Entity.class);
            RotationUtils.lookAt(entity1, RotationUtils.LookAtPos.CENTER);
            return NIL;
        }
    }

    static class lookAtVec3d extends OneArgFunction {
        public LuaValue call(LuaValue vec3dTable) {
            LuaTable vec3d = vec3dTable.checktable();
            System.out.println(vec3d.get(0));

            RotationUtils.lookAt(new Vec3d(vec3d.get(0).todouble(), vec3d.get(1).todouble(), vec3d.get(2).todouble()));
            return NIL;
        }
    }

    class rotate extends TwoArgFunction {
        public LuaValue call(LuaValue yaw, LuaValue pitch) {
            player.setYaw(yaw.tofloat());
            player.setPitch(pitch.tofloat());
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
            BlockUtils.breakBlock(pos, true);
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
