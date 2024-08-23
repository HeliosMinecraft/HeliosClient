package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

import static dev.heliosclient.hud.hudelements.ArmorHud.DamageMode.Bar;

public class ArmorHud extends HudElement {
    public SettingGroup sgSettings = new SettingGroup("Settings");

    public static HudElementData<ArmorHud> DATA = new HudElementData<>("ArmorHUD","Displays your armor and its damage",ArmorHud::new);

    private final CycleSetting damageMode = sgSettings.add(new CycleSetting.Builder()
            .name("DamageMode")
            .description("Mode to show the armor's damage in")
            .value(List.of(DamageMode.values()))
            .onSettingChange(this)
            .defaultListOption(Bar)
            .build()
    );
    private final BooleanSetting damageModeAbove = sgSettings.add(new BooleanSetting.Builder()
            .name("DamageMode Above")
            .description("Shows damage of the armor above the item")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    private final BooleanSetting vertical = sgSettings.add(new BooleanSetting.Builder()
            .name("Vertical")
            .description("Shows the armor in a vertical manner")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    public ArmorHud() {
        super(DATA);
        addSettingGroup(sgSettings);

        this.width = 20;
        this.height =  20 * 4 + 4;
        this.renderBg.setValue(true);
        this.rounded.setValue(true);
    }
    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        ItemStack helmet, chestplate, leggings, boots;

        if(isInHudEditor && mc.player == null){
            helmet = Items.NETHERITE_HELMET.getDefaultStack();
            chestplate = Items.NETHERITE_CHESTPLATE.getDefaultStack();
            leggings = Items.NETHERITE_LEGGINGS.getDefaultStack();
            boots = Items.NETHERITE_BOOTS.getDefaultStack();
        }else if(mc.player != null){
            helmet = mc.player.getEquippedStack(EquipmentSlot.HEAD);
            chestplate = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            leggings = mc.player.getEquippedStack(EquipmentSlot.LEGS);
            boots = mc.player.getEquippedStack(EquipmentSlot.FEET);
        } else{
            return;
        }

        int x = this.x;
        int y = this.y;

        if(this.damageModeAbove.value){
            y = y + 3;
        }

        drawContext.drawItem(helmet,x,y);
        drawDamageBar(drawContext,helmet,x,getDamageBarY(y));

        if(vertical.value){
            y += 20;
        }else{
            x += 20;
        }

        drawContext.drawItem(chestplate,x,y);
        drawDamageBar(drawContext,chestplate,x,getDamageBarY(y));

        if(vertical.value){
            y += 20;
        }else{
            x += 20;
        }

        drawContext.drawItem(leggings,x,y);
        drawDamageBar(drawContext,leggings,x,getDamageBarY(y));

        if(vertical.value){
            y += 20;
        }else{
            x += 20;
        }

        drawContext.drawItem(boots,x,y);
        drawDamageBar(drawContext,boots,x,getDamageBarY(y));

        // + 18 for the first item.
        this.width = x - this.x + 18;
        this.height = y - this.y + 20;
    }

    private int getDamageBarY(int orgY){
        if(damageModeAbove.value){
            return orgY - 2;
        } else{
            return orgY + 16;
        }
    }

    public void drawDamageBar(DrawContext context, ItemStack stack, int x, int y){
        if(stack == null || stack.isEmpty())return;
        //Gives percentage between 0 and 1
        double durabilityPercentage = ((double) stack.getMaxDamage() - (double) stack.getDamage()) / (double) stack.getMaxDamage();
        int barWidth = Math.round((float)durabilityPercentage * 16);
        int color = PlayerUtils.getDurabilityColor(durabilityPercentage);

        switch ((DamageMode)damageMode.getOption()){
            case Percentage -> {
                drawText(context, String.format("%.0f",durabilityPercentage * 100) + "%",x,y,color);
            }
            case Number -> {
                drawText(context,String.valueOf(stack.getMaxDamage() - stack.getDamage()),x,y,color);
            }
            case Bar -> {
                Renderer2D.drawRectangle(context.getMatrices().peek().getPositionMatrix(), x,y + 0.1f,barWidth,1,color);
            }
        }

    }

    private void drawText(DrawContext context,String text, int x, int y,int color){
        if(!Renderer2D.isVanillaRenderer()){
            x += Math.round(16.25 - FontRenderers.Super_Small_fxfontRenderer.getStringWidth(text))/2f;
            FontRenderers.Super_Small_fxfontRenderer.drawString(context.getMatrices(),text,x,y - 0.1f,color);
        }else{
            context.getMatrices().push();
            context.getMatrices().scale(0.5f,0.5f,1);
            x += (18 - (mc.textRenderer.getWidth(text) * 0.5f))/2f;

            float scaledX = (x / 0.5f);
            float scaledY = (y / 0.5f);
            context.drawText(mc.textRenderer,text,(int)scaledX,(int)scaledY - 1,color,false);
            context.getMatrices().pop();
        }
    }

    public enum DamageMode {
        Bar,
        Percentage,
        Number
    }
}
