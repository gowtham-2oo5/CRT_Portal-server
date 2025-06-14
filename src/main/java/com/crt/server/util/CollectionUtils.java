package com.crt.server.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class to help prevent ConcurrentModificationException
 * and provide thread-safe collection operations
 */
public class CollectionUtils {

    /**
     * Creates a defensive copy of a collection to prevent ConcurrentModificationException
     */
    public static <T> List<T> defensiveCopy(Collection<T> collection) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    /**
     * Creates a thread-safe defensive copy using CopyOnWriteArrayList
     */
    public static <T> List<T> threadSafeDefensiveCopy(Collection<T> collection) {
        if (collection == null) {
            return new CopyOnWriteArrayList<>();
        }
        return new CopyOnWriteArrayList<>(collection);
    }

    /**
     * Creates a defensive copy as a Set for O(1) lookup operations
     */
    public static <T> Set<T> defensiveCopyAsSet(Collection<T> collection) {
        if (collection == null) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Creates a thread-safe defensive copy as a Set
     */
    public static <T> Set<T> threadSafeDefensiveCopyAsSet(Collection<T> collection) {
        if (collection == null) {
            return ConcurrentHashMap.newKeySet();
        }
        return ConcurrentHashMap.newKeySet(collection.size());
    }

    /**
     * Safely removes elements from a collection while iterating
     */
    public static <T> void safeRemoveIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return;
        }
        
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (predicate.test(element)) {
                iterator.remove();
            }
        }
    }

    /**
     * Safely modifies elements in a collection while iterating
     */
    public static <T> List<T> safeTransform(Collection<T> collection, Function<T, T> transformer) {
        if (collection == null) {
            return new ArrayList<>();
        }
        
        return collection.stream()
                .map(transformer)
                .collect(Collectors.toList());
    }

    /**
     * Creates a synchronized list wrapper
     */
    public static <T> List<T> synchronizedList() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Creates a synchronized list wrapper with initial capacity
     */
    public static <T> List<T> synchronizedList(int initialCapacity) {
        return Collections.synchronizedList(new ArrayList<>(initialCapacity));
    }

    /**
     * Creates a synchronized set wrapper
     */
    public static <T> Set<T> synchronizedSet() {
        return Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Safely checks if a collection contains an element by creating a defensive copy
     */
    public static <T> boolean safeContains(Collection<T> collection, T element) {
        if (collection == null || element == null) {
            return false;
        }
        
        // Create defensive copy to avoid ConcurrentModificationException
        List<T> safeCopy = new ArrayList<>(collection);
        return safeCopy.contains(element);
    }

    /**
     * Safely gets the size of a collection
     */
    public static <T> int safeSize(Collection<T> collection) {
        if (collection == null) {
            return 0;
        }
        
        try {
            return collection.size();
        } catch (ConcurrentModificationException e) {
            // If concurrent modification occurs, create defensive copy and get size
            return new ArrayList<>(collection).size();
        }
    }

    /**
     * Safely converts a collection to stream
     */
    public static <T> java.util.stream.Stream<T> safeStream(Collection<T> collection) {
        if (collection == null) {
            return java.util.stream.Stream.empty();
        }
        
        // Create defensive copy to prevent ConcurrentModificationException during stream operations
        return new ArrayList<>(collection).stream();
    }

    /**
     * Batch process a large collection to avoid memory issues
     */
    public static <T, R> List<R> batchProcess(Collection<T> collection, int batchSize, Function<List<T>, List<R>> processor) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<T> list = new ArrayList<>(collection);
        List<R> results = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, list.size());
            List<T> batch = list.subList(i, endIndex);
            results.addAll(processor.apply(batch));
        }
        
        return results;
    }

    /**
     * Safely merge two collections without duplicates
     */
    public static <T> List<T> safeMerge(Collection<T> collection1, Collection<T> collection2) {
        Set<T> merged = new HashSet<>();
        
        if (collection1 != null) {
            merged.addAll(new ArrayList<>(collection1));
        }
        
        if (collection2 != null) {
            merged.addAll(new ArrayList<>(collection2));
        }
        
        return new ArrayList<>(merged);
    }

    /**
     * Thread-safe way to add elements to a collection
     */
    public static <T> void safeAdd(Collection<T> collection, T element) {
        if (collection != null && element != null) {
            synchronized (collection) {
                collection.add(element);
            }
        }
    }

    /**
     * Thread-safe way to add all elements to a collection
     */
    public static <T> void safeAddAll(Collection<T> target, Collection<T> source) {
        if (target != null && source != null && !source.isEmpty()) {
            synchronized (target) {
                target.addAll(new ArrayList<>(source));
            }
        }
    }
}
