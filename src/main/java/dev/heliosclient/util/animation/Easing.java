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
            case QUARTIC_IN -> quarticIn(t);
            case QUARTIC_OUT -> quarticOut(t);
            case QUARTIC_IN_OUT -> quarticInOut(t);
            case BOUNCE_IN -> bounceIn(t);
            case BOUNCE_OUT -> bounceOut(t);
            case BOUNCE_IN_OUT -> bounceInOut(t);
            case SINE_IN -> easeInSine(t);
            case SINE_OUT -> easeOutSine(t);
            case SINE_IN_OUT -> easeInOutSine(t);
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
            return -1 + (4 - 2 * t) * t;
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

    public static float quarticIn(float t) {
        return t * t * t * t;
    }

    public static float quarticOut(float t) {
        float f = t - 1;
        return f * f * f * (1 - t) + 1;
    }

    public static float quarticInOut(float t) {
        if (t < 0.5f) {
            return 8 * t * t * t * t;
        } else {
            float f = t - 1;
            return -8 * f * f * f * f + 1;
        }
    }

    public static float bounceIn(float t) {
        return 1 - bounceOut(1 - t);
    }

    public static float bounceOut(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            t -= 1.5 / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5 / d1) {
            t -= 2.25 / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625 / d1;
            return n1 * t * t + 0.984375f;
        }
    }

    public static float bounceOutWithOvershoot(float t) {
        float overshoot = 1.4f; // Adjust this value to control the overshoot

        float bounce = bounceOut(t);
        if (t < 0.5f) {
            return bounce * overshoot;
        } else {
            return bounce * (2 - overshoot);
        }
    }


    public static float bounceInOut(float t) {
        if (t < 0.5) {
            return bounceIn(t * 2) * 0.5f;
        } else {
            return bounceOut(t * 2 - 1) * 0.5f + 0.5f;
        }
    }

    public static float easeInSine(float t) {
        return (float) (1 - Math.cos((t * Math.PI) / 2));
    }

    public static float easeOutSine(float t) {
        return (float) Math.sin((t * Math.PI) / 2);
    }

    public static float easeInOutSine(float t) {
        return (1 - (float) Math.cos(t * Math.PI)) / 2;
    }
}
