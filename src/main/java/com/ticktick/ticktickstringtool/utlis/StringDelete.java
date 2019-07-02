package com.ticktick.ticktickstringtool.utlis;

import com.ticktick.ticktickstringtool.callback.ExecCallback;
import com.ticktick.ticktickstringtool.callback.FinishCallback;
import com.ticktick.ticktickstringtool.data.KeyValue;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringDelete {

    private final String baseDir;
    private ExecCallback execCallback;
    private FinishCallback finishCallback;
    private Set<String> needDeleteKeys;
    private DeleteOriginStringGetter getter;
    private boolean isCommon = true;
    private boolean isNormal = true;

    public static StringDelete newInstance(String baseDir) {
        return new StringDelete(baseDir);
    }

    private StringDelete(String baseDir) {
        this.baseDir = baseDir;
    }

    public StringDelete setExecCallback(ExecCallback execCallback) {
        this.execCallback = execCallback;
        return this;
    }

    public StringDelete setFinishCallback(FinishCallback callback) {
        this.finishCallback = callback;
        return this;
    }

    public StringDelete setNeedDeleteKeys(Set<String> needDeleteKeys) {
        this.needDeleteKeys = needDeleteKeys;
        return this;
    }

    public StringDelete setDeleteOriginStringGetter(DeleteOriginStringGetter getter) {
        this.getter = getter;
        return this;
    }

    public StringDelete setCommon(boolean common) {
        isCommon = common;
        return this;
    }

    public StringDelete setNormal(boolean normal) {
        isNormal = normal;
        return this;
    }

    public void exec() {
        Paths paths = Paths.getInstance(baseDir);
        if (isCommon) {
            execCommon(paths);
        }
        if (isNormal) {
            execNormal(paths);
        }
    }

    private void execCommon(Paths paths) {
        exec(paths.commonXmlPathCn1());
        exec(paths.commonXmlPathCn2());
        exec(paths.commonXmlPathEn1());
        exec(paths.commonXmlPathEn2());
        exec(paths.commonXmlPathEn3());
    }

    private void execNormal(Paths paths) {
        exec(paths.xmlPathCn1());
        exec(paths.xmlPathCn2());
        exec(paths.xmlPathEn1());
        exec(paths.xmlPathEn2());
        exec(paths.xmlPathEn3());
    }

    private void exec(String path) {
        List<KeyValue<String, String>> keyValues = getter.get(path);
        List<String> needDeleteStrings = keyValues.stream()
                .filter(keyValue -> needDeleteKeys.contains(keyValue.key.trim()))
                .map(keyValue -> keyValue.value)
                .collect(Collectors.toList());
        write(path, needDeleteStrings);
    }


    private void write(String path, List<String> needDeleteStrings) {
        execCallback.onExec(path);
        File file = new File(path);
        try {
            String content = FileUtils.readFileToString(file, Charset.forName("utf8"));
            StringBuilder builder = new StringBuilder(content);
            try (PrintStream writer = new PrintStream((new FileOutputStream(file)))) {
                for (String string : needDeleteStrings) {
                    int index;
                    while ((index = builder.indexOf(string)) > 0) {
                        int start = index - "  ".length();
                        int end = index + string.length() + "\n".length();
                        builder.delete(start, end);
                    }
                }
                writer.print(builder.toString());
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        finishCallback.onFinish(path);

    }

    public interface DeleteOriginStringGetter {
        List<KeyValue<String, String>> get(String path);
    }

}
