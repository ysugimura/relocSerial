package com.cm55.relocSerial;

import static org.junit.Assert.*;

import org.junit.*;

public class TypeCodeReplacerTest {

  @Test
  public void test() {
    TypeCodeReplacer r = new TypeCodeReplacer("[[[Ljava.lang.Object;");
    assertEquals("[[[Ljava.lang.Object;", r.getOriginal());
    assertEquals("java.lang.Object", r.getElement());
    assertEquals("[[[Ljava.lang.String;", r.getReplaced("java.lang.String"));
    
    r = new TypeCodeReplacer("java.lang.Object");
    assertEquals("java.lang.Object", r.getOriginal());
    assertEquals("java.lang.Object", r.getElement());
    assertEquals("java.lang.String", r.getReplaced("java.lang.String"));
    
    r = new TypeCodeReplacer(int[].class.getName());
    try {
      r.getReplaced("long");
      fail();
    } catch (Exception ex) {}
    assertEquals("[I", r.getOriginal());
  }

}
