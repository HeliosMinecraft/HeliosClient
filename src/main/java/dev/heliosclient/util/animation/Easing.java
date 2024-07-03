package dev.heliosclient.util.animation;

public class Easing {
    public static float ease(EasingType type, float t) {
        return switch (type) {
            case LINEAR_IN -> linearIn(t);
            case LINEAR_OUT -> linearOut(t);
            case LINEAR_IN_OUT -> linearInOut(t);
            case QUADRATIC_IN -> quadraticIn(t);
            case QUADRATIC_OUT -> quadraticOut(t);
            case QUADRATIC_IN_OUT -> quadraticInOut(t);
            case CUBIC_IN -> cubicIn(t);
            case CUBIC_OUT -> cubicOut(t);
            case CUBIC_IN_OUT -> cubicInOut(t);
            case LINEAR_SIGMOID -> linearSigmoid(t);
            default -> throw new IllegalArgumentException("Invalid easing type: " + type);
        };
    }

    public static float linearIn(float t) {
        return t;
    }

    public static float linearOut(float t) {
        return t;
    }

    public static float linearInOut(float t) {
        return t;
    }

    public static float quadraticIn(float t) {
        return t * t;
    }

    public static float quadraticOut(float t) {
        return -t * (t - 2);
    }

    public static float quadraticInOut(float t) {
        if (t < 0.5f) {
            return 2 * t * t;
        } else {
            return -2 * t * t + 4 * t - 1;
        }
    }

    public static float linearSigmoid(float t) {
        return (float) (1 / (1 + Math.exp(-10 * (t - 0.5))));
    }

    public static float cubicIn(float t) {
        return t * t * t;
    }

    public static float cubicOut(float t) {
        float f = t - 1;
        return f * f * f + 1;
    }

    public static float cubicInOut(float t) {
        if (t < 0.5f) {
            return 4 * t * t * t;
        } else {
            float f = -2 * t + 2;
            return 1 - (f * f * f) / 2;
        }
    }

    public static float easeInOutSine(float t) {
        return (1 - (float) Math.cos(t * Math.PI)) / 2;
    }
}
