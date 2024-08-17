package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

public class PolygonMeshPatternRenderer {
    private static final Random random = new Random();
    public static PolygonMeshPatternRenderer INSTANCE = new PolygonMeshPatternRenderer();
    private int NUM_POINTS = 75;
    private Point[] points = new Point[NUM_POINTS];
    public float MAX_DISTANCE = 75.0f;
    public float radius = 2f;
    public GradientManager.Gradient maskGradient = GradientManager.getGradient("Linear2D");

    public PolygonMeshPatternRenderer() {
        create();
    }

    public void create() {
        int width = 800;
        int height = 600;

        if (MinecraftClient.getInstance().getWindow() != null) {
            width = MinecraftClient.getInstance().getWindow().getScaledWidth();
            height = MinecraftClient.getInstance().getWindow().getScaledHeight();
        }

        points = new Point[NUM_POINTS];

        for (int i = 0; i < NUM_POINTS; i++) {
            points[i] = new Point(random.nextFloat() * width, random.nextFloat() * height);
        }
    }

    public void setNumOfPoints(int NUM_POINTS) {
        this.NUM_POINTS = NUM_POINTS;
        this.create();
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        Renderer2D.drawToGradientMask(matrixStack.peek().getPositionMatrix(), maskGradient.getStartGradient(), maskGradient.getEndGradient(), maskGradient.getEndGradient(), maskGradient.getStartGradient(), 0, 0, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), () -> {
            for (Point point : points) {
                point.move(mouseX, mouseY);
                point.display(matrixStack);
            }

            //If distance to nearby points are less than max distance then join and render those lines.
            for (int i = 0; i < NUM_POINTS; i++) {
                for (int j = i + 1; j < NUM_POINTS; j++) {
                    float distance = points[i].distanceTo(points[j]);
                    if (distance < MAX_DISTANCE) {
                        drawLine(matrixStack, points[i], points[j]);
                    }
                }
            }
        });
    }

    private void drawLine(MatrixStack matrixStack, Point p1, Point p2) {
        Renderer2D.drawLine(matrixStack, p1.x, p1.y, p2.x, p2.y, 1f, -1);
    }

    private class Point {
        float x, y;
        float xSpeed, ySpeed;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
            this.xSpeed = random.nextFloat() * 0.25f - 0.25f;
            this.ySpeed = random.nextFloat() * 0.25f - 0.25f;
        }

        void move(int mouseX, int mouseY) {
            float dx = x - mouseX;
            float dy = y - mouseY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float force = Math.max(0, 100 - distance) / 5000;

            xSpeed += force * dx / distance;
            ySpeed += force * dy / distance;

            x += xSpeed;
            y += ySpeed;

            if (x < 0 || x > MinecraftClient.getInstance().getWindow().getScaledWidth()) xSpeed *= -1;
            if (y < 0 || y > MinecraftClient.getInstance().getWindow().getScaledHeight()) ySpeed *= -1;
        }

        void display(MatrixStack matrixStack) {
            Renderer2D.drawFilledCircle(matrixStack.peek().getPositionMatrix(), x, y, radius, -1);
        }

        //Distance formula
        float distanceTo(Point other) {
            return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
        }
    }
}
