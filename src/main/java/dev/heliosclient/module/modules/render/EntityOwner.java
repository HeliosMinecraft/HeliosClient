package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.Text;

import java.awt.*;

import static dev.heliosclient.HeliosClient.MC;

public class EntityOwner extends Module_ {
    public EntityOwner() {
        super("EntityOwner", "Displays owner of tameable entities.", Categories.RENDER);
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof TameableEntity tameableEntity) {
                if (tameableEntity.isTamed()) {
                    String text;
                    if(tameableEntity.getOwner() == null) {
                        text = "Failed to get Owner";
                    }else{
                        Text displayName = tameableEntity.getOwner().getDisplayName();
                        if(displayName == null){
                          text = "Owner UUID: " +  tameableEntity.getOwner().getUuidAsString();
                          Renderer3D.drawText(FontRenderers.Mid_fxfontRenderer, "Owner name not found", (float) entity.getPos().x, (float) entity.getEyeY() + 0.5f, (float) entity.getPos().z, -(FontRenderers.Mid_fxfontRenderer.getStringWidth("Owner name not found") / 2.0f) + 0.1f, -FontRenderers.Mid_fxfontRenderer.getStringHeight(text) - 2, 1f, Color.RED.getRGB());
                        }else {
                          text = displayName.getString();
                        }
                    }
                    Renderer3D.drawText(FontRenderers.Mid_fxfontRenderer, text, (float) entity.getPos().x, (float) entity.getEyeY() + 0.5f, (float) entity.getPos().z, -(FontRenderers.Mid_fxfontRenderer.getStringWidth(text) / 2.0f) + 0.1f, 0, 1f, Color.WHITE.getRGB());
                }
            }
        }
    }
}
