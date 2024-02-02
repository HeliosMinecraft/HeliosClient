package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;

public class HudBox implements Listener {
    private float x, y, width, height;
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float prevX, prevY;

    public HudBox(float x, float y, float width, float height) {
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

    public boolean intersects(HudBox other) {
        float thisLeft = Math.min(this.x, this.x + this.width);
        float thisRight = Math.max(this.x, this.x + this.width);
        float thisTop = Math.min(this.y, this.y + this.height);
        float thisBottom = Math.max(this.y, this.y + this.height);

        float otherLeft = Math.min(other.x, other.x + other.width);
        float otherRight = Math.max(other.x, other.x + other.width);
        float otherTop = Math.min(other.y, other.y + other.height);
        float otherBottom = Math.max(other.y, other.y + other.height);

        return thisLeft < otherRight && thisRight > otherLeft &&
                thisTop < otherBottom && thisBottom > otherTop;
    }


    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public void setPosition(float x, float y) {
        prevX = x;
        prevY = y;
        this.y = y;
        this.x = x;
        this.left = x;
        this.top = y;
        this.right = x + width;
        this.bottom = y + height;
    }

    public void setDimensions(float width, float height) {
        this.width = width;
        this.height = height;
        this.right = x + width;
        this.bottom = y + height;
    }

    public void set(float x, float y, float width, float height) {
        setPosition(x, y);
        setDimensions(width, height);
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        prevY = y;
        this.y = y;
        this.top = y;
        this.bottom = y + height;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        prevX = x;
        this.x = x;
        this.left = x;
        this.right = x + width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        this.bottom = y + height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        this.right = x + width;
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
