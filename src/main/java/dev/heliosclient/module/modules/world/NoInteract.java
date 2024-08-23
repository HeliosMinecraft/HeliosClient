package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BeginBreakingBlockEvent;
import dev.heliosclient.event.events.block.BlockInteractEvent;
import dev.heliosclient.event.events.entity.EntityInteractEvent;
import dev.heliosclient.event.events.player.PlayerAttackEntityEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class NoInteract extends Module_ {
    private SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting noBlockInteract = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Block Interact")
            .description("Cancels all interactions with a block like opening chests,shulkers,etc.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting noBlockMine = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Block Mine")
            .description("Prevents you from mining any block and cancels mining")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );

    BooleanSetting noEntityInteract = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Entity Interact")
            .description("Cancels all interactions with a entity")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting noEntityHit = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Entity Hit")
            .description("Cancels all attacks to an entity")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting noNameTagInteract = sgGeneral.add(new BooleanSetting.Builder()
            .name("No NameTag Interact")
            .description("Cancels all interactions and hit sent to an entity with a nametag")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );


    public NoInteract() {
        super("NoInteract","Prevents you from interacting with blocks or entities", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onInteractEntity(EntityInteractEvent event){
        if(noEntityInteract.value || isNameTagged(event.getEntity())){
            event.cancel();
        }
    }

    @SubscribeEvent
    public void onEntityHit(PlayerAttackEntityEvent.PRE event){
        if(noEntityHit.value || isNameTagged(event.getTarget())){
            event.cancel();
        }
    }

    private boolean isNameTagged(Entity entity){
        return noNameTagInteract.value && entity instanceof LivingEntity entity1 && entity1.hasCustomName();
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onInteractBlock(BlockInteractEvent event){
        if(noBlockInteract.value){
            event.cancel();
        }
    }
    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onAttackBlock(BeginBreakingBlockEvent event){
        if(noBlockMine.value){
            event.cancel();
        }
    }
}
