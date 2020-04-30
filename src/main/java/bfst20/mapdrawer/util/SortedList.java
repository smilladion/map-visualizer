package bfst20.mapdrawer.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.LongSupplier;

/**
 * This is a special type of ArrayList made to replace an ordinary HashMap,
 * which takes up too much space when containing many elements.
 * It sorts its elements based on their ID, and so when you use it to fetch an
 * element from its ID, it does a binary search in order to find it.
 * Not quicker than a HashMap (with O(1) lookup), but a worthy trade-off for memory.
 */
public class SortedList<T extends LongSupplier> extends ArrayList<T> {

    private boolean isSorted;
    
    /** Returns the element associated with the given ID. */
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
