package com.cc.test.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * 单个入参，单个返回的场景
 *
 * @param <T>
 * @param <R>
 */
@Slf4j
class SingleTask<T, R> extends RecursiveTask<List<R>> {

    private static final long serialVersionUID = 4315189806425636032L;

    private final List<T> list;

    private final Function<T, R> function;

    public SingleTask(List<T> list, Function<T, R> function) {
        this.list = list;
        this.function = function;
    }

    @Override
    protected List<R> compute() {
        if (list.isEmpty())
            return new ArrayList<>();

        int size = list.size();
        if (size == 1) {
            T t = list.get(0);
            R result = function.apply(t);

            List<R> data = new ArrayList<>();
            if (result != null)
                data.add(result);

            return data;
        }

        int middle = size / 2;
        SingleTask<T, R> left = new SingleTask<>(list.subList(0, middle), function);
        SingleTask<T, R> right = new SingleTask<>(list.subList(middle, size), function);

        invokeAll(left, right);

        List<R> v1 = left.join();
        List<R> v2 = right.join();
        v1.addAll(v2);

        return v1;
    }
}