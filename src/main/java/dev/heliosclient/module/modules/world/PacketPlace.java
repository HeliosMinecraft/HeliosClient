package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BlockInteractEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketPlace extends Module_ {
    public PacketPlace() {
        super("PacketPlace","Places blocks using packets", Categories.WORLD);
    }
    int sequence = 0;

    @SubscribeEvent
    public void onBlockInteract(BlockInteractEvent event){
        if(mc.player == null) return;
        BlockHitResult hitResult = event.getHitResult();

     //   if(!BlockUtils.canPlace(hitResult.getBlockPos(),mc.world.getBlockState(hitResult.getBlockPos())))return;
        event.setCanceled(true);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,hitResult,sequence));
        sequence++;
    }
}
