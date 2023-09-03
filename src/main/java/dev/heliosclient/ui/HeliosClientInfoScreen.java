package dev.heliosclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class HeliosClientInfoScreen extends Screen {
    public static HeliosClientInfoScreen INSTANCE = new HeliosClientInfoScreen();
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    protected HeliosClientInfoScreen() {
        super(Text.literal("Helios Client"));
    }

    protected void init() {
        this.layout.addHeader(new TextWidget(this.getTitle(), this.textRenderer));
        GridWidget gridWidget = this.layout.addBody(new GridWidget()).setSpacing(8);
        gridWidget.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(1);
        adder.add(ButtonWidget.builder(Text.literal("GitHub"), ConfirmLinkScreen.opening("https://github.com/HeliosClient/HeliosClient/", this, true)).width(210).build());
        adder.add(ButtonWidget.builder(Text.literal("Contributors"), ConfirmLinkScreen.opening("https://github.com/HeliosClient/HeliosClient/#contributors", this, true)).width(210).build());
        adder.add(ButtonWidget.builder(Text.literal("Credits"), ConfirmLinkScreen.opening("https://github.com/HeliosClient/HeliosClient/#contributors", this, true)).width(210).build());
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).build());
        this.layout.refreshPositions();
        this.layout.forEachChild(this::addDrawableChild);
    }

    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

}
