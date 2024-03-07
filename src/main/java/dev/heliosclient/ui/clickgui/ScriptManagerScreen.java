package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ScriptManagerScreen extends Screen {
    public static ScriptManagerScreen INSTANCE = new ScriptManagerScreen();
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    private int numRows = 4; // Number of rows (adjust as needed)
    private int numColumns = 4; // Number of columns (adjust as needed)
    private int scrollOffset = 0, maxScroll;
    private int entryWidth, entryHeight;
    public NavBar navBar = new NavBar();
    static boolean isListening = false, showLocalScripts = true;
    static int scaledWidth;
    static int scaledHeight;
    String heading = "Script Manager";

    protected ScriptManagerScreen() {
        super(Text.of("ScriptSelector"));
    }

    @Override
    protected void init() {
        super.init();
       calculateTable();
       scrollOffset = Math.min(scrollOffset,maxScroll);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (mc.world == null) {
            super.renderBackgroundTexture(context);
        }
        calculateTable();
        scaledWidth = mc.getWindow().getScaledWidth();
        scaledHeight = mc.getWindow().getScaledHeight();

        //Draw background and heading bg
        Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), 100,32,scaledWidth - 200,scaledHeight - 80,3,ColorUtils.changeAlpha(Color.BLACK,125).getRGB());
        Renderer2D.drawRoundedRectangleWithShadow(context.getMatrices(), 100,32,scaledWidth - 200,18,3,4,Color.BLACK.getRGB(),true,true,false,false);

        //Reload all scripts button
        Renderer2D.drawRoundedRectangleWithShadow(context.getMatrices(), scaledWidth - 125,33,15,15,5,3,new Color(0xE2080009, true).brighter().getRGB());
        Renderer2D.drawOutlineRoundedBox(context.getMatrices().peek().getPositionMatrix(), scaledWidth - 125,33,15,15,5,0.7f,Color.WHITE.getRGB());
        FontRenderers.Mid_iconRenderer.drawString(context.getMatrices(),"\uEA1D",scaledWidth - 121.7f,36.5f,Color.WHITE.getRGB());


        //Draw heading
        FontRenderers.Mid_fxfontRenderer.drawString(context.getMatrices(),heading, 100 + (scaledWidth - 200)/2.0f - FontRenderers.Mid_fxfontRenderer.getStringWidth(heading)/2.0f,32 +  FontRenderers.Mid_fxfontRenderer.getStringHeight(heading)/2.0f ,Color.WHITE.getRGB());

        //Draw separating line between the scripts and buttons
        Renderer2D.drawRectangleWithShadow(context.getMatrices(), 175,50,1f,scaledHeight - 98,Color.BLACK.brighter().brighter().getRGB(),4);

        //Draw buttons
        drawButton(context,102,55,"Local Scripts","\uF15D",mouseX,mouseY);
        drawButton(context,102,73,"Cloud Scripts","\uEA37",mouseX,mouseY);


        if(showLocalScripts) {
            drawLocalScriptEntries(context);
        }else{
            displayCloudEntries(context);
        }

        if(hoveredOverRefreshAll(mouseX,mouseY)){
            Tooltip.tooltip.changeText("Refreshes/Reloads all scripts");
        }

        navBar.render(context,textRenderer,mouseX,mouseY);
        Tooltip.tooltip.render(context,mc.textRenderer,mouseX,mouseY);
    }

    public void drawLocalScriptEntries(DrawContext context){
        // Enable scissor for scrolling
        Renderer2D.enableScissor(175, 50,scaledWidth - 150 , scaledHeight - 100);

        // Render script entries
        for (int i = 0; i < LuaScriptManager.luaFiles.size(); i++) {
            int row = i / numColumns;
            int col = i % numColumns;
            int entryX = 185 + col * entryWidth;
            int entryY = 55 + row * entryHeight + row * 30 - scrollOffset;

            drawScript(context,entryX,entryY,LuaScriptManager.luaFiles.get(i));
        }
        Renderer2D.disableScissor();

        // Draw scrollbar
        int scrollbarX = scaledWidth - 99;
        int scrollbarY = 50;
        float scrollbarHeight = maxScroll > 0 ? (int) (Math.pow(entryHeight, 2)*2 / maxScroll) : 0;
        float scrollbarPosition = maxScroll > 0?  scrollbarY + (scaledHeight - 100 - scrollbarHeight) * scrollOffset / maxScroll : scrollbarY; // Position of the scrollbar depends on the current scroll offset
        Renderer2D.drawRectangleWithShadow(context.getMatrices(), scrollbarX, scrollbarPosition, 1.5f, scrollbarHeight, Color.BLACK.brighter().brighter().getRGB(),2);
    }
    public static void displayCloudEntries(DrawContext context){
        FontRenderers.Large_fxfontRenderer.drawString(context.getMatrices(),"Work in progress",175 + (scaledWidth - 150 - 175)/2.0f - FontRenderers.Large_fxfontRenderer.getStringWidth("Work in progress")/2.0f ,55,Color.YELLOW.getRGB());
    }
    public static void drawButton(DrawContext drawContext, int x, int y, String text, String icon, int mouseX, int mouseY){
        if(isMouseOver(mouseX,mouseY,x,y,70,13)) {
            y = y-1;
        }
            Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x + 1, y, 70, 13, 4, 4, Color.BLACK.darker().darker().getRGB());
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), text, x + 1 + (70 / 2.0f) - FontRenderers.Small_fxfontRenderer.getStringWidth(text) / 2.0f, y + FontRenderers.Small_fxfontRenderer.getStringHeight(text) / 2.0f - 0.5f, Color.WHITE.getRGB());
            FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), icon, x + 1 + FontRenderers.Small_iconRenderer.getStringWidth(icon), y + FontRenderers.Small_iconRenderer.getStringHeight(text) / 2.0f  - 0.5f , Color.WHITE.getRGB());

    }

    public void calculateTable(){
        // Calculate entry size based on available space and GUI scale
        entryWidth =(scaledWidth - 275) / numColumns;
        entryHeight = (scaledHeight - 100) / numRows;

        numColumns = ((scaledWidth - 200) - 185)/43;
        numRows = ((scaledHeight - 100) - 50)/40;

        if(numColumns <= 0){
            numColumns = 2;
        }

        if(numRows <= 0){
            numRows = 2;
        }

        // Calculate the maximum scroll offset based on the number of scripts and the size of the entries
        maxScroll = LuaScriptManager.luaFiles.size() > 1? Math.max(0, (3*LuaScriptManager.luaFiles.size() * entryHeight)/4 - (scaledHeight - 200)) : 0;
    }
    public static boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height){
       return mouseX>= x && mouseX<=x + width && mouseY>= y && mouseY <= y + height;
    }

    public static void drawScript(DrawContext drawContext, int x, int y, LuaFile file){
        //Icon
        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(),x,y,60,70,4,4, ColorUtils.changeAlpha(ColorManager.INSTANCE.ClickGuiPrimary().darker().darker().darker(), 169).getRGB());
        Renderer2D.drawOutlineGradientRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x,y,60,70,4,0.7f, ColorManager.INSTANCE.getPrimaryGradientStart(),  ColorManager.INSTANCE.getPrimaryGradientEnd(),ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientStart());
        FontRenderers.Ultra_Large_iconRenderer.drawString(drawContext.getMatrices(),"\uF0F6",x + 19,y + 12,Color.WHITE.getRGB());

        Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientEnd(),ColorManager.INSTANCE.getPrimaryGradientEnd(),ColorManager.INSTANCE.getPrimaryGradientStart(),x, y + 58,60,12,4,false,false,true,true);

        //Name
        FontRenderers.Mid_fxfontRenderer.drawString(drawContext.getMatrices(),file.getScriptName(),x + 30 - FontRenderers.Mid_fxfontRenderer.getStringWidth(file.getScriptName())/2.0f,y + 43f,Color.WHITE.getRGB());

        //Bind
        String bindKey = KeycodeToString.translateShort(file.bindKey).toUpperCase();
        if(file.isListeningForBind){
            bindKey = "Set";
        }
        Renderer2D.drawRoundedRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(),x + 4, y + 60,FontRenderers.Small_fxfontRenderer.getStringWidth(bindKey) + 3,8,2,Color.BLACK.getRGB(),100,1,1);
        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(),bindKey,x + 4.9f,y + 60.5f, Color.WHITE.getRGB());

        //File state (loaded/unloaded)
        drawOnOffButton(drawContext,x + 26,y + 60,file.isLoaded());
        //Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(),xModified + 10, y + 60,FontRenderers.Small_fxfontRenderer.getStringWidth(file.isLoaded? "DISABLE":"ENABLE") + 3,7,2,Color.BLACK.getRGB());
        //FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(),file.isLoaded? "DISABLE":"ENABLE",xModified + 10,y + 60.5f, file.isLoaded? Color.RED.getRGB() : Color.GREEN.getRGB());

        //Refresh / Reload
        Renderer2D.drawRoundedRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(),x + 45f, y + 59.5f,FontRenderers.Small_iconRenderer.getStringWidth("\uEA75") + 2,8,2,Color.BLACK.getRGB(),100,1,1);
        FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(),"\uEA1D",x + 46f,y + 60.5f, Color.WHITE.getRGB());
    }

    public boolean hoveredOverRefreshAll(double mouseX, double mouseY){
        return isMouseOver(mouseX,mouseY, scaledWidth - 125, 33,15, 15);
    }
    public boolean hoveredOverLocalScripts(double mouseX, double mouseY){
        return isMouseOver(mouseX,mouseY,102, 55,70, 13);
    }
    public boolean hoveredOverCloudScripts(double mouseX, double mouseY){
        return isMouseOver(mouseX,mouseY,102, 73,70, 13);
    }
    public boolean hoveredOverFileState(double mouseX, double mouseY, int entryX, int entryY){
        return isMouseOver(mouseX,mouseY,entryX + 26, entryY + 60,16, 8);
    }
    public boolean hoveredOverRefreshFile(double mouseX, double mouseY, int entryX, int entryY){
        return isMouseOver(mouseX,mouseY,entryX + 45f, entryY + 59.5f,4 +FontRenderers.Small_iconRenderer.getStringWidth("\uEA75"), 8);
    }
    public boolean hoveredOverBind(double mouseX, double mouseY,LuaFile file, int entryX, int entryY){
        String bindKeyName = KeycodeToString.translateShort(file.bindKey).toUpperCase();
        return isMouseOver(mouseX,mouseY,entryX + 5f, entryY + 60f,FontRenderers.Small_fxfontRenderer.getStringWidth(bindKeyName) + 3, 7.4f);
    }
    public static void drawOnOffButton(DrawContext context, float x, float y, boolean state){
        Renderer2D.drawRoundedRectangleWithShadowBadWay(context.getMatrices().peek().getPositionMatrix(), x,y,15,8,4,state? Color.GREEN.getRGB() : Color.RED.getRGB(),100,1,1);

        float filledX = state? x + 12 :  x + 3;
        Renderer2D.drawFilledCircle(context.getMatrices().peek().getPositionMatrix(), filledX,y + 4,4,Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = MathHelper.clamp(scrollOffset - (int)verticalAmount * 10, 0, maxScroll); // Clamp the scroll offset between 0 and maxScroll
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        navBar.mouseClicked((int) mouseX,(int) mouseY,button);

        // Check mouse clicked on any of the entries
        for (int i = 0; i < LuaScriptManager.luaFiles.size(); i++) {
            int row = i / numColumns;
            int col = i % numColumns;
            int entryX = 185 + col * entryWidth;
            int entryY = 55 + row * entryHeight + row * 30  - scrollOffset;
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            if(hoveredOverFileState(mouseX,mouseY,entryX,entryY)){
                SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
                if(file.isLoaded()){
                    LuaScriptManager.INSTANCE.closeScript(file);
                }else{
                    LuaScriptManager.INSTANCE.loadScript(file);
                }
            }
            if(hoveredOverRefreshFile(mouseX,mouseY,entryX,entryY)){
                LuaScriptManager.reloadScript(file);
            }
            if(hoveredOverBind(mouseX,mouseY,file,entryX,entryY)){
                file.setListening(true);
                isListening = true;
            }
        }
        if(hoveredOverLocalScripts(mouseX,mouseY)){
            showLocalScripts = true;
            SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
        }
        if(hoveredOverCloudScripts(mouseX,mouseY)){
            showLocalScripts = false;
            SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
        }
        if(hoveredOverRefreshAll(mouseX,mouseY)){
           LuaScriptManager.getScripts();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        // I don't want to add check for luaFiles is null so using old loop and not enhanced.
        for(int i = 0; i < LuaScriptManager.luaFiles.size(); i++){
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            if(file.isListeningForBind && keyCode != GLFW.GLFW_KEY_ESCAPE){
                file.setBindKey(keyCode);
                file.setListening(false);
                isListening = false;
            }
            if(file.isListeningForBind && keyCode == GLFW.GLFW_KEY_ESCAPE){
                file.setBindKey(-1);
                file.setListening(false);
                isListening = false;
            }
        }
        if(!isListening && keyCode == GLFW.GLFW_KEY_ESCAPE){
            this.close();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}