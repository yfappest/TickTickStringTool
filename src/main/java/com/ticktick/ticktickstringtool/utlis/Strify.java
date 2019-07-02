package com.ticktick.ticktickstringtool.utlis;

import com.ticktick.ticktickstringtool.callback.ExecCallback;
import com.ticktick.ticktickstringtool.callback.FinishCallback;
import com.ticktick.ticktickstringtool.data.KeyValue;
import com.ticktick.ticktickstringtool.data.StringKeyValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Strify {

    private List<StringKeyValue> stringKeyValues;

    private final String baseDir;
    private boolean isCommon = false;
    private StrifyOriginStringGetter getter;
    private ExecCallback execCallback;
    private FinishCallback finishCallback;

    private Strify(String baseDir) {
        this.baseDir = baseDir;
    }

    public static Strify newInstance(String baseDir) {
        return new Strify(baseDir);
    }

    public Strify setStringKeyValues(List<StringKeyValue> stringKeyValues) {
        this.stringKeyValues = stringKeyValues;
        return this;
    }

    public Strify setCommon(boolean common) {
        isCommon = common;
        return this;
    }

    public Strify setExecCallback(ExecCallback execCallback) {
        this.execCallback = execCallback;
        return this;
    }

    public Strify setFinishCallback(FinishCallback callback) {
        this.finishCallback = callback;
        return this;
    }


    public Strify setStrifyOriginStringGetter(StrifyOriginStringGetter getter) {
        this.getter = getter;
        return this;
    }

    public void exec() {
        Paths paths = Paths.getInstance(baseDir);
        if (isCommon) {
            execCommon(paths);
        } else {
            execNormal(paths);
        }
    }

    private void execCommon(Paths paths) {
        exec(paths.commonXmlPathCn1(), Lang.ZH);
        exec(paths.commonXmlPathCn2(), Lang.ZH);
        exec(paths.commonXmlPathEn1(), Lang.EN);
        exec(paths.commonXmlPathEn2(), Lang.EN);
        exec(paths.commonXmlPathEn3(), Lang.EN);
    }

    private void execNormal(Paths paths) {
        exec(paths.xmlPathCn1(), Lang.ZH);
        exec(paths.xmlPathCn2(), Lang.ZH);
        exec(paths.xmlPathEn1(), Lang.EN);
        exec(paths.xmlPathEn2(), Lang.EN);
        exec(paths.xmlPathEn3(), Lang.EN);
    }

    private void exec(String path, Lang lang) {
        List<String> items = getNewStrings(lang);
        write(path, items, getter.get(path));
    }


    private List<String> getNewStrings(Lang lang) {
        List<KeyValue<String, String>> items = getItems(lang);
        List<String> strings = new ArrayList<>();
        Set<String> existKeys = new HashSet<>(); //避免重复值
        for (KeyValue<String, String> keyValue : items) {
            if (!existKeys.contains(keyValue.key)) {
                strings.add("<string name=\"" + keyValue.key + "\">" + keyValue.value + "</string>");
                existKeys.add(keyValue.key);
            }
        }
        return strings;
    }

    private List<KeyValue<String, String>> getItems(Lang lang) {
        return stringKeyValues.stream().map(v -> KeyValue.get(v.key, lang == Lang.ZH ? v.zhVal : v.enVal)).collect(Collectors.toList());
    }

    private void write(String path, List<String> strings, String orgin) {
        execCallback.onExec(path);
        File file = new File(path);
        try (PrintStream writer = new PrintStream((new FileOutputStream(file)))) {
            writer.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:ignore=\"MissingTranslation\">");
            writer.print(orgin);
            for (String string : strings) {
                writer.print("  ");
                writer.println(string);
                writer.flush();
            }
            writer.print("</resources>");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishCallback.onFinish(path);
    }

    public interface StrifyOriginStringGetter {
        String get(String path);
    }

    enum Lang {
        ZH, EN
    }


}
