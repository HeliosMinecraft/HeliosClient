package dev.heliosclient.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
            out = (int) value;
        }
        return out;
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

    public static int calculateLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), dp[i - 1][j] + 1), dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static double jaroWinklerSimilarity(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0;
        }

        int m = 0; // match count
        int t = 0; // transposition count

        int s1Len = s1.length();
        int s2Len = s2.length();

        int range = Math.max(0, Math.max(s1Len, s2Len) / 2 - 1);

        // match and transposition
        boolean[] s1Matches = new boolean[s1Len];
        boolean[] s2Matches = new boolean[s2Len];
        for (int i = 0; i < s1Len; i++) {
            int start = Math.max(0, i - range);
            int end = Math.min(i + range + 1, s2Len);

            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                m++;
                break;
            }
        }
        if (m == 0) return 0;
        int k = 0;
        for (int i = 0; i < s1Len; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) t++;
            k++;
        }

        double jaro = ((m / (double) s1Len) + (m / (double) s2Len) + ((m - t / 2.0) / m)) / 3.0;
// jaro winkler
        int l = 0; // length of common prefix
        int p = 0; // scaling factor
        while (l < s1.length() && l < s2.length() && s1.charAt(l) == s2.charAt(l) && l < 4) l++;
        return jaro + l * p * (1 - jaro);
    }


    public static HashMap<String, Object> filterAndSortMap(HashMap<String, Object> map, String input) {
        List<Map.Entry<String, Object>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparingInt((Map.Entry<String, Object> entry) -> calculateLevenshteinDistance(entry.getKey(), input)));

        HashMap<String, Object> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static List<String> filterAndSortList(List<String> list, String input) {
        List<String> filteredList = new ArrayList<>();
        for (String entry : list) {
            if (entry.equals(input)) {
                filteredList.add(entry);
            }
        }

        filteredList.sort(Comparator.comparingInt((String entry) -> calculateLevenshteinDistance(entry, input)));

        return filteredList;
    }
}
