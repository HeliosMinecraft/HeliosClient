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
        if (t < 4/11.0) {
            return (121 * t * t)/16.0f;
        } else if (t < 8/11.0) {
            return (363/40.0f * t * t) - (99/10.0f * t) + 17/5.0f;
        } else if (t < 9/10.0) {
            return (4356/361.0f * t * t) - (35442/1805.0f * t) + 16061/1805.0f;
        } else {
            return (54/5.0f * t * t) - (513/25.0f * t) + 268/25.0f;
        }
    }

    public static float bounceInOut(float t) {
        if(t < 0.5) {
            return 0.5f * bounceIn(t*2);
        } else {
            return 0.5f * bounceOut(t * 2 - 1) + 0.5f;
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
