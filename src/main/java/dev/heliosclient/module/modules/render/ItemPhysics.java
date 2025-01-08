package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.entity.ItemPhysicsEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.item.ItemStack;

public class ItemPhysics extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting accurateAmount = sgGeneral.add(new BooleanSetting.Builder()
            .name("Accurate amount")
            .description("Displays the items stack accurately")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Scale of the items")
            .onSettingChange(this)
            .value(1.0)
            .defaultValue(1.0)
            .min(0.0)
            .max(2)
            .roundingPlace(2)
            .build()
    );

    public ItemPhysics() {
        super("ItemPhysics", "Applies physics to items", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @SubscribeEvent
    public void onItemPhysicsEvent(ItemPhysicsEvent event) {
        event.setCanceled(true);

        /*
        ItemStack itemStack = event.item.getStack();
        MatrixStack matrixStack = event.matrixStack;
        int j = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        Random random = Random.create(j);
        BakedModel bakedModel = event.itemEntityRenderState.seed.(itemStack, event.item.getWorld(), null, event.item.getId());
        boolean hasDepth = bakedModel.hasDepth();
        int amount = getRenderedAmount(itemStack);
        float scale = this.scale.value > 0.0 ? (float) this.scale.value : bakedModel.getTransformation().ground().scale.x();
        float m = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
        matrixStack.translate(0.0F, 0.25F * m, 0.0F);
        if (!hasDepth) {
            matrixStack.scale(scale, scale, scale);
            matrixStack.translate(0.0, -0.09375F * (amount - 1) * 0.01F, 0.0);
        }
        if (itemStack.getItem() != Items.DRAGON_HEAD || itemStack.getItem() != Items.SHIELD) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        }
        Random rotationRandom = Random.create(itemStack.hashCode());
        float rotationAngle = rotationRandom.nextFloat() * 360.0F;
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle));

        for (int u = 0; u < amount; ++u) {
            matrixStack.push();
            if (u > 0 && !hasDepth) {
                matrixStack.translate(random.nextFloat() * 0.3F - 0.15F, random.nextFloat() * 0.3F - 0.15F, random.nextFloat() * 0.3F - 0.10F);
            }

            event.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, event.i, OverlayTexture.DEFAULT_UV, matrixStack, event.vertexConsumerProvider, mc.world,0);
            matrixStack.pop();
        }

         */
    }

    private int getRenderedAmount(ItemStack stack) {
        if (accurateAmount.value)
            return stack.getCount();
        return stack.getCount() > 48 ? 5 : stack.getCount() > 32 ? 4 : stack.getCount() > 16 ? 3 : stack.getCount() > 1 ? 2 : 1;
    }
}
