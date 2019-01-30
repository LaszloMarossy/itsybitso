package com.itsybitso.util;

import java.security.SecureRandom;

import java.util.Random;

public class RandomString {

  private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  //    private static final String lower = upper.toLowerCase(Locale.ROOT);
  private static final String digits = "0123456789";
  private static final int length = 10;
  private static final String alphanum = upper + digits;

  private final Random random;
  private final char[] symbols;
  private final char[] buf;

  public RandomString() {
    this.random = new SecureRandom();
    this.symbols = alphanum.toCharArray();
    this.buf = new char[length];
  }


  public String getRandomString() {
    for (int idx = 0; idx < buf.length; ++idx) {
      buf[idx] = symbols[random.nextInt(symbols.length)];
    }
    return new String(buf);
  }


}