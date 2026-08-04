package tech.kayys.alkhawarizm.data;

import java.util.Iterator;

public interface Dataset<T> extends Iterable<T> {
    Iterator<T> iterator();
}