package com.cm55.relocSerial;

import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.*;

@RunWith(Suite.class)
@SuiteClasses( { 
  InnerToStatic2Test.class,
  InnerToStaticTest.class,
  RelocSerializerTest.class,
  TypeCodeReplacerTest.class
})
public class AllTest {
  public static void main(String[] args) {
    JUnitCore.main(AllTest.class.getName());
  }
}
