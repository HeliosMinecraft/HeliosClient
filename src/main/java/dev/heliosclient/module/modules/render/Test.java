package dev.heliosclient.module.modules.render;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyboardInputEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.pathfinder.*;
import dev.heliosclient.module.modules.render.pathfinder.goals.*;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.lists.ItemListSetting;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.inputbox.InputBox;
import dev.heliosclient.util.render.GradientBlockRenderer;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;


@Deprecated(forRemoval = true)
public class Test extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting rectangle = sgGeneral.add(new BooleanSetting("Rectangle", "", this, false, () -> true, false));
    BooleanSetting rectangle2 = sgGeneral.add(new BooleanSetting("rectangle2 with gradient", "", this, false, () -> true, false));

    BooleanSetting rounded = sgGeneral.add(new BooleanSetting("Rounded", "", this, false, () -> true, false));
    BooleanSetting Circle = sgGeneral.add(new BooleanSetting("Circle", "", this, false, () -> true, false));
    BooleanSetting Triangle = sgGeneral.add(new BooleanSetting("Triangle", "", this, false, () -> true, false));
    StringSetting num = sgGeneral.add(new StringSetting("Enter a number 2", "DESCRIPTION", "E", 100, this, InputBox.InputMode.ALL, () -> true, "E"));
    String[] list = new String[]{"1ST", "2ND", "3RD", "LMAO"};
    StringListSetting stringListSetting = sgGeneral.add(new StringListSetting("Enter a number", "DESCRIPTION", list, 4, 100, InputBox.InputMode.PREDICATE(c -> c == 'A' || c == 'a'), () -> true));

    BooleanSetting PartiallyRounded = sgGeneral.add(new BooleanSetting("NotRounded", "", this, false, () -> true, false));
    BooleanSetting GradientRounded = sgGeneral.add(new BooleanSetting("gradientrounded", "", this, false, () -> true, false));
    BooleanSetting Gradient = sgGeneral.add(new BooleanSetting("Gradient", "", this, false, () -> true, false));

    BooleanSetting Arc = sgGeneral.add(new BooleanSetting("Arc", "", this, false, () -> true, false));
    BooleanSetting TracerLine = sgGeneral.add(new BooleanSetting("TracerLine", "", this, false, () -> true, false));
    BooleanSetting blockOutlineAndFIll = sgGeneral.add(new BooleanSetting("blockOutlineAndFIll", "", this, false, () -> true, false));
    RGBASetting color = sgGeneral.add(new RGBASetting("Color", "color", Color.WHITE, false, this, () -> true));

    ItemListSetting itemListSetting = sgGeneral.add(new ItemListSetting.Builder()
            .name("Items")
            .description("Items2")
            .items(new ArrayList<>())
            .filter(item -> true)
            .build()
    );
    GradientSetting gradientSetting = sgGeneral.add(new GradientSetting.Builder()
            .name("gradientSetting")
            .description("gradientSetting2")
            .defaultValue(GradientManager.getGradient("Rainbow"))
            .onSettingChange(this)
            .build()
    );

    private float calibrate(boolean zAxis) {
        if (zAxis) {
            return Math.abs(mc.player.headYaw) / 90 - 1;
        } else {
            return (float) (2 / Math.PI * (Math.asin(Math.sin(mc.player.headYaw / 180 * Math.PI))));
        }
    }

    public Test() {
        super("Test", "Render Test", Categories.RENDER);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }
    Node goalNode = Node.fromBlockPos(new BlockPos(42,72,279));
    Node startNode = null;
    AStarPathFinderTickBased pathFinder = null;
    Node nextNode = null;
    PlayerMovement movement = null;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world != null && mc.player != null) {
            startNode = Node.fromBlockPos(mc.player.getBlockPos());
            nextNode = startNode;
            pathFinder = new AStarPathFinderTickBased(startNode, new GoalXZ(goalNode));
            movement = new PlayerMovement(pathFinder);

            //movement.setPath(pathFinder.getPath().toArray(new Node[0]));
        }

        GradientBlockRenderer.renderGradientBlock(
                ColorManager.INSTANCE::getPrimaryGradientStart,
                ColorManager.INSTANCE::getPrimaryGradientEnd,
                new BlockPos(100, 70, 100),
                true,
                1020300,
                QuadColor.CardinalDirection.NORTH
        );
    }


    @Override
    public void onDisable() {
        super.onDisable();
        GradientBlockRenderer.clearGradientBlocks();
    }

    @SubscribeEvent
    public void render(RenderEvent event) {
        // Draw a 2D rectangle using the CustomRenderer class
        DrawContext drawContext = event.getDrawContext();
        if (rectangle.value)
            Renderer2D.drawRectangleWithShadow(drawContext.getMatrices(), 10, 10, 100, 50, 0xFFFF0000, 10);

        if (rounded.value)
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 10, 10, 200, 100, 0xFFFF0000, 10);


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
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(pathFinder != null){
            pathFinder.processNextTick();
        }
    }

    @SubscribeEvent
    public void onE(KeyboardInputEvent e){
        movement.movePlayer(e);
    }

    @SubscribeEvent
    public void renderer3d(Render3DEvent event) {
        PlayerEntity player = HeliosClient.MC.player;

        /*
        if (player != null && player.getWorld() != null) {
            Renderer3D.renderThroughWalls();
            WireframeEntityRenderer.render(player, 1f, QuadColor.single(255, 255, 255, 100), LineColor.single(-1), 1f, true, true, true);
            Renderer3D.stopRenderingThroughWalls();
        }

         */


        if (pathFinder != null) {
            Node prevNode = Node.fromBlockPos(mc.player.getBlockPos());
           // pathFinder.setAndRecreate(prevNode,goalNode);
            int i = 0;
            for (Node node : pathFinder.getPath()) {
                if(i > 0) {
                    int color = getColorForCost(node.getFCost());
                    Renderer3D.drawBoxBoth(new Box(node.toBlockPos()).contract(0.53f), QuadColor.single(Color.DARK_GRAY.getRGB()), 1.2f);
                    Renderer3D.drawLine(node.toBlockPos().toCenterPos(), prevNode.toBlockPos().toCenterPos(), LineColor.single(color), 1.2f);
                }
                prevNode = node;
                i++;
            }
        }
    }
    private int getColorForCost(double cost) {
        float ratio = (float) ((cost - Double.MIN_VALUE) / (Double.MAX_VALUE - Double.MIN_VALUE));
        int red = (int) (255 * ratio);
        int green = (int) (255 * (1 - ratio));
        return ColorUtils.rgbaToInt(red,green,0,245);
    }
}
