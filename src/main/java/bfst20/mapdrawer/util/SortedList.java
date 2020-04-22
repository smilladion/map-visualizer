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
        int low = 0;
        int high = size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            T midElement = get(mid);
            long midId = midElement.getAsLong();

            if (midId < id) {
                low = mid + 1;
            } else if (midId > id) {
                high = mid - 1;
            } else {
                return midElement;
            }
        }

        return null;
    }
}
