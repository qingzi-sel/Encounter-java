package com.encounter.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {

    private RandomUtil() {}

    public static double nextDouble(double min, double max) {
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static <T> T pickRandom(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
