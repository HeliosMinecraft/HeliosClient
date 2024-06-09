package dev.heliosclient.system;

import java.util.Random;

public class UniqueID {
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
    private static final Random RANDOM = new Random();
    private static int LENGTH = 5;
    public String uniqueID;

    public UniqueID(String id) {
        this.uniqueID = id;
    }

    public static UniqueID setLengthAndGet(int length) {
        LENGTH = length;
        UniqueID id = generate();
        LENGTH = 5;
        return id;
    }

    public static UniqueID generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return new UniqueID(sb.toString());
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
