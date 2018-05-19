package com.cm55.relocSerial;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.*;

public class RelocSerializerTest {

  @Test
  public void test1() throws Exception {
    byte[]bytes;
    {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream objOut = new ObjectOutputStream(byteOut);    
      objOut.writeObject(new Foo0());
      bytes = byteOut.toByteArray();
    }
    
    RelocSerializer ser = new RelocSerializer();
    ser.addTarget(Foo1.class, "foo", Foo0.class.getName());
    ser.addTarget(Bar1.class, "bar", Bar0.class.getName());
    Foo1 foo = ser.deserialize(bytes);
    assertEquals(Bar1.class, foo.bar.getClass());
  }
  
  public static class Foo0 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar0 bar = new Bar0();
  }
  
  public static class Bar0 implements Serializable {
    private static final long serialVersionUID = 1L;
  }
  
  public static class Foo1 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar1 bar = new Bar1();
  }
  
  public static class Bar1 implements Serializable {
    private static final long serialVersionUID = 1L;
  }
  
  @Test
  public void test2() {
    RelocSerializer ser = new RelocSerializer();
    try {
      ser.addTarget(Sample0.class, "sample0", null);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
    try {
      ser.addTarget(Sample1.class, "sample1", null);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
    try {
      ser.addTarget(Sample2.class, "sample2", null);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
    try {
      ser.addTarget(Sample3.class, "sample3", null);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
  }
  
  public static class Sample0 implements Serializable {    
  }
  
  public static class Sample1 {
    private static final long serialVersionUID = 1L;
  }
  
  public static class Sample2 implements Serializable {   
    private static final int serialVersionUID = 1;
  }
  
  public static class Sample3 implements Serializable {   
    private final long serialVersionUID = 1L;
  }
}
