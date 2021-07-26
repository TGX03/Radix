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
                    for (int x = 0; x < first[0].size(); x++) {
                        long element = first[0].get(x);
                        long list = element & position;
                        if (list == 0) {
                            second[0].add(element);
                        } else {
                            second[1].add(element);
                        }
                    }
                    for (int x = 0; x < first[1].size(); x++) {
                        long element = first[1].get(x);
                        long list = element & position;
                        if (list == 0) {
                            second[0].add(element);
                        } else {
                            second[1].add(element);
                        }
                    }
                    first[0].clear();
                    first[1].clear();
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    for (int x = 0; x < second[0].size(); x++) {
                        long element = second[0].get(x);
                        long list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    for (int x = 0; x < second[1].size(); x++) {
                        long element = second[1].get(x);
                        long list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    second[0].clear();
                    second[1].clear();
                    current = run.FIRSTSOURCE;
                }
            }
            position = position<<1;
        }
        switch (current) {

            case FIRSTSOURCE -> {
                long[] result = new long[first[0].size() + first[1].size()];
                long[] firstArr = first[0].toArray();
                System.arraycopy(firstArr, 0, result, 0, firstArr.length);
                long[] secondArr = first[1].toArray();
                System.arraycopy(secondArr, 0, result, firstArr.length, secondArr.length);
                return result;
            }

            case SECONDSOURCE -> {
                long[] result = new long[second[0].size() + second[1].size()];
                long[] firstArr = second[0].toArray();
                System.arraycopy(firstArr, 0, result, 0, firstArr.length);
                long[] secondArr = second[1].toArray();
                System.arraycopy(secondArr, 0, result, firstArr.length, secondArr.length);
                return result;
            }

            default -> throw new Error("Don't know what happened");
        }
    }

}
