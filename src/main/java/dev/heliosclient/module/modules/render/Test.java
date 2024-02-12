package dev.heliosclient.module.modules.render;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.event.events.block.BlockInteractEvent;
import dev.heliosclient.event.events.player.*;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import me.x150.renderer.render.Renderer3d;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static dev.heliosclient.util.Renderer2D.drawContext;


public class Test extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting rectangle = sgGeneral.add(new BooleanSetting("Rectangle", "", this, false, () -> true, false));
    BooleanSetting rectangle2 = sgGeneral.add(new BooleanSetting("rectangle2 with gradient", "", this, false, () -> true, false));

    BooleanSetting rounded = sgGeneral.add(new BooleanSetting("Rounded", "", this, false, () -> true, false));
    BooleanSetting Circle = sgGeneral.add(new BooleanSetting("Circle", "", this, false, () -> true, false));
    BooleanSetting Triangle = sgGeneral.add(new BooleanSetting("Triangle", "", this, false, () -> true, false));
    StringSetting num = sgGeneral.add(new StringSetting("Enter a number 2", "DESCRIPTION", "E", 100, InputBox.InputMode.ALL, () -> true, "E"));
    String[] list = new String[]{"1ST", "2ND", "3RD", "LMAO"};
    StringListSetting stringListSetting = sgGeneral.add(new StringListSetting("Enter a number", "DESCRIPTION", list, 4, 100, InputBox.InputMode.DIGITS_AND_CHARACTERS, () -> true));

    BooleanSetting PartiallyRounded = sgGeneral.add(new BooleanSetting("NotRounded", "", this, false, () -> true, false));
    BooleanSetting GradientRounded = sgGeneral.add(new BooleanSetting("gradientrounded", "", this, false, () -> true, false));
    BooleanSetting Gradient = sgGeneral.add(new BooleanSetting("Gradient", "", this, false, () -> true, false));

    BooleanSetting Arc = sgGeneral.add(new BooleanSetting("Arc", "", this, false, () -> true, false));
    BooleanSetting TracerLine = sgGeneral.add(new BooleanSetting("TracerLine", "", this, false, () -> true, false));
    BooleanSetting blockOutlineAndFIll = sgGeneral.add(new BooleanSetting("blockOutlineAndFIll", "", this, false, () -> true, false));
    RGBASetting color = sgGeneral.add(new RGBASetting("Color", "color", Color.WHITE, false, this, () -> true));

    public Test() {
        super("Test", "Render Test", Categories.RENDER);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @SubscribeEvent
    public void render(RenderEvent event) {
        // Draw a 2D rectangle using the CustomRenderer class
        DrawContext drawContext = event.getDrawContext();
        if (rectangle.value)
            Renderer2D.drawRectangleWithShadow(drawContext.getMatrices(), 10, 10, 100, 50, 0xFFFF0000, 10);

        // Draw a 2D rounded rectangle using the CustomRenderer class
        if (rounded.value)
            Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), 120, 10, 25, 15, 3, 20, Color.YELLOW.getRGB());

        // Draw a 2D circle using the CustomRenderer class
        if (Circle.value)
            Renderer2D.drawCircleWithShadow(drawContext.getMatrices(), 165, 35, 20, 10, 0x0AEE27);

        // Draw a 2D triangle using the CustomRenderer class
        if (Triangle.value)
            Renderer2D.drawTriangle(drawContext.getMatrices().peek().getPositionMatrix(), 10, 10, 100, 50, 60, 120, Color.WHITE.getRGB());

        // Draw a 2D arc using the CustomRenderer class
        if (Arc.value)
            Renderer2D.drawArc(drawContext.getMatrices().peek().getPositionMatrix(), 165, 35, 50, 1f, Color.WHITE.getRGB(), 0, 90);

        //Not so rounded rectange
        if (PartiallyRounded.value)
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 20, 20, false, true, true, false, 120, 120, 10, 0xFF00FF00);

        if (GradientRounded.value)
            Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), 22, 20, 40, 40, Color.BLUE, Color.WHITE, Color.BLACK, Color.GRAY, 2, 20, Color.WHITE);

        if (Gradient.value) {
        }


    }

    @SubscribeEvent
    public void renderer3d(Render3DEvent event) {
        PlayerEntity player = HeliosClient.MC.player;

        if (player != null && player.getWorld() != null) {
            Vec3d end = new Vec3d(200, 200, 200);
            Vec3d end2 = new Vec3d(200, player.getY(), 200);

            QuadColor gradient = QuadColor.gradient(Color.WHITE.getRGB(), Color.GREEN.getRGB(), QuadColor.CardinalDirection.SOUTH);

            Renderer3D.drawBoxBoth(new BlockPos(player.getBlockPos().getX() - 5, player.getBlockPos().getY() + 2, player.getBlockPos().getZ() + 6), gradient, 3);
            Renderer3D.drawLine(Renderer3D.getEyeTracer(), end, LineColor.gradient(Color.WHITE.getRGB(), Color.GREEN.getRGB()), 1f);
            Renderer3D.renderItem(Items.DIAMOND_BLOCK.getDefaultStack(), end);
            Renderer3D.drawItemWithPhysics(Items.DIAMOND_BLOCK.getDefaultStack(), end2, mc.getTickDelta());
        }
    }


    public void atTick(TickEvent.CLIENT event) {
        System.out.println("Client Tick");
    }

    public void atServerTick(TickEvent.WORLD event) {
        System.out.println("World Tick");
    }

    public void atPlayerTick(TickEvent.PLAYER event) {
        System.out.println("Player Tick");
    }


    @SubscribeEvent
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        System.out.println("Join");
    }

    @SubscribeEvent
    public void onPlayerLeaveEvent(PlayerLeaveEvent event) {
        System.out.println("PlayerLeaveEvent");
    }

    @SubscribeEvent
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        System.out.println("PlayerDeathEvent");
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        System.out.println("PlayerRespawnEvent");
    }

    @SubscribeEvent
    public void onPlayerDamageEvent(PlayerDamageEvent event) {
        System.out.println("PlayerDamageEvent");
    }

    public void onPlayerMotionEvent(PlayerMotionEvent event) {
        System.out.println("PlayerMotionEvent");
    }

    @SubscribeEvent
    public void itemDropEvent(ItemDropEvent event) {
        System.out.println("ItemDropEvent");
    }

    @SubscribeEvent
    public void itemPickUpEvent(ItemPickupEvent event) {
        System.out.println("ItemPickupEvent");
    }

    @SubscribeEvent
    public void onChatMsg(ChatMessageEvent event) {
        System.out.println("ChatMessageEvent");
    }

    @SubscribeEvent
    public void BlockPlaceEvent(BlockInteractEvent event) {
        System.out.println("BlockInteractEvent");
    }

    @SubscribeEvent
    public void BlockBreakEvent(BlockBreakEvent event) {
        System.out.println("BlockBreakEvent");
    }


}
