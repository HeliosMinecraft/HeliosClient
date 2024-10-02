package dev.heliosclient.ui.clickgui.script;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.util.inputbox.InputBox;
import dev.heliosclient.util.inputbox.MultiLineInputBox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;

public class TestScriptEditorScreen extends Screen {
    private MultiLineInputBox inputBox;
    private LuaFile file;

    protected TestScriptEditorScreen(LuaFile file) {
        super(Text.of("E"));
        this.file = file;
        try {
            this.inputBox = new MultiLineInputBox(HeliosClient.MC.getWindow().getScaledWidth() - 40, HeliosClient.MC.getWindow().getScaledHeight() - 40, file.getText(), Integer.MAX_VALUE, InputBox.InputMode.ALL);
            inputBox.displayLineNos = true;
            inputBox.doSyntaxHighLighting = true;
            inputBox.doAutoComplete = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if(inputBox.getLines().isEmpty()){
            try {
                inputBox.setText(file.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        inputBox.render(context,20,20,mouseX,mouseY,textRenderer);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        inputBox.mouseScrolled(verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
