package org.yo.yotil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection操作的帮助类
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 2020-12-12 15:39:23
 */
public class CollectionUtils {
    /**
     * null or empty
     *
     * @param t 待操作列表
     * @return true:null or empty
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-02-21 16:32:46
     */
    public static <T> boolean isNullOrEmpty(Collection<T> t) {
        return t == null || t.isEmpty();
    }

    /**
     * not null and not empty
     *
     * @param t 待操作列表
     * @return true:null or empty
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-02-21 16:32:46
     */
    public static <T> boolean isNotNullOrEmpty(Collection<T> t) {
        return !isNullOrEmpty(t);
    }

    /**
     * 默认值
     *
     * @param t 待操作列表
     * @return 如果t有值则返回t，否则返回Collections.emptyList()
     */
    public static <T> Collection<T> defaultIfEmpty(Collection<T> t) {
        return defaultIfEmpty(t, Collections.emptyList());
    }

    /**
     * 默认值
     *
     * @param t 待操作列表
     * @param d 默认值
     * @return 如果t有值则返回t，否则返回默认值
     */
    public static <T> Collection<T> defaultIfEmpty(Collection<T> t, Collection<T> d) {
        return isNullOrEmpty(t) ? d : t;
    }

    /**
     * 向 sourceList 追加 t
     * <p>注意：有做简单的contains去重复，但是contains底层是基于equals，所以对于对象，如果要去重，请自己重写equals()和hashCode()<p>
     *
     * @param sourceList 源数据
     * @param t          待追加的数据
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-02-21 13:30:55
     */
    public static <T> void append(Collection<T> sourceList, T t) {
        if (sourceList == null || t == null || sourceList.contains(t)) {
            return;
        }
        sourceList.add(t);
    }

    /**
     * 向 sourceList 追加 t
     *
     * @param sourceList   源数据
     * @param t            待追加的数据
     * @param keyExtractor 去重字段
     * @param <T>          类型
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-02-21 13:30:55
     */
    public static <T> void append(Collection<T> sourceList, T t, Function<? super T, ?> keyExtractor) {
        if (sourceList == null || t == null) {
            return;
        }
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        for (T s : sourceList) {
            if (s == null) {
                continue;
            }
            seen.add(keyExtractor.apply(s));
        }
        if (seen.add(keyExtractor.apply(t))) {
            sourceList.add(t);
        }
    }

    /**
     * 向 sourceList 追加 appendList
     * <p>注意：有做简单的contains去重复，但是contains底层是基于equals，所以对于对象，如果要去重，请自己重写equals()和hashCode()<p>
     *
     * @param sourceList 源数据
     * @param appendList 待追加的数据
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-02-21 13:30:55
     */
    public static <T> void appendToList(Collection<T> sourceList, Collection<T> appendList) {
        if (sourceList == null || isNullOrEmpty(appendList)) {
            return;
        }
        for (T t : appendList) {
            if (t == null || sourceList.contains(t)) {
                continue;
            }
            sourceList.add(t);
        }
    }

    /**
     * 向 sourceList 追加 appendList
     *
     * @param sourceList   源数据
     * @param appendList   待追加的数据
     * @param keyExtractor 去重字段
     * @param <T>          类型
     * @author Kyle.Y.Li
     * @since 1.0.0 2021-01-08 09:51:22
     */
    public static <T> void appendToList(Collection<T> sourceList, Collection<T> appendList, Function<? super T, ?> keyExtractor) {
        if (sourceList == null) {
            return;
        }
        if (isNullOrEmpty(appendList)) {
            return;
        }
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        for (T t : sourceList) {
            if (t == null) {
                continue;
            }
            seen.add(keyExtractor.apply(t));
        }
        for (T t : appendList) {
            if (t == null || !seen.add(keyExtractor.apply(t))) {
                continue;
            }
            sourceList.add(t);
        }
    }

    /**
     * 合并多个集合到新的集合，并根据指定条件去重复
     *
     * @param ts                   源数据
     * @param distinctKeyExtractor 去重字段
     * @param <T>                  元素类型
     * @return 合并去重的集合
     */
    @SafeVarargs
    public static <T> Collection<T> merge(Function<? super T, ?> distinctKeyExtractor, Collection<T>... ts) {
        if (distinctKeyExtractor == null || ts == null || ts.length < 1) {
            return null;
        }
        return Stream.of(ts)
                .filter(CollectionUtils::isNotNullOrEmpty)
                .flatMap(Collection::stream)
                .filter(distinctPre(distinctKeyExtractor))
                .collect(Collectors.toList());
    }

    /**
     * 根据 operator函数 删除元素
     *
     * @param t      待操作列表
     * @param filter 删除条件Fun
     * @return 是否删除成功
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-18 17:44:52
     */
    public static <T> boolean removeIf(Collection<T> t, Predicate<? super T> filter) {
        if (isNullOrEmpty(t)) {
            return false;
        }
        boolean removed = false;
        final Iterator<T> iterator = t.iterator();
        while (iterator.hasNext()) {
            if (filter.test(iterator.next())) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * 根据字段去重复
     *
     * @param t            待操作列表
     * @param keyExtractor 去重字段
     * @return 去重复后结果
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-03 10:03:40
     */
    public static <T> Collection<T> distinct(Collection<T> t, Function<? super T, ?> keyExtractor) {
        if (isNullOrEmpty(t)) {
            return t;
        }
        Predicate<? super T> distinctPre = distinctPre(keyExtractor);
        return t.stream().filter(distinctPre).collect(Collectors.toList());
    }

    /**
     * 构造去重复的条件
     *
     * @param keyExtractor 去重字段
     * @return 去重复的条件
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-03 10:03:59
     */
    private static <T> Predicate<T> distinctPre(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> t != null && seen.add(keyExtractor.apply(t));
    }

    /**
     * 判断是否存在
     *
     * @param t         list
     * @param predicate condition
     * @return true:存在，false:不存在
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-07 16:08:37
     */
    public static <T> boolean exists(Collection<T> t, Predicate<T> predicate) {
        if (isNullOrEmpty(t)) {
            return false;
        }
        for (T i : t) {
            if (i == null) {
                return false;
            }
            if (predicate.test(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否全部存在
     *
     * @param t         list
     * @param predicate condition
     * @param <T>       type
     * @return true:全部存在, false:不是全部存在
     */
    public static <T> boolean all(Collection<T> t, Predicate<T> predicate) {
        if (isNullOrEmpty(t)) {
            return false;
        }
        for (T i : t) {
            if (i == null || !predicate.test(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从一个集合中查找符合要求的第一个元素（面对Stream.filter()查找元素，请优先使用这个方法，这个方法效率高得多）。
     * <p>
     *
     * @param <T>       元素类型
     * @param t         集合
     * @param predicate 验证器
     * @return 如果找到则返回头一个匹配的元素，否则返回null
     */
    public static <T> T find(Collection<T> t, Predicate<? super T> predicate) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        for (T i : t) {
            if (i != null && predicate.test(i)) {
                return i;
            }
        }
        return null;
    }

    /**
     * 从一个集合中查找符合要求的第一个元素（面对Stream.filter()查找元素，请优先使用这个方法，这个方法效率高得多）。
     * <p>
     *
     * @param <T>       元素类型
     * @param <R>       转化后的类型
     * @param t         集合
     * @param predicate 验证器
     * @param mapper    转换器
     * @return 如果找到则通过转换器返回头一个匹配的元素，否则返回null
     */
    public static <T, R> R find(Collection<T> t, Predicate<? super T> predicate, Function<? super T, ? extends R> mapper) {
        T i = find(t, predicate);
        if (i == null) {
            return null;
        }
        return mapper.apply(i);
    }

    /**
     * 从一个集合中查找符合要求的集合
     *
     * @param <T>       元素类型
     * @param t         集合
     * @param predicate 验证器
     * @return 如果找到则返回匹配的元素集合，否则返回null
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-08-07 13:49:06
     */
    public static <T> Collection<T> findList(Collection<T> t, Predicate<? super T> predicate) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        return t.stream().filter(i -> i != null && predicate.test(i)).collect(Collectors.toList());
    }

    /**
     * 从一个集合中查找符合要求的集合，并转换成一个新的集合
     *
     * @param <T>       元素类型
     * @param <R>       转化后的类型
     * @param t         集合
     * @param predicate 验证器
     * @param mapper    转换器
     * @return 如果找到匹配的元素集合，则根据转换器转换类型后返回元素集合，否则返回null
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-12-12 16:10:56
     */
    public static <T, R> Collection<R> findList(Collection<T> t, Predicate<? super T> predicate, Function<? super T, ? extends R> mapper) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        return t
                .stream()
                .filter(i -> i != null && predicate.test(i)) //find the source list that meets the predicate condition
                .map(mapper) //convert:source -> target
                .collect(Collectors.toList());
    }

    /**
     * 从一个集合中查找符合要求的集合，并转换成一个新的集合，最后将转换后的集合去重复
     *
     * @param <T>                  元素类型
     * @param <R>                  转化后的类型
     * @param t                    集合
     * @param predicate            验证器
     * @param mapper               转换器
     * @param distinctKeyExtractor 去重字段
     * @return 如果找到匹配的元素集合，则根据转换器转换类型后返回元素集合，否则返回null
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-12-12 16:10:56
     */
    public static <T, R> Collection<R> findList(Collection<T> t, Predicate<? super T> predicate, Function<? super T, ? extends R> mapper, Function<? super R, ?> distinctKeyExtractor) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        Collection<R> rs = new ArrayList<>(t.size());
        Set<Object> s = ConcurrentHashMap.newKeySet(t.size());
        for (T i : t) {
            if (i == null || !predicate.test(i)) {
                continue;
            }
            R r = mapper.apply(i);
            if (r == null || distinctKeyExtractor == null || !s.add(distinctKeyExtractor.apply(r))) {
                continue;
            }
            rs.add(r);
        }
        s = null;
        return rs;
    }

    /**
     * 从一个集合中查找符合要求的集合，并转换成一个新的集合，并从原始数据源移除满足条件的数据
     *
     * @param <T>       元素类型
     * @param <R>       转化后的类型
     * @param t         集合
     * @param predicate 验证器
     * @param mapper    转换器
     * @return 如果找到匹配的元素集合，则根据转换器转换类型后返回元素集合，否则返回null
     * @author Kyle.Y.Li
     * @since feature_41399 2024-04-09 16:24:11
     */
    public static <T, R> Collection<R> findUniqueList(Collection<T> t, Predicate<? super T> predicate, Function<? super T, ? extends R> mapper) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        //记录满足条件的source
        Collection<T> m = findList(t, predicate);
        if (isNullOrEmpty(m)) {
            return null;
        }
        //结果
        Collection<R> r = findList(t, predicate, mapper);
        //删除源数据中满足条件的数据
        t.removeAll(m);
        return r;
    }

    /**
     * 从 {@param list} 集合中查找到 小于  {@param key} 比较对象的  Collection<T>集合
     * <p>T必须实现Comparable,因为有使用:NavigableSet<p/>
     * <p>时间复杂度:O(log n)</p>
     *
     * @param <T> 元素类型
     * @param t   T集合
     * @param key T比较对象
     * @return 小于 比较对象的集合
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-30 13:39:48
     */
    public static <T extends Comparable<? super T>> Collection<T> findLowestList(Collection<T> t, T key) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        TreeSet<T> treeSet = new TreeSet<T>(t);
        NavigableSet<T> navigableSet = treeSet.headSet(key, false);
        if (navigableSet.isEmpty()) {
            return null;
        }
        return navigableSet;
    }

    /**
     * 从 {@param list} 集合中查找到 小于  {@param key} 比较对象的  Collection<T>集合
     * <p>T必须实现Comparable,因为有使用:NavigableSet<p/>
     * <p>时间复杂度:O(log n)</p>
     *
     * @param <T> 元素类型
     * @param t   T集合
     * @param key T比较对象
     * @return 小于 比较对象的集合
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-30 13:39:48
     */
    public static <T extends Comparable<? super T>> Collection<T> findHighestList(Collection<T> t, T key) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        TreeSet<T> treeSet = new TreeSet<T>(t);
        NavigableSet<T> navigableSet = treeSet.tailSet(key, false);
        if (navigableSet.isEmpty()) {
            return null;
        }
        return navigableSet;
    }

    /**
     * 从 {@param list} 集合中查找到 小于  或 大于 {@param key} 比较对象的  T 的子集合
     * <p>T必须实现Comparable,因为有使用:二分查找算法<p/>
     * <p>时间复杂度:O(log n)</p>
     *
     * @param <T>      元素类型
     * @param list     T集合
     * @param key      T比较对象
     * @param isLowest 是否小于比较对象(true:小于,false:大于)
     * @return 小于 或 大于 比较对象的集合
     * @author Kyle.Y.Li
     * @since 1.0.0 2020-07-30 13:39:48
     */
    public static <T extends Comparable<? super T>> List<T> findSubList(List<T> list, T key, boolean isLowest) {
        if (isNullOrEmpty(list)) {
            return null;
        }
        //排序
        Collections.sort(list);
        //二分查找算法,快速查找满足条件的元素位置
        int index = Collections.binarySearch(list, key);
        //判断是否存在比 最低阈值 还小的item,存在返回
        if (isLowest) {
            //如果集合中不存在该元素，则会返回 -(插入点  + 1)
            if (index < 1) {
                index = Math.abs(index) - 1;
            }
            //如果集合中存在该元素，则会返回该元素在集合中的下标
            if (index > 0) {
                //当前元素的下表是:index,比 最低阈值 小的第1个值下标是:index - 1,subList截取不包含toIndex,所以截取时再+1
                index = index - 1;
                if (index >= list.size() - 1) {
                    index = list.size() - 1;
                }
                return new ArrayList<T>(list.subList(0, index + 1));
            }
        }
        //判断是否存在比 最低阈值 还大的item,存在返回
        else {
            //如果集合中存在该元素，则会返回该元素在集合中的下标
            if (index >= 0) {
                index = index + 1;
            }
            //如果集合中不存在该元素，则会返回 -(插入点  + 1)
            if (index < 0) {
                index = Math.abs(index) - 1;
            }
            if (index < list.size()) {
                return new ArrayList<T>(list.subList(index, list.size()));
            }
        }
        return null;
    }
    
    /**
     * 将集合进行分片
     *
     * @param <T>  元素类型
     * @param t    集合
     * @param size 每片个数
     * @return 如果找到匹配的元素集合，则根据转换器转换类型后返回元素集合，否则返回null
     * @author Kyle.Y.Li
     * @since feature_41399 2024-04-09 16:24:11
     */
    public static <T> List<List<T>> partition(List<T> t, int size) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        return (t instanceof RandomAccess)
                ? new RandomAccessPartition<>(t, size)
                : new Partition<>(t, size);
    }

    /**
     * 根据字段排序
     *
     * @param t            集合
     * @param keyExtractor 排序字段
     * @param <T>          元素类型
     * @param <U>          排序字段限定
     * @return 排序结果
     * @author Kyle.Y.Li
     * @since feature_41767 2024-07-19 13:45:11
     */
    public static <T, U extends Comparable<? super U>> Collection<T> orderBy(Collection<T> t, Function<? super T, ? extends U> keyExtractor) {
        if (isNullOrEmpty(t) || keyExtractor == null) {
            return null;
        }
        return t
                .stream()
                .sorted(Comparator.comparing(keyExtractor))
                .collect(Collectors.toList());
    }

    /**
     * 根据字段排序
     *
     * @param t            集合
     * @param keyExtractor 排序字段
     * @param <T>          元素类型
     * @param <U>          排序字段限定
     * @return 排序结果
     * @author Kyle.Y.Li
     * @since feature_41767 2024-07-19 13:45:11
     */
    public static <T, U extends Comparable<? super U>> Collection<T> orderByDescending(Collection<T> t, Function<? super T, ? extends U> keyExtractor) {
        if (isNullOrEmpty(t) || keyExtractor == null) {
            return null;
        }
        return t
                .stream()
                .sorted(Comparator.comparing(keyExtractor).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 返回 {@link List}
     *
     * @param t   集合
     * @param <T> 元素类型
     * @return 返回 {@link List}
     */
    public static <T> List<T> toList(Collection<T> t) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        return new ArrayList<>(t);
    }

    /**
     * 返回 {@link Set}
     *
     * @param t   集合
     * @param <T> 元素类型
     * @return 返回 {@link Set}
     */
    public static <T> Set<T> toSet(Collection<T> t) {
        if (isNullOrEmpty(t)) {
            return null;
        }
        return new TreeSet<>(t);
    }

    /**
     * 将 {@link Iterable} 全部添加到 {@link Collection}
     *
     * @param t        集合
     * @param iterable 迭代器
     * @param <T>      元素类型
     * @return 返回{@link Collection}
     */
    public static <T> boolean addAll(final Collection<T> t, final Iterable<? extends T> iterable) {
        if (isNullOrEmpty(t) || iterable == null) {
            return false;
        }
        if (iterable instanceof Collection<?>) {
            return t.addAll((Collection<? extends T>) iterable);
        }
        return addAll(t, iterable.iterator());
    }

    /**
     * 将 {@link Iterator} 全部添加到 {@link Collection}
     *
     * @param t        集合
     * @param iterator 迭代器
     * @param <T>      元素类型
     * @return 返回{@link Collection}
     */
    public static <T> boolean addAll(final Collection<T> t, final Iterator<? extends T> iterator) {
        if (isNullOrEmpty(t) || iterator == null) {
            return false;
        }
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= t.add(iterator.next());
        }
        return changed;
    }
}
