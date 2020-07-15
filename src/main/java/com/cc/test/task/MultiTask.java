package com.cc.test.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

/**
 * 单个入参，多个返回值的场景
 *
 * @param <T>
 * @param <R>
 */
class MultiTask<T, R> extends RecursiveTask<List<R>> {

    private static final long serialVersionUID = 4315189806425636032L;

    private final List<T> list;

    private final Function<T, List<R>> function;

    public MultiTask(List<T> list, Function<T, List<R>> function) {
        this.list = list;
        this.function = function;
    }

    @Override
    protected List<R> compute() {
        if (list.isEmpty())
            return new ArrayList<>();

        int size = list.size();
        if (size == 1) {
            List<R> result = function.apply(list.get(0));

            if (result == null) {
                result = new ArrayList<>();
            }

            return result;
        }

        int middle = size / 2;
        MultiTask<T, R> left = new MultiTask<>(list.subList(0, middle), function);
        MultiTask<T, R> right = new MultiTask<>(list.subList(middle, size), function);

        invokeAll(left, right);

        List<R> v1 = left.join();
        List<R> v2 = right.join();

        v1.addAll(v2);

        return v1;
    }
}