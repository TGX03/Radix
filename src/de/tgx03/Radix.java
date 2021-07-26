package de.tgx03;

public final class Radix {

    private enum run {FIRST, FIRSTSOURCE, SECONDSOURCE}

    public static long[] sort(long[] source, int numberLength) {
        LongList[] first = new LongList[2];
        LongList[] second = new LongList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new LongList();
            second[i] = new LongList();
        }
        run current = run.FIRST;
        long position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (long element : source) {
                        long list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = position<<1;
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first);
            }

            case SECONDSOURCE -> {
                return mergeLists(second);
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    private static LongList[] sortPosition(LongList[] source, long position) {
        LongList[] result = new LongList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new LongList(size);
        result[1] = new LongList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                long element = source[i].get(x);
                long list = element & position;
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    private static long[] mergeLists(LongList[] source) {
        long[] result = new long[source[0].size() + source[1].size()];
        long[] first = source[0].toArray();
        System.arraycopy(first, 0, result, 0, first.length);
        long[] second = source[1].toArray();
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
