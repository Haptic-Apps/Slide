package me.ccrama.redditslide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos on 10/19/2016.
 */

public class CaseInsensitiveArrayList extends ArrayList<String> {

    public CaseInsensitiveArrayList() {
        super();
    }

    public CaseInsensitiveArrayList(CaseInsensitiveArrayList strings) {
        super(strings);
    }

    public CaseInsensitiveArrayList(List<String> strings) {
        super(strings);
    }

    @Override
    public boolean contains(Object o) {
        String parameter = (String) o;
        for (String s : this) {
            if (parameter.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}
