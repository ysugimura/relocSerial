package com.cm55.relocSerial;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;


public class InnerToStatic2Test {

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

    
    // デシリアライズする
    RelocSerializer ser = new RelocSerializer();
    ser.addTarget(Foo2.class, "foo", Foo0.class.getName());
    ser.addTarget(Foo2.Bar2.class, "bar", Foo0.Bar0.class.getName());
    Foo2 foo = ser.deserialize(bytes);
    assertEquals(Foo2.Bar2.class, foo.bar.getClass());
    assertEquals(123, foo.bar.value);
    assertSame(foo, foo.bar.this$1);
  }
  

  public static class Foo0 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar0 bar = new Bar0();
    
    public class Bar0 implements Serializable {
      private static final long serialVersionUID = 1L;
      int value = 123;
    }
  }

  
  public static class Foo2 implements Serializable {
    private static final long serialVersionUID = 1L;
    Bar2 bar = new Bar2();
    
    public static class Bar2 implements Serializable {
      private static final long serialVersionUID = 1L;
      Foo2 this$1;
      int value;
    }
  }
}
