package dev.heliosclient.module.modules.render.hiteffect;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerAttackEntityEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.hiteffect.particles.OrbParticle;
import dev.heliosclient.module.modules.render.hiteffect.particles.TextParticle;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.lists.ParticleListSetting;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.inputbox.InputBox;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitEffect extends Module_ {

    private final Random random = Random.create();

    List<HitEffectParticle> particles = new CopyOnWriteArrayList<>();

    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting particleAmount = sgGeneral.add(new DoubleSetting.Builder()
            .name("Amount")
            .description("Amount of particles to be spawned")
            .onSettingChange(this)
            .defaultValue(10d)
            .value(10d)
            .min(0)
            .max(500f)
            .roundingPlace(0)
            .build()
    );
    DoubleSetting time_in_seconds = sgGeneral.add(new DoubleSetting.Builder()
            .name("Age (in s)")
            .description("The time in seconds for the effect to last for")
            .onSettingChange(this)
            .defaultValue(4d)
            .min(0d)
            .max(60d)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting randomColor = sgGeneral.add(new BooleanSetting.Builder()
            .name("Random Color")
            .description("Color for each particle will be chosen randomly")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    RGBASetting color = sgGeneral.add(new RGBASetting.Builder()
            .name("Color")
            .description("Color of effect")
            .onSettingChange(this)
            .rainbow(true)
            .defaultValue(Color.RED)
            .build()
    );

    DropDownSetting type = sgGeneral.add(new DropDownSetting.Builder()
            .name("Type")
            .description("Particle type to display")
            .onSettingChange(this)
            .value(List.of(EffectType.values()))
            .defaultListOption(EffectType.ORBS)
            .build()
    );
    // Orbs
    DoubleSetting gravityAmount = sgGeneral.add(new DoubleSetting.Builder()
            .name("Gravity Amount")
            .description("Amount of gravity effect applied to the orbs, affects newly spawned particles")
            .onSettingChange(this)
            .defaultValue(0.005d)
            .value(0.005d)
            .min(0d)
            .max(0.2d)
            .roundingPlace(3)
            .shouldRender(() -> type.getOption() == EffectType.ORBS)
            .build()
    );
    BooleanSetting randomRadius = sgGeneral.add(new BooleanSetting.Builder()
            .name("Random Radius")
            .description("Radius of the orbs will be random between 0.2d and 1.2d, affects newly spawned particles")
            .onSettingChange(this)
            .defaultValue(true)
            .shouldRender(() -> type.getOption() == EffectType.ORBS)
            .build()
    );
    BooleanSetting coolerPhysics = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cooler Physics")
            .description("The orbs will have cooler physics than realistic type")
            .onSettingChange(this)
            .defaultValue(true)
            .shouldRender(() -> type.getOption() == EffectType.ORBS)
            .build()
    );
    DoubleSetting orbsRadius = sgGeneral.add(new DoubleSetting.Builder()
            .name("Radius")
            .description("Radius of the orbs, affects newly spawned particles")
            .onSettingChange(this)
            .defaultValue(0.1d)
            .min(0.01)
            .max(5d)
            .roundingPlace(2)
            .shouldRender(() -> !randomRadius.value && type.getOption() == EffectType.ORBS)
            .build()
    );
    // Text
    StringListSetting texts = sgGeneral.add(new StringListSetting.Builder()
            .name("Text")
            .description("Text which will be displayed around the player hit")
            .inputMode(InputBox.InputMode.ALL)
            .characterLimit(100)
            .defaultBoxes(10)
            .defaultValue(new String[]{"Kachow", "EZ", "Hit", "Pow!", "BAM!", "BOOM!", "Zap", "GG", "Owned", "LOL"})
            .shouldRender(() -> type.getOption() == EffectType.TEXT)
            .build()
    );
    BooleanSetting comicalText = sgGeneral.add(new BooleanSetting.Builder()
            .name("Comical Test")
            .description("The text will comical and funny. Random Colors works best with this")
            .onSettingChange(this)
            .defaultValue(true)
            .shouldRender(() -> type.getOption() == EffectType.TEXT)
            .build()
    );
    DoubleSetting scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Scale of the text")
            .onSettingChange(this)
            .value(0.5d)
            .defaultValue(0.5d)
            .min(0.01)
            .max(5d)
            .roundingPlace(1)
            .shouldRender(() -> type.getOption() == EffectType.TEXT || type.getOption() == EffectType.EZ)
            .build()
    );
    //Particle Effect
    ParticleListSetting particle_effect = sgGeneral.add(new ParticleListSetting.Builder()
            .name("Particle")
            .description("Particle to spawn")
            .particles(ParticleTypes.DAMAGE_INDICATOR)
            .iSettingChange(this)
            .shouldRender(() -> type.getOption() == EffectType.PARTICLE_EFFECT)
            .build()
    );

    public HitEffect() {
        super("HitEffect", "Displays particles when you hit", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

        particle_effect.setMaxSelectable(1);

        TextParticle.COMICAL = comicalText.value;
        OrbParticle.COOLER_PHYSICS = coolerPhysics.value;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (HeliosClient.MC.world == null) return;

        particles.removeIf(hitEffectParticle -> hitEffectParticle.isDiscarded);

        for (HitEffectParticle particle : particles) {
            if(particle == null) continue;
            particle.tick();
        }
    }

    @SubscribeEvent
    public void on3dRender(Render3DEvent event) {

        RenderSystem.enableDepthTest();
        for (HitEffectParticle particle : particles) {
            particle.render(event.getMatrices(), color.value);
        }
        RenderSystem.disableDepthTest();
    }

    @SubscribeEvent
    public void onHit(PlayerAttackEntityEvent event) {
        if(mc.player == null ||  mc.player.isDead()) return;

        Random random = Random.create();
        Entity target = event.getTarget();
        for (int i = 0; i < (int) particleAmount.value; i++) {
            // Use a random angle for the spread of particles
            double angle = 2 * Math.PI * random.nextDouble();
            // Use the square root of the random number for a more even distribution
            double spread = Math.sqrt(random.nextDouble()) * (i * 5.2f) / (particleAmount.value);
            double offsetX = spread * Math.cos(angle);
            double offsetY = ((random.nextDouble() - 0.5) * 2) + target.getHeight();
            double offsetZ = spread * Math.sin(angle);
            Vec3d position = target.getPos().add(offsetX, offsetY, offsetZ);

            switch ((EffectType) type.getOption()) {
                case ORBS -> {
                    BlockPos blockPos = BlockUtils.toBlockPos(position);
                    BlockState blockState = mc.world.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        Vec3d velocity = new Vec3d(-target.getVelocity().x / 10, 0, -target.getVelocity().z / 10);
                        particles.add(new OrbParticle(position, velocity, randomRadius.value ? random.nextFloat() + 0.2f : (float) orbsRadius.value, (float) gravityAmount.value, (float) time_in_seconds.value, randomColor.value));
                    }
                }
                case TEXT -> {
                    TextParticle.COMICAL = comicalText.value;
                    particles.add(new TextParticle(getRandomText(), position, (float) scale.value, (float) time_in_seconds.value,randomColor.value));
                }
                case EZ -> {
                    TextParticle.COMICAL = comicalText.value;
                    particles.add(new TextParticle("EZ!", position, (float) scale.value, (float) time_in_seconds.value,randomColor.value));
                }
                case CRITS -> {
                    particles.clear();
                    mc.player.addCritParticles(target);
                }
                case PARTICLE_EFFECT -> {
                    particles.clear();
                    ParticleType<?> particle;
                    if (!particle_effect.getSelectedEntries().isEmpty()) {
                        particle = particle_effect.getSelectedEntries().get(0);
                        if (particle != null)
                            mc.world.addParticle((ParticleEffect) particle, position.x, position.y, position.z, 0, 0, 0);
                    }
                }
            }
        }
    }

    public String getRandomText() {
        int randomIndex = random.nextInt(texts.value.length);
        return texts.value[randomIndex];
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        TextParticle.COMICAL = comicalText.value;
        OrbParticle.COOLER_PHYSICS = coolerPhysics.value;
    }

    public enum EffectType {
        ORBS,
        TEXT,
        EZ,
        PARTICLE_EFFECT,
        CRITS

    }
}
