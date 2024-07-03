package dev.heliosclient.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MathUtils {

    /**
     * Rounding to given number of places.
     *
     * @param value  Target double.
     * @param places Number of rounding places.
     * @return Rounded double.
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int d2iSafe(Object value) {
        int out;
        try {
            out = (int) Math.floor((double) value);
        } catch (Exception e) {
            try {
                out = (int) value;
            } catch (Exception exception) {
                out = Math.toIntExact((long) value);
            }
        }
        return out;
    }
    public static int o2iSafe(Object obj) {
        int result;
        try {
            if (obj instanceof Double db) {
                result = ((Double) obj).intValue();
            } else if (obj instanceof Float) {
                result = ((Float) obj).intValue();
            } else if (obj instanceof Integer) {
                result = (Integer) obj;
            } else {
                throw new IllegalArgumentException("Object is not an instance of Double, Float or Integer");
            }
        } catch (ClassCastException e) {
            // This should never happen if we've checked the instance correctly
            throw new RuntimeException("Unexpected ClassCastException", e);
        }
        return result;
    }

    public static double length2D(Vec3d vec3d) {
        return MathHelper.sqrt((float) (vec3d.x * vec3d.x + vec3d.z * vec3d.z));
    }

    /**
     * Not a math utility
     *
     * @param list
     * @return
     */
    public static String[] listToArray(List<String> list) {
        // Create a new array with the same size as the list
        String[] array = new String[list.size()];

        // Copy each element from the list to the array
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        // Return the array
        return array;
    }

    public static List<String> filterAndSortList(List<String> list, String input) {
        List<String> filteredList = new ArrayList<>();
        for (String entry : list) {
            if (entry.equals(input)) {
                filteredList.add(entry);
            }
        }

        filteredList.sort(Comparator.comparingInt((String entry) -> StringUtils.getLevenshteinDistance(entry, input)));

        return filteredList;
    }
}
