package dev.heliosclient.module.modules;

import dev.heliosclient.event.*;
import dev.heliosclient.event.events.*;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.Renderer3D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


public class Test extends Module_ implements Listener {
    BooleanSetting rectangle = new BooleanSetting("Rectangle","",this,false);
    BooleanSetting rounded = new BooleanSetting("Rounded","",this,false);
    BooleanSetting Circle = new BooleanSetting("Circle","",this,false);
    BooleanSetting Triangle = new BooleanSetting("Triangle","",this,false);

    BooleanSetting PartiallyRounded = new BooleanSetting("NotRounded","",this,false);
    BooleanSetting Arc = new BooleanSetting("Arc","",this,false);
    BooleanSetting TracerLine = new BooleanSetting("TracerLine","",this,false);
    BooleanSetting blockOutlineAndFIll = new BooleanSetting("blockOutlineAndFIll","",this,false);


    public Test() {
        super("Test", "Render Test",  Category.RENDER);
        settings.add(rectangle);
        settings.add(rounded);
        settings.add(Circle);
        settings.add(Arc);
        settings.add(Triangle);
        settings.add(TracerLine);
        settings.add(blockOutlineAndFIll);
        settings.add(PartiallyRounded);
        quickSettings.add(rectangle);
        quickSettings.add(rounded);
        quickSettings.add(Circle);
        quickSettings.add(Arc);
        quickSettings.add(Triangle);
        quickSettings.add(TracerLine);
        quickSettings.add(blockOutlineAndFIll);
        quickSettings.add(PartiallyRounded);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        EventManager.register(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventManager.unregister(this);
    }

    @Override
    public void render(DrawContext drawContext, float tickDelta, CallbackInfo info) {
        // Draw a 2D rectangle using the CustomRenderer class
        if (rectangle.value)
            Renderer2D.fill(drawContext,10, 10, 100, 50, 0xFFFF0000);

        // Draw a 2D rounded rectangle using the CustomRenderer class
        if (rounded.value)
            Renderer2D.drawRoundedRectangle(drawContext,120, 10, 10, 5, 1, Color.DARK_GRAY.getRGB());

        // Draw a 2D circle using the CustomRenderer class
        if (Circle.value)
            Renderer2D.drawFilledCircle(drawContext,165, 35, 20, 0xFF0000FF);

        // Draw a 2D triangle using the CustomRenderer class
        if (Triangle.value)
            Renderer2D.drawTriangle(drawContext,10, 10, 100, 50, 60, 120, Color.WHITE.getRGB());

        // Draw a 2D arc using the CustomRenderer class
        if (Arc.value)
            Renderer2D.drawArc(drawContext,165, 35, 50, 0, 90, Color.WHITE.getRGB());

        //Not so rounded rectange
        if (PartiallyRounded.value)
            Renderer2D.drawRoundedRectangle(drawContext, 20, 20, false, true, true, false, 120, 120, 10, 0xFF00FF00);

        Entity entity = mc.player;
        // Draw a 3D block outline using the CustomRenderer3D class
        if (entity != null) {
            BlockPos blockPos = new BlockPos(entity.getBlockX() + 5, entity.getBlockY(), entity.getBlockZ() - 5);
                if (blockOutlineAndFIll.value) {
                    Renderer3D.drawBlockOutline(drawContext, blockPos, 2.0f, 0xFF0000FF);
                    //Renderer3D.drawFilledBox(drawContext, blockPos, 0xFF0000FF);
                    //  Renderer3D.drawTracerLine(blockPos,2.0f, Color.WHITE.getRGB(),drawContext);
                }
                // Draw a tracer line towards an entity using the CustomRenderer3D class
                if (TracerLine.value) {
                    System.out.println("drawing at" + blockPos);
                    Renderer3D.drawLineFromPlayer(drawContext, blockPos.toCenterPos(), Color.WHITE.getRGB(), 5.0f, tickDelta);
            }
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void atTick(TickEvent event) {
        System.out.println("Tick");
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        System.out.println("Join");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)

    public void onPlayerLeaveEvent(PlayerLeaveEvent event){
        System.out.println("PlayerLeaveEvent");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        System.out.println("PlayerDeathEvent");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event){
        System.out.println("PlayerRespawnEvent");
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void itemDropEvent(ItemDropEvent event){
        System.out.println("ItemDropEvent");
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void itemPickUpEvent(ItemPickupEvent event){
        System.out.println("ItemPickupEvent");
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void onChatMsg(ChatMessageEvent event){
        System.out.println("ChatMessageEvent");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void BlockPlaceEvent(BlockPlaceEvent event){
        System.out.println("BlockPlaceEvent");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, side = Dist.CLIENT)
    public void BlockBreakEvent(BlockBreakEvent event){
        System.out.println("BlockBreakEvent");
    }


}
