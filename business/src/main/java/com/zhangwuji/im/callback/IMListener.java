package com.zhangwuji.im.callback;

public interface IMListener<T> {
    public abstract void onSuccess(T response);

    public abstract void onFaild();

    public abstract void onTimeout();
}
