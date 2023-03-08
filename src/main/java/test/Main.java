package test;

import searchengine.util.texts.TextUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
public class Main {

    public static void main(String[] args) {

        String html = "В России наступает весна";

        TextUtil textUtil = new TextUtil(html);
        try {
            LinkedHashMap<String, Integer> stringIntegerLinkedHashMap = textUtil.countWords();
            System.out.println(stringIntegerLinkedHashMap);
        } catch (IOException e) {

        }

        String str = "В России то идет снегопад" + "то целыми днями тает снег";

    }
}
