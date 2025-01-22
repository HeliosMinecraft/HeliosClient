package dev.heliosclient.util.render.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

public class RoundedRectangle extends Shape<RoundedRectangle> {
    private float width, height, radius = 1.0f, outlineThickness, shadowOffsetX, shadowOffsetY;

    private int shadowColor, blurRadius;

    //Vertex colors
    private int v1Color, v2Color, v3Color, v4Color;

    //Boolean properties
    private boolean TL, BL, BR, TR, outline = false, shadow, blurred;

    private static final Rectangle outlineRectangle = new Rectangle();

    //0 to 2pi values of sin and cos
    private static final float[] sina = new float[36];
    private static final float[] cosa = new float[36];

    static {
        for (int i = 0; i < 36; i++) {
            float angle = (float) (i * Math.PI / 18);
            sina[i] = (float) Math.sin(angle);
            cosa[i] = (float) Math.cos(angle);
        }
    }

    public RoundedRectangle reset() {
        this.TL = true;
        this.TR = true;
        this.BL = true;
        this.BR = true;
        this.width = 0;
        this.height = 0;
        this.radius = 1.0f;
        this.outline = false;
        this.outlineThickness = 0.0f;
        this.shadow = false;
        this.blurred = false;
        this.shadowOffsetX = 0;
        this.shadowOffsetY = 0;
        this.blurRadius = 0;
        this.shadowColor = 0;
        return color(-1);
    }

    public RoundedRectangle corners(boolean TL, boolean BL, boolean BR, boolean TR) {
        this.BR = BR;
        this.BL = BL;
        this.TL = TL;
        this.TR = TR;
        return this;
    }

    public RoundedRectangle shadow(boolean shadow, boolean blurred, int blurRadius, float shadowOffsetX, float shadowOffsetY, int shadowOpacity, int shadowColor) {
        this.shadow = shadow;
        this.blurred = blurred;
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;
        this.blurRadius = blurRadius;
        this.shadowColor = ColorUtils.argbToRgb(shadowColor,shadowOpacity);
        return this;
    }

    public RoundedRectangle badShadow(boolean shadow, float shadowOffsetX, float shadowOffsetY, int shadowOpacity, int shadowColor) {
        return shadow(shadow,false,0, shadowOffsetX,shadowOffsetY,shadowOpacity,shadowColor);
    }

    public RoundedRectangle badShadow(boolean shadow, float shadowOffsetX, float shadowOffsetY, int shadowOpacity) {
        return badShadow(shadow,shadowOffsetX,shadowOffsetY,shadowOpacity,0);
    }

    public RoundedRectangle blurredShadow(boolean shadow, int blurRadius, float shadowOffsetX, float shadowOffsetY, int shadowColor) {
        return shadow(shadow,true,blurRadius, shadowOffsetX,shadowOffsetY,255,shadowColor);
    }

    public RoundedRectangle blurredShadow(boolean blurredShadow, int blurRadius, int shadowColor) {
        return blurredShadow(blurredShadow,blurRadius,0,0,shadowColor);
    }


    public RoundedRectangle outline(boolean outline, float outlineThickness) {
        this.outline = outline;
        this.outlineThickness = outlineThickness;
        return this;
    }

    public RoundedRectangle size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public RoundedRectangle radius(float radius) {
        // Ensure the radius is not larger than half the width or height
        if (width != 0 && height != 0) radius = Math.min(radius, Math.min(width, height) / 2);
        this.radius = radius;
        return this;
    }

    public RoundedRectangle color(Color color) {
        return color(color.getRGB());
    }

    public RoundedRectangle color(int color) {
        return color(color,color,color,color);
    }

    public RoundedRectangle color(Color startColor, Color endColor, Renderer2D.Direction direction) {
        return color(startColor.getRGB(), endColor.getRGB(), direction);
    }

    public RoundedRectangle color(int startColor, int endColor, Renderer2D.Direction direction) {
        int[] array = direction.getVertexColor(startColor,endColor);
        return color(array[0],array[1],array[2],array[3]);
    }

    public RoundedRectangle color(int tl, int tr, int br, int bl) {
        this.v1Color = tl;
        this.v2Color = tr;
        this.v3Color = br;
        this.v4Color = bl;
        return this;
    }

    public RoundedRectangle color(Color tl, Color tr, Color br, Color bl) {
        return color(tl.getRGB(), tr.getRGB(), br.getRGB(), bl.getRGB());
    }

    @Override
    public void draw(MatrixStack stack) {
        Matrix4f matrix = stack.peek().getPositionMatrix();

        if(shadow){
            if(blurred){
                if(outline){
                    Renderer2D.drawOutlineBlurredShadow(stack, x + shadowOffsetX, y + shadowOffsetY, width, height, blurRadius, new Color(shadowColor));
                } else {
                    Renderer2D.drawBlurredShadow(stack, x + shadowOffsetX, y + shadowOffsetY, width, height, blurRadius, new Color(shadowColor));
                }
            } else {
                float centerX = x + (width / 2.0f);
                float centerY = y + (height / 2.0f);

                drawRoundedRectangleInternal(matrix, centerX + shadowOffsetX, centerY + shadowOffsetY, width, height, radius, shadowColor, TL, TR, BL, BR);
            }
        }

        if(outline){
            drawRoundedRectangleOutline(stack, x, y, width, height, radius, outlineThickness, v1Color, v2Color, v3Color, v4Color, TL, TR, BL, BR);
        } else{
            drawRoundedGradientRectangle(matrix, x, y, width, height, radius, v1Color, v2Color, v3Color, v4Color, TL, TR, BL, BR);
        }
    }

    private void drawRoundedGradientRectangle(Matrix4f matrix, float x, float y, float width, float height, float radius,int color1, int color2, int color3, int color4, boolean TL, boolean TR, boolean BL, boolean BR) {
        //Draw a single rounded rectangle for same colors
        float centerX = x + (width / 2.0f);
        float centerY = y + (height / 2.0f);

        if(color1 == color2 && color2 == color3 && color3 == color4){
            drawRoundedRectangleInternal(matrix, centerX, centerY, width, height, radius, color1, TL, TR, BL, BR);
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);

        drawRoundedRectangleInternal(matrix, centerX, centerY, width, height, radius, color3, TL, TR, BL, BR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x, y + height, 0.0F).color(color4);
        buffer.vertex(matrix, x + width, y + height, 0.0F).color(color3);
        buffer.vertex(matrix, x + width, y, 0.0F).color(color2);
        buffer.vertex(matrix, x, y, 0.0F).color(color1);

        end();
    }

    public void drawRoundedRectangleInternal(Matrix4f ma, float cx, float cy, float dx, float dy, float r, int rgba, boolean TL, boolean TR, boolean BL, boolean BR) {
        float halfWidth = dx * 0.5f;
        float halfHeight = dy * 0.5f;

        int i;
        dx -= r + r;
        dy -= r + r;

        float x0 = cx + (0.5f * dx);
        float y0 = cy + (0.5f * dy);

        float x = 0, y;


        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        RenderSystem.disableCull();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buffer.vertex(ma, cx, cy, 0).color(rgba);

        if (BR) {
            for (i = 0; i < 9; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buffer.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            buffer.vertex(ma, cx + halfWidth, cy + halfHeight, 0).color(rgba);
        }


        x0 -= dx;
        if (BL) {
            for (i = 9; i < 18; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buffer.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            buffer.vertex(ma, cx - halfWidth, cy + halfHeight, 0).color(rgba);
        }

        y0 -= dy;
        if (TL) {
            for (i = 18; i < 27; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buffer.vertex(ma, x, y, 0).color(rgba);
            }

        } else {
            buffer.vertex(ma, cx - halfWidth, cy - halfHeight, 0).color(rgba);
        }

        x0 += dx;
        if (TR) {
            for (i = 27; i < 36; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buffer.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            x = cx + halfWidth;
            buffer.vertex(ma, x, cy - halfHeight, 0).color(rgba);
        }

        if (!BR) {
            buffer.vertex(ma, x, cy + halfHeight, 0).color(rgba);
        } else {
            buffer.vertex(ma, x, cy + (0.5f * dy), 0).color(rgba);
        }

        end();
        RenderSystem.depthMask(true);
    }

    //TODO: Can be optimised further
    public void drawRoundedRectangleOutline(MatrixStack matrixStack, float x, float y, float width, float height, float radius, float thickness,
                                            int colorTL, int colorTR, int colorBR, int colorBL,
                                            boolean roundTL, boolean roundTR, boolean roundBL, boolean roundBR) {

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        // Draw the rectangles for the outline with gradient
        drawGradient(matrixStack, x + radius, y, width - radius * 2, thickness, colorTL, colorTR, Renderer2D.Direction.LEFT_RIGHT); // Top rectangle
        drawGradient(matrixStack, x + radius, y + height - thickness, width - radius * 2, thickness, colorBR, colorBL, Renderer2D.Direction.RIGHT_LEFT); // Bottom rectangle

        drawGradient(matrixStack, x, y + radius, thickness, height - radius * 2, colorTL, colorBL, Renderer2D.Direction.BOTTOM_TOP); // Left rectangle
        drawGradient(matrixStack, x + width - thickness, y + radius, thickness, height - radius * 2, colorTR, colorBR, Renderer2D.Direction.TOP_BOTTOM); // Right rectangle

        // Draw the arcs for the rounded corners
        if (roundTL) {
            drawArc(matrix4f, x + radius, y + radius, radius, thickness, colorTL, 180, 270); // Top-left arc
        }
        if (roundTR) {
            drawArc(matrix4f, x + width - radius, y + radius, radius, thickness, colorTR, 90, 180); // Top-right arc
        }
        if (roundBR) {
            drawArc(matrix4f, x + width - radius, y + height - radius, radius, thickness, colorBR, 0, 90); // Bottom-right arc
        }
        if (roundBL) {
            drawArc(matrix4f, x + radius, y + height - radius, radius, thickness, colorBL, 270, 360); // Bottom-left arc
        }
    }

    private void drawGradient(MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, Renderer2D.Direction direction) {
        outlineRectangle.position(x, y).size(width, height).color(color1, color2, direction).outline(false, 0.0f).draw(matrix);
    }

    public void drawArc(Matrix4f matrix4f, float xCenter, float yCenter, float radius, float thickness, int color, int startAngle, int endAngle) {
        begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = startAngle; i <= endAngle; i++) {
            double innerX = xCenter + Math.sin(Math.toRadians(i)) * (radius - thickness);
            double innerY = yCenter + Math.cos(Math.toRadians(i)) * (radius - thickness);
            double outerX = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double outerY = yCenter + Math.cos(Math.toRadians(i)) * radius;

            buffer.vertex(matrix4f, (float) innerX, (float) innerY, 0).color(color);
            buffer.vertex(matrix4f, (float) outerX, (float) outerY, 0).color(color);
        }

        end();
    }
}
