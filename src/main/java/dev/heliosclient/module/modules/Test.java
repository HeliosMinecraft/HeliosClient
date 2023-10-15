package dev.heliosclient.module.modules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.*;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import me.x150.renderer.render.Renderer3d;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Test extends Module_ {
    BooleanSetting rectangle = new BooleanSetting("Rectangle", "", this, false);
    BooleanSetting rounded = new BooleanSetting("Rounded", "", this, false);
    BooleanSetting Circle = new BooleanSetting("Circle", "", this, false);
    BooleanSetting Triangle = new BooleanSetting("Triangle", "", this, false);
    StringSetting num = new StringSetting("Enter a number", "DESCRIPTION", "E", 100, InputBox.InputMode.ALL);
    String[] list = new String[]{"1ST", "2ND", "3RD", "LMAO"};
    StringListSetting stringListSetting = new StringListSetting("Enter a number", "DESCRIPTION", list, 4, 100, InputBox.InputMode.DIGITS_AND_CHARACTERS);

    BooleanSetting PartiallyRounded = new BooleanSetting("NotRounded", "", this, false);
    BooleanSetting GradientRounded = new BooleanSetting("gradientrounded", "", this, false);
    BooleanSetting Gradient = new BooleanSetting("Gradient", "", this, false);

    BooleanSetting Arc = new BooleanSetting("Arc", "", this, false);
    BooleanSetting TracerLine = new BooleanSetting("TracerLine", "", this, false);
    BooleanSetting blockOutlineAndFIll = new BooleanSetting("blockOutlineAndFIll", "", this, false);
    RGBASetting color = new RGBASetting("Color", "color", Color.WHITE);
    RGBASetting color2 = new RGBASetting("Color1", "color", Color.WHITE);
    RGBASetting color3 = new RGBASetting("Color2", "color", Color.WHITE);
    RGBASetting color4 = new RGBASetting("Color3", "color", Color.WHITE);
    RGBASetting color5 = new RGBASetting("Color4", "color", Color.WHITE);
    RGBASetting color6 = new RGBASetting("Color5", "color", Color.WHITE);


    public Test() {
        super("Test", "Render Test", Category.RENDER);
        settings.add(rectangle);
        settings.add(rounded);
        settings.add(Circle);
        settings.add(Arc);
        settings.add(Triangle);
        settings.add(TracerLine);
        settings.add(blockOutlineAndFIll);
        settings.add(PartiallyRounded);
        settings.add(num);
        settings.add(stringListSetting);
        settings.add(color);
        settings.add(color2);
        settings.add(color3);
        settings.add(color4);
        settings.add(color5);
        settings.add(color6);

        settings.add(GradientRounded);
        settings.add(Gradient);


        quickSettings.add(rectangle);
        quickSettings.add(rounded);
        quickSettings.add(Circle);
        quickSettings.add(Arc);
        quickSettings.add(Triangle);
        quickSettings.add(TracerLine);
        quickSettings.add(blockOutlineAndFIll);
        quickSettings.add(PartiallyRounded);
        quickSettings.add(num);
        quickSettings.add(color);

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
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 10, 10, 100, 50, 0xFFFF0000);

        // Draw a 2D rounded rectangle using the CustomRenderer class
        if (rounded.value)
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 120, 10, 10, 5, 1, Color.DARK_GRAY.getRGB());

        // Draw a 2D circle using the CustomRenderer class
        if (Circle.value)
            Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), 165, 35, 20, 0xFF0000FF);

        // Draw a 2D triangle using the CustomRenderer class
        if (Triangle.value)
            Renderer2D.drawTriangle(drawContext.getMatrices().peek().getPositionMatrix(), 10, 10, 100, 50, 60, 120, Color.WHITE.getRGB());

        // Draw a 2D arc using the CustomRenderer class
        if (Arc.value)
            Renderer2D.drawArc(drawContext.getMatrices().peek().getPositionMatrix(), 165, 35, 50,1f, Color.WHITE.getRGB(), 0, 90);

        //Not so rounded rectange
        if (PartiallyRounded.value)
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 20, 20, false, true, true, false, 120, 120, 10, 0xFF00FF00);

        if (GradientRounded.value)
            Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(),Color.BLUE,Color.WHITE, Color.BLACK,Color.GRAY, 22, 20, 40,40,2);

        if (Gradient.value)
            Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), 20, 20, 40,40,ColorUtils.getRainbowColor().getRGB(),Color.WHITE.getRGB());
    }
    @SubscribeEvent
    public void renderer3d(Render3DEvent event) {
        Renderer3d.renderThroughWalls();
        PlayerEntity player = HeliosClient.MC.player;
        Vec3d start = new Vec3d(player.getX() + 2,player.getY() + 1,player.getZ() - 3);
        Vec3d start2 = new Vec3d(50,player.getY() - 1,player.getZ() + 2);
        Vec3d start3 = new Vec3d(player.getX() - 5,player.getY() + 2,player.getZ() +6);


        Vec3d dimenstions = new Vec3d(1,1,1);
        Renderer3d.renderOutline(event.getMatrices(),Color.WHITE,start,dimenstions);
        Renderer3d.renderLine(event.getMatrices(),Color.yellow,start,player.getEyePos());
        Renderer3d.renderFilled(event.getMatrices(),Color.GREEN,start2,dimenstions);
        Renderer3d.renderEdged(event.getMatrices(),Color.CYAN,Color.BLACK,start3,dimenstions);

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
