package com.logistimo.callisto.service.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 14/03/17.
 */
public class MyTestClass {
  public static void main(String[] args) {
    String
        a =
        "select $$aaa$$ from my query of $$bbb$$. done and extra text. $$ccc$$ some more extra text.";
    System.out.println(getAllMatches(a, 0));
  }

  public static List<String> getAllMatches(String text, int start) {
    List<String> matches = new ArrayList<>();
    int ss = text.indexOf("$$", start);
    int se = text.indexOf("$$", ss + 1) + 2;
    String subStr = text.substring(ss, se);
    matches.add(subStr);
    if (text.indexOf("$$", se + 1) >= 0) {
      matches.addAll(getAllMatches(text, se + 1));
    }
    return matches;
  }
}
