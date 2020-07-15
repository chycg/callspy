package com.cc.test.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

/**
 * 多个入参，多个返回的场景
 *
 * @param <T>
 * @param <R>
 */
class ListTask<T, R> extends RecursiveTask<List<R>> {

    private static final long serialVersionUID = 4315189806425636032L;

    private final List<T> list;

    private final Function<List<T>, List<R>> function;

    private final int groupSize;

    public ListTask(List<T> list, Function<List<T>, List<R>> function, int groupSize) {
        this.list = list;
        this.function = function;
        this.groupSize = groupSize < 1 ? 1 : groupSize;
    }

    @Override
    protected List<R> compute() {
        if (list.isEmpty())
            return new ArrayList<>();

        int size = list.size();
        if (size <= groupSize) {
            List<R> result = function.apply(list);

            if (result == null) {
                result = new ArrayList<>();
            }

            return result;
        }

        int middle = size / 2;
        ListTask<T, R> left = new ListTask<>(list.subList(0, middle), function, groupSize);
        ListTask<T, R> right = new ListTask<>(list.subList(middle, size), function, groupSize);

        invokeAll(left, right);

        List<R> v1 = new ArrayList<>(left.join());
        List<R> v2 = right.join();

        v1.addAll(v2);

        return v1;
    }
}