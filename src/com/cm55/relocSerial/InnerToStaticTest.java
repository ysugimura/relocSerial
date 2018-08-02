package com.cm55.relocSerial;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public class InnerToStaticTest {

  @Test
  public void test1() throws Exception {
    
    // シリアライズする。Bar0は内部クラス
    byte[]bytes;
    {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream objOut = new ObjectOutputStream(byteOut);    
      objOut.writeObject(new Foo0());
      bytes = byteOut.toByteArray();
    }

    // デシリアライズする。Bar1はstaticクラス
    RelocSerializer ser = new RelocSerializer();
    ser.addTarget(Foo1.class, "foo", Foo0.class.getName());
    ser.addTarget(Foo1.Bar1.class, "bar", Foo0.Bar0.class.getName());
    Foo1 foo = ser.deserialize(bytes);
    assertEquals(Foo1.Bar1.class, foo.bar.getClass());
    assertEquals(123, foo.bar.value);
  }
  
  @Test
  public void test2() {
    assertNotNull(Foo0.Bar0.class.getEnclosingClass());
    assertNotNull(Foo1.Bar1.class.getEnclosingClass());
  }
  
  public static class Foo0 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar0 bar = new Bar0();
    
    public class Bar0 implements Serializable {
      private static final long serialVersionUID = 1L;
      int value = 123;
    }
  }
    
  public static class Foo1 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar1 bar = new Bar1();
    
    public static class Bar1 implements Serializable {
      private static final long serialVersionUID = 1L;
      int value;
    }
  }

}
