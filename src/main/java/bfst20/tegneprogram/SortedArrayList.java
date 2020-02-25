package bfst20.tegneprogram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.LongSupplier;

public class SortedArrayList<T extends LongSupplier> {

    private ArrayList<T> list;
    private boolean isSorted;

    public SortedArrayList() {
        list = new ArrayList<>();
        isSorted = false;
    }

    public void add(T t) {
        list.add(t);
    }

    public T get(long id) {
        if (!isSorted) {
            list.sort(Comparator.comparing(T::getAsLong));
            isSorted = true;
        }
        return binarySearch(id);
    }

    public T binarySearch(long id) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            T midElement = list.get(mid);
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
