package dev.heliosclient.util.animation;

import java.util.ArrayList;
import java.util.List;

public class KeyframeAnimation {

    private final List<Keyframe> keyframes = new ArrayList<>();
    private float currentTime = 0;

    public void addKeyframe(float time, float value) {
        keyframes.add(new Keyframe(time, value));
    }

    public float update(float deltaTime) {
        currentTime += deltaTime;

        Keyframe previousKeyframe = null;
        for (Keyframe keyframe : keyframes) {
            if (keyframe.time > currentTime) {
                if (previousKeyframe == null) {
                    return keyframe.value;
                } else {
                    float t = (currentTime - previousKeyframe.time) / (keyframe.time - previousKeyframe.time);
                    return previousKeyframe.value + t * (keyframe.value - previousKeyframe.value);
                }
            }
            previousKeyframe = keyframe;
        }

        return previousKeyframe != null ? previousKeyframe.value : 0;
    }

    private static class Keyframe {
        float time;
        float value;

        Keyframe(float time, float value) {
            this.time = time;
            this.value = value;
        }
    }
}
