package com.core4etc.mqtt;

import java.util.regex.Pattern;

public interface Constant {

    Pattern VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    String ROUTE_GROUP_NAME = "route";
    Pattern ROUTE_REGEX = Pattern.compile("^/(?<" + ROUTE_GROUP_NAME + ">\\w+)/.*");
    Pattern MOBILE_NUMBER_REGEX = Pattern.compile("^(\\+98|09|9)\\d{9}$");
    String DATE_FORMATTING = "yyyy-MM-dd HH:mm:ss.SSS";
    String UI_DATE_FORMATTING = "yyyy/MM/dd-HH:mm:ss";
    String SLASH = "/";
    String DASH = "-";
    String EMPTY = "";
    String DOT = ".";
    String DOT_REGEX = "\\.";

}
