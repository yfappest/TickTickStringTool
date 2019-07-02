package com.ticktick.ticktickstringtool.utlis;

public class Paths {

    private static final String XML_PATH_CN1 = "/tickTick_CN/src/main/res/values/strings.xml";
    private static final String XML_PATH_CN2 = "/TickTick_IN/src/main/res/values-zh-rCN/strings.xml";
    private static final String XML_PATH_EN1 = "/TickTick/src/main/res/values/strings.xml";
    private static final String XML_PATH_EN2 = "/TickTick_IN/src/main/res/values/strings.xml";
    private static final String XML_PATH_EN3 = "/TickTick_CN/src/main/res/values-en/strings.xml";
    private static final String COMMON_XML_PATH_CN1 = "/tickTick_CN/src/main/res/values/strings-common.xml";
    private static final String COMMON_XML_PATH_CN2 = "/TickTick_IN/src/main/res/values-zh-rCN/strings-common.xml";
    private static final String COMMON_XML_PATH_EN1 = "/TickTick/src/main/res/values/strings-common.xml";
    private static final String COMMON_XML_PATH_EN2 = "/TickTick_IN/src/main/res/values/strings-common.xml";
    private static final String COMMON_XML_PATH_EN3 = "/TickTick_CN/src/main/res/values-en/strings-common.xml";

    private final String baseDir;

    private static Paths paths;
    public static Paths getInstance(String baseDir) {
        if(paths == null){
            paths = new Paths(baseDir);
        }
        return paths;
    }
    
    private Paths(String baseDir) {
        this.baseDir = baseDir;
    }

    public final String xmlPathCn1() {
        return baseDir + XML_PATH_CN1;
    }

    public final String xmlPathCn2() {
        return baseDir + XML_PATH_CN2;
    }

    public final String xmlPathEn1() {
        return baseDir + XML_PATH_EN1;
    }

    public final String xmlPathEn2() {
        return baseDir + XML_PATH_EN2;
    }

    public final String xmlPathEn3() {
        return baseDir + XML_PATH_EN3;
    }

    public final String commonXmlPathCn1() {
        return baseDir + COMMON_XML_PATH_CN1;
    }

    public final String commonXmlPathCn2() {
        return baseDir + COMMON_XML_PATH_CN2;
    }

    public final String commonXmlPathEn1() {
        return baseDir + COMMON_XML_PATH_EN1;
    }

    public final String commonXmlPathEn2() {
        return baseDir + COMMON_XML_PATH_EN2;
    }

    public final String commonXmlPathEn3() {
        return baseDir + COMMON_XML_PATH_EN3;
    }
}
