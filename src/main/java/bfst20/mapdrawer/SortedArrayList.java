package bfst20.mapdrawer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.LongSupplier;

public class SortedArrayList<T extends LongSupplier> {
    private ArrayList<T> SAList;
    private boolean isSorted;

    public SortedArrayList() {
        SAList = new ArrayList<>();
        isSorted = false;
    }

    public void add(T t) {
        SAList.add(t);
    }

    public T get(long id) {
        if (!isSorted) {
            SAList.sort(Comparator.comparing(T::getAsLong));
            isSorted = true;
        }
        return binarySearch(id);
    }

    public T binarySearch(long id) {
        int minimumValue = 0;
        int maximumValue = SAList.size() - 1;

        for (int i = 0; minimumValue <= maximumValue; i++) {
            int middleValue = (minimumValue + maximumValue) / 2;
            T middleIndex = SAList.get(minimumValue);
            long middleID = middleIndex.getAsLong();

            if (middleID < id) {
                minimumValue = middleValue + 1;
            } else if (middleID > id) {
                minimumValue = middleValue - 1;
            } else {
                return middleIndex;
            }

        }
        return null;
    }
}