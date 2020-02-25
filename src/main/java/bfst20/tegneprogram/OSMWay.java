package bfst20.tegneprogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.LongSupplier;

public class OSMWay extends ArrayList<OSMNode> implements LongSupplier {
    private static final long serialVersionUID = 406066911888757425L;

    long id;

    public OSMWay() {

    }

    public OSMWay(long id) {
        this.id = id;
    }

	public OSMNode first() {
		return get(0);
	}

	public OSMNode last() {
		return get(size()-1);
	}

	public static OSMWay merge(OSMWay before, OSMWay after) {
        if (before == null) return after;
        if (after == null) return before;
        var res = new OSMWay();
        if (before.first() == after.first()) {
            res.addAll(before);
            Collections.reverse(res);
            res.remove(res.size() - 1);
            res.addAll(after);
        } else if (before.first() == after.last()) {
            res.addAll(after);
            res.remove(res.size() - 1);
            res.addAll(before);
        } else if (before.last() == after.first()) {
            res.addAll(before);
            res.remove(res.size() - 1);
            res.addAll(after);
        } else if (before.last() == after.last()) {
            var tmp = new ArrayList<>(after);
            Collections.reverse(tmp);
            res.addAll(before);
            res.remove(res.size() - 1);
            res.addAll(tmp);
        } else {
            throw new IllegalArgumentException("Cannot merge unconnected OSMWays");
        }
        return res;
	}

    @Override
    public long getAsLong() {
        return id;
    }
}