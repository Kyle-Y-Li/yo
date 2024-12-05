package org.yo.yotil;


import java.util.AbstractList;
import java.util.List;

/**
 * 列表分区或分段<br>
 * 通过传入分区长度，将指定列表分区为不同的块，每块区域的长度相同（最后一块可能小于长度）<br>
 * 分区是在原List的基础上进行的，返回的分区是不可变的抽象列表，原列表元素变更，分区中元素也会变更。
 *
 * @param <T> 元素类型
 * @author Kyle.Y.Li
 * @since 1.0.0 2020-12-12 15:39:23
 */
public class Partition<T> extends AbstractList<List<T>> {
    private final List<T> list;
    private final int size;
    
    /**
     * 列表分区
     *
     * @param list 被分区的列表，非空
     * @param size 每个分区的长度，必须 > 0
     */
    public Partition(List<T> list, int size) {
        if (list == null) {
            throw new NullPointerException();
        }
        if (size < 1) {
            throw new IllegalArgumentException();
        }
        this.list = list;
        this.size = Math.min(list.size(), size);
    }
    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     */
    @Override
    public List<T> get(int index) {
        final int fromIndex = index * size;
        final int toIndex = Math.min(fromIndex + size, list.size());
        return list.subList(fromIndex, toIndex);
    }
    
    /**
     * Returns the number of elements in this list.  If this list contains
     * more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        final int size = this.size;
        if (size == 0) {
            return 0;
        }
        final int total = list.size();
        int div = total / size;
        int rem = total % size;
        return rem > 0 ? div + 1 : div;
    }
}
