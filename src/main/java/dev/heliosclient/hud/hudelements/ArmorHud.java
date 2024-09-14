package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.heliosclient.FontChangeEvent;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.fontutils.fxFontRenderer;
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
    private final DoubleSetting textSize = sgSettings.add(new DoubleSetting.Builder()
            .name("Text Size")
            .description("Size of the durability shown")
            .min(1)
            .max(10)
            .defaultValue(4d)
            .onSettingChange(this)
            .roundingPlace(1)
            .shouldRender(()-> !damageMode.isOption(Bar))
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

    fxFontRenderer cFontRenderer;

    public ArmorHud() {
        super(DATA);
        addSettingGroup(sgSettings);

        this.width = 20;
        this.height =  20 * 4 + 4;
        this.renderBg.setValue(true);
        this.rounded.setValue(true);

    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if(setting == textSize && HeliosClient.shouldUpdate()){
            this.cFontRenderer = new fxFontRenderer(FontManager.fonts,textSize.getFloat());
        }
    }
    @SubscribeEvent
    public void onFontChange(FontChangeEvent e){
        if(mc.getWindow() == null) return;

        this.cFontRenderer = new fxFontRenderer(e.getFonts(),textSize.getFloat());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        if(cFontRenderer == null){
            this.cFontRenderer = new fxFontRenderer(FontManager.fonts,textSize.getFloat());
        }

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

        int x = this.x + 1;
        int y = this.y;

        if(this.damageModeAbove.value){
            y = y + 4;
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
            return damageMode.isOption(Bar) ? orgY - 2 : orgY - (int) Renderer2D.getCustomStringHeight(cFontRenderer);
        } else{
            return damageMode.isOption(Bar) ? orgY + 16 : orgY + 12 + (int) Renderer2D.getCustomStringHeight(cFontRenderer);
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
            x += Math.round(16.25 -cFontRenderer.getStringWidth(text))/2f;
            cFontRenderer.drawString(context.getMatrices(),text,x,y - 0.1f,color);
        }else{
            float scale = textSize.getFloat() * 0.5f;
            context.getMatrices().push();
            context.getMatrices().scale(scale,scale,1);
            x += (18 - (mc.textRenderer.getWidth(text) * scale))/2f;

            float scaledX = (x / scale);
            float scaledY = (y / scale);
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
