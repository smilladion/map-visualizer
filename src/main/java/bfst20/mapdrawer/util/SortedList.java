package bfst20.mapdrawer.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.LongSupplier;

public class SortedList<T extends LongSupplier> extends ArrayList<T> {

    private boolean isSorted;

    public T get(long id) {
        if (!isSorted) {
            sort(Comparator.comparing(T::getAsLong));
            isSorted = true;
        }
        
        return binarySearch(id);
    }

    private T binarySearch(long id) {
        int minimumValue = 0;
        int maximumValue = size() - 1;

        for (int i = 0; minimumValue <= maximumValue; i++) {
            int middleValue = (minimumValue + maximumValue) / 2;
            T middleIndex = get(minimumValue);
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
