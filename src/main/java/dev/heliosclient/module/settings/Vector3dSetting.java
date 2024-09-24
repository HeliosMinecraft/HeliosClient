package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.system.mixininterface.IVec3d;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.misc.MapReader;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;

import static dev.heliosclient.util.fontutils.FontRenderers.Small_fxfontRenderer;

public class Vector3dSetting extends Setting<Vec3d> implements ISettingChange {
    private final DoubleSetting xSet;
    private final DoubleSetting ySet;
    private final DoubleSetting zSet;

    public Vector3dSetting(String name, String description, BooleanSupplier shouldRender, Vec3d defaultValue, double min, double max, int roundingPlace, ISettingChange settingChange, String xName, String yName, String zName) {
        super(shouldRender, defaultValue);
        value = defaultValue;
        this.name = name;
        this.description = description;
        this.iSettingChange = settingChange;
        xSet = new DoubleSetting.Builder()
                .name(xName)
                .min(min)
                .max(max)
                .defaultValue(defaultValue.x)
                .value(defaultValue.x)
                .onSettingChange(this)
                .roundingPlace(roundingPlace)
                .build();

        ySet = new DoubleSetting.Builder()
                .name(yName)
                .min(min)
                .max(max)
                .onSettingChange(this)
                .defaultValue(defaultValue.y)
                .value(defaultValue.y)
                .roundingPlace(roundingPlace)
                .build();

        zSet = new DoubleSetting.Builder()
                .name(zName)
                .min(min)
                .max(max)
                .roundingPlace(roundingPlace)
                .onSettingChange(this)
                .defaultValue(defaultValue.z)
                .value(defaultValue.z)
                .build();

        this.height = xSet.height + ySet.height + zSet.height + 10;
        this.heightCompact = xSet.heightCompact + ySet.heightCompact + zSet.heightCompact + 5;

        //Result of bad coding.... ugghhhh. I am gonna replace this with scale based animation later.
        //Todo
        xSet.setAnimationDone(true);
        xSet.setAnimationProgress(1.0f);
        ySet.setAnimationDone(true);
        ySet.setAnimationProgress(1.0f);
        zSet.setAnimationDone(true);
        zSet.setAnimationProgress(1.0f);
    }

    public Vector3dSetting(String name, String description, BooleanSupplier shouldRender, Vec3d defaultValue, double min, double max, int roundingPlace, ISettingChange settingChange) {
        this(name, description, shouldRender, defaultValue, min, max, roundingPlace, settingChange, "X", "Y", "Z");
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;

        this.xSet.render(drawContext, this.x, this.y + 10, mouseX, mouseY, textRenderer);
        this.ySet.render(drawContext, this.x, this.y + this.xSet.height + 10, mouseX, mouseY, textRenderer);
        this.zSet.render(drawContext, this.x, this.y + this.xSet.height + this.ySet.height + 10, mouseX, mouseY, textRenderer);
        ((IVec3d) value).heliosClient$set(this.xSet.value, this.ySet.value, this.zSet.value);
        Renderer2D.drawFixedString(drawContext.getMatrices(), this.name, this.x + width / 2.0f - Renderer2D.getFxStringWidth(name) / 2.0f, this.y + 2, Color.WHITE.getRGB());

        alsoRender(new RenderContext(drawContext, x, y, mouseX, mouseY, textRenderer));
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;

        this.xSet.renderCompact(drawContext, this.x, this.y + 10, mouseX, mouseY, textRenderer);
        this.ySet.renderCompact(drawContext, this.x, this.y + this.xSet.heightCompact + 10, mouseX, mouseY, textRenderer);
        this.zSet.renderCompact(drawContext, this.x, this.y + this.xSet.heightCompact + this.ySet.heightCompact + 10, mouseX, mouseY, textRenderer);
        ((IVec3d) value).heliosClient$set(this.xSet.value, this.ySet.value, this.zSet.value);
        String trimmedName = Small_fxfontRenderer.trimToWidth(name, getWidthCompact());

        Small_fxfontRenderer.drawString(drawContext.getMatrices(), trimmedName, this.x + getWidthCompact() / 2.0f - Renderer2D.getCustomStringWidth(name, Small_fxfontRenderer) / 2.0f, this.y + 3, ColorManager.INSTANCE.defaultTextColor());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        this.xSet.mouseClicked(mouseX, mouseY, button);
        this.ySet.mouseClicked(mouseX, mouseY, button);
        this.zSet.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        this.xSet.mouseReleased(mouseX, mouseY, button);
        this.ySet.mouseReleased(mouseX, mouseY, button);
        this.zSet.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        this.xSet.keyPressed(keyCode, scanCode, modifiers);
        this.ySet.keyPressed(keyCode, scanCode, modifiers);
        this.zSet.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromFile(MapReader map) {
        if (!map.has(getSaveName())) {
            this.xSet.setValue(defaultValue.x);
            this.ySet.setValue(defaultValue.y);
            this.zSet.setValue(defaultValue.z);
            value = defaultValue;
            HeliosClient.LOGGER.error("{} has no Vec3d values... defaulting", this.name);
        } else {
            List<Double> vec3 = (List<Double>) map.getAs(getSaveName(),List.class);
            this.xSet.setValue(vec3.get(0));
            this.ySet.setValue(vec3.get(1));
            this.zSet.setValue(vec3.get(2));
            value = new Vec3d(xSet.value, ySet.value, zSet.value);
        }
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        objectList.add(xSet.get());
        objectList.add(ySet.get());
        objectList.add(zSet.get());

        return objectList;
    }

    public Vec3d getVec() {
        return value;
    }

    public Vec3d getNewVec() {
        return new Vec3d(xSet.value, ySet.value, zSet.value);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        if (iSettingChange != null)
            iSettingChange.onSettingChange(this);
    }

    public double getVecX() {
        return this.xSet.value;
    }

    public double getVecY() {
        return this.ySet.value;
    }

    public double getVecZ() {
        return this.zSet.value;
    }

    @Override
    public Vec3d get() {
        return value;
    }

    @Override
    public void setValue(Vec3d value) {
        this.value = value;
    }

    public static class Builder extends SettingBuilder<Builder, Vec3d, Vector3dSetting> {
        ISettingChange ISettingChange;
        double min, max;
        int roundingPlace;
        String x = "X", y = "Y", z = "Z";

        public Builder() {
            super(Vec3d.ZERO);
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        public Builder roundingPlace(int roundingPlace) {
            this.roundingPlace = roundingPlace;
            return this;
        }

        public Builder defaultValue(double x, double y, double z) {
            this.defaultValue = new Vec3d(x, y, z);
            return this;
        }

        public Builder value(double x, double y, double z) {
            this.value = new Vec3d(x, y, z);
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder xName(String xName) {
            this.x = xName;
            return this;
        }

        public Builder yName(String yName) {
            this.y = yName;
            return this;
        }

        public Builder zName(String zName) {
            this.z = zName;
            return this;
        }

        @Override
        public Vector3dSetting build() {
            if (defaultValue == null) {
                defaultValue = value;
            }
            return new Vector3dSetting(name, description, shouldRender, defaultValue, min, max, roundingPlace, ISettingChange, x, y, z);
        }
    }
}
