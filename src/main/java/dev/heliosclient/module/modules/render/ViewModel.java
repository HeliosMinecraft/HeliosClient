package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.ArmRenderEvent;
import dev.heliosclient.event.events.render.HeldItemRendererEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.Vector3dSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class ViewModel extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting disableFoodAnimation = sgGeneral.add(new BooleanSetting.Builder()
            .name("Disable Food Animations")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting oldAnimations = sgGeneral.add(new BooleanSetting.Builder()
            .name("Old 1.8 Animations")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting swordAnimation = sgGeneral.add(new BooleanSetting.Builder()
            .name("Old Sword Blocking animation")
            .description("Displays 1.8 sword animations. Only visually, does not affect anything in game")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->oldAnimations.value)
            .build()
    );
    public DoubleSetting swingSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Swing Speed")
            .onSettingChange(this)
            .value(6.0)
            .defaultValue(6.0)
            .min(0.0)
            .max(22)
            .roundingPlace(1)
            .build()
    );
    public DoubleSetting offHandprog = sgGeneral.add(new DoubleSetting.Builder()
            .name("Offhand swing progress")
            .onSettingChange(this)
            .value(0d)
            .defaultValue(0d)
            .min(0)
            .max(1)
            .roundingPlace(1)
            .build()
    );
    public DoubleSetting mainHandprog = sgGeneral.add(new DoubleSetting.Builder()
            .name("MainHand swing progress")
            .onSettingChange(this)
            .value(0d)
            .defaultValue(0d)
            .min(0)
            .max(1)
            .roundingPlace(1)
            .build()
    );
    SettingGroup sgMainHand = new SettingGroup("MainHand");

    private final Vector3dSetting scaleMain = sgMainHand.add(new Vector3dSetting.Builder()
            .name("MainHand scale")
            .description("The scale of your main hand.")
            .value(1, 1, 1)
            .defaultValue(1, 1, 1)
            .max(5.0)
            .roundingPlace(1)
            .build()
    );
    private final Vector3dSetting posMain = sgMainHand.add(new Vector3dSetting.Builder()
            .name("MainHand position")
            .description("The position of your main hand.")
            .value(0, 0, 0)
            .defaultValue(0, 0, 0)
            .range(-3, 3)
            .roundingPlace(1)
            .build()
    );
    private final Vector3dSetting rotMain = sgMainHand.add(new Vector3dSetting.Builder()
            .name("MainHand rotation")
            .description("The rotation of your main hand.")
            .value(0, 0, 0)
            .defaultValue(0, 0, 0)
            .range(-180, 180)
            .roundingPlace(0)
            .build()
    );
    SettingGroup sgOffHand = new SettingGroup("OffHand");
    private final Vector3dSetting scaleOff = sgOffHand.add(new Vector3dSetting.Builder()
            .name("OffHand scale")
            .description("The scale of your off hand.")
            .value(1, 1, 1)
            .defaultValue(1, 1, 1)
            .max(5)
            .roundingPlace(1)
            .build()
    );
    // Offhand
    private final Vector3dSetting posOff = sgOffHand.add(new Vector3dSetting.Builder()
            .name("OffHand position")
            .description("The position of your off hand.")
            .value(0, 0, 0)
            .defaultValue(0, 0, 0)
            .range(-3, 3)
            .roundingPlace(1)
            .build()
    );
    private final Vector3dSetting rotOff = sgOffHand.add(new Vector3dSetting.Builder()
            .name("OffHand rotation")
            .description("The rotation of your off hand.")
            .value(0, 0, 0)
            .defaultValue(0, 0, 0)
            .range(-180, 180)
            .roundingPlace(0)
            .build()
    );
    SettingGroup sgArm = new SettingGroup("Arm");

    // Arm
    private final Vector3dSetting scaleArm = sgArm.add(new Vector3dSetting.Builder()
            .name("Arm scale")
            .defaultValue(1, 1, 1)
            .max(5)
            .roundingPlace(1)
            .build()
    );

    private final Vector3dSetting posArm = sgArm.add(new Vector3dSetting.Builder()
            .name("Arm position")
            .defaultValue(0, 0, 0)
            .range(-3, 3)
            .roundingPlace(1)
            .build()
    );

    private final Vector3dSetting rotArm = sgArm.add(new Vector3dSetting.Builder()
            .name("Arm rotation")
            .defaultValue(0, 0, 0)
            .range(-180, 180)
            .roundingPlace(0)
            .build()
    );


    public ViewModel() {
        super("ViewModel", "Changes the view of your hands on screen", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgMainHand);
        addSettingGroup(sgOffHand);
        addSettingGroup(sgArm);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgMainHand.getSettings());
        addQuickSettings(sgOffHand.getSettings());
        addQuickSettings(sgArm.getSettings());
    }

    @SubscribeEvent
    public void onHeldItemRender(HeldItemRendererEvent event) {
        if(oldAnimations.value && swordAnimation.value && mc.player.getMainHandStack().getItem() instanceof SwordItem && mc.options.useKey.isPressed()) {
            if (event.getHand() == Hand.MAIN_HAND) {
                event.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(-95));
                event.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
                event.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80));
                event.getMatrix().translate(-0.050, -0.080, 0.050);
            }
            if (event.getHand() == Hand.OFF_HAND && mc.player.getOffHandStack().getItem() == Items.SHIELD) {
                event.getMatrix().scale(0, 0, 0);
            }
        }

        if (event.getHand() == Hand.MAIN_HAND) {
            rotate(event.getMatrix(), rotMain.value);
            scale(event.getMatrix(), scaleMain.value);
            translate(event.getMatrix(), posMain.value);
        } else {
            rotate(event.getMatrix(), rotOff.value);
            scale(event.getMatrix(), scaleOff.value);
            translate(event.getMatrix(), posOff.value);
        }
    }

    @SubscribeEvent
    public void onRenderArm(ArmRenderEvent event) {
        rotate(event.getMatrix(), rotArm.value);
        scale(event.getMatrix(), scaleArm.value);
        translate(event.getMatrix(), posArm.value);
    }

    private void rotate(MatrixStack matrix, Vec3d rotation) {
        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rotation.x));
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) rotation.y));
        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rotation.z));
    }

    private void scale(MatrixStack matrix, Vec3d scale) {
        matrix.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }

    private void translate(MatrixStack matrix, Vec3d translation) {
        matrix.translate((float) translation.x, (float) translation.y, (float) translation.z);
    }

    public boolean oldAnimations() {
        return isActive() && oldAnimations.value;
    }
}
