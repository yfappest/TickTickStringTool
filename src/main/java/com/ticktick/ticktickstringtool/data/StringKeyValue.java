package com.ticktick.ticktickstringtool.data;

public class StringKeyValue {

    public final String key;
    public final String zhVal;
    public final String enVal;


    public StringKeyValue(String key, String zhVal, String enVal) {
        this.key = key.replace(" ", "_").replace("'", "_").toLowerCase();
        this.zhVal = zhVal;
        this.enVal = enVal;
    }

    public StringKeyValue(String[] kv) {
        this(kv[0], kv[1], kv[2]);
    }
}
