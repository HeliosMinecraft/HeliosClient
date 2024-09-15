package dev.heliosclient.managers;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GradientManager {
    private static final LinkedHashMap<String, Gradient> gradients = new LinkedHashMap<>();

    public static void registerGradient(String name, Gradient gradientSupplier) {
        gradients.put(name, gradientSupplier);
    }

    public static Gradient getGradient(String name) {
        return gradients.get(name);
    }

    public static Set<String> getAllGradientsNames(){
        return gradients.keySet();
    }

    @Nullable
    public static String getKeyForGradient(Gradient gradient) {
        if(gradient == null){
            return null;
        }
        for (Map.Entry<String, Gradient> entry : gradients.entrySet()) {
            if (entry.getValue().equals(gradient)) {
                return entry.getKey();
            }
        }
        return null; // Return null if the gradient is not found
    }


    public static class Gradient {
        private final Supplier<Color> startGradient;
        private final Supplier<Color> endGradient;

        public static Gradient of(Supplier<Color> startGradient, Supplier<Color> endGradient){
            return new Gradient(startGradient, endGradient);
        }

        public Gradient(Supplier<Color> startGradient, Supplier<Color> endGradient) {
            this.startGradient = startGradient;
            this.endGradient = endGradient;
        }

        public Color getStartGradient() {
            return startGradient.get();
        }

        public Color getEndGradient() {
            return endGradient.get();
        }
    }

    public static class GradientBuilder {
        private String name;
        private Supplier<Color> startGradient;
        private Supplier<Color> endGradient;

        public GradientBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public GradientBuilder setStartGradient(Supplier<Color> startGradient) {
            this.startGradient = startGradient;
            return this;
        }

        public GradientBuilder setEndGradient(Supplier<Color> endGradient) {
            this.endGradient = endGradient;
            return this;
        }

        public void register() {
            if (name == null || startGradient == null || endGradient == null) {
                throw new IllegalStateException("Name and gradients must be set before registering");
            }
            GradientManager.registerGradient(name, GradientManager.Gradient.of(startGradient, endGradient));
        }
    }
}
