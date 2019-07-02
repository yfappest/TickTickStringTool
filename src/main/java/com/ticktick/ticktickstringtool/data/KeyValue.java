package com.ticktick.ticktickstringtool.data;

public class KeyValue<F,S> {

    public final F key;
    public final S value;

    private KeyValue(F key, S value) {
        this.key = key;
        this.value = value;
    }

    public static <F,S> KeyValue<F,S> get(F key,S value) {
       return new KeyValue<>(key,value);
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
