package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;

public class Hitbox implements Listener {
    private float x, y, width, height;
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;
    private float prevX, prevY;

    public Hitbox(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        EventManager.register(this);
        this.left = x;
        this.top = y;
        this.right = x + width;
        this.bottom = y + height;
    }

    public boolean intersects(Hitbox other) {
        return this.x < other.x + other.width && this.x + this.width > other.x &&
                this.y < other.y + other.height && this.y + this.height > other.y;
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public void setPosition(float x, float y) {
        prevX = x;
        prevY = y;
        this.y = y;
        this.x = x;
    }

    public void setDimensions(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void set(float x, float y, float width, float height) {
        prevX = x;
        prevY = y;
        this.y = y;
        this.x = x;
        this.width = width;
        this.height = height;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        prevY = y;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        prevX = x;
        this.x = x;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getBottom() {
        return bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getTop() {
        return top;
    }

    public float getPrevX() {
        return prevX;
    }

    public float getPrevY() {
        return prevY;
    }
}
