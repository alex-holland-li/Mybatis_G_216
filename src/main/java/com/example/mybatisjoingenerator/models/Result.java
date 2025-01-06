package com.example.mybatisjoingenerator.models;

/**
 * @author liyun
 * @program Mybatis_G_216
 * @create 2025/1/6
 **/
public class Result<T> {
    public T data;
    public String error;

    public Result(T data, String error) {
        this.data = data;
        this.error = error;
    }

    public static <D> Result<D> error(String s) {
        return new Result<>(null, s);
    }

    public static <D> Result<D> ok(D data) {
        return new Result<>(data, null);
    }
}
