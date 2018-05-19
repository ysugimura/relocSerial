package com.cm55.relocSerial;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.logging.*;

public class RelocSerializer {
  
  /** ロガー */
  protected static Log log = LogFactory.getLog(RelocSerializer.class);

  /** ObjectStreamClassのnameフィールド */
  protected static final Field oscNameField;
  static {
    // OPbjectStreamClassのnameフィールドへのアクセスを得る
    try {
      oscNameField = ObjectStreamClass.class.getDeclaredField("name");
      oscNameField.setAccessible(true);
    } catch (NoSuchFieldException ex) {
      throw new RuntimeException(ex);
    }    
  }
  
  /** 現在のクラス名称から永続名への変換マップ。シリアライズ時に用いる */
  protected Map<String, String>serializeMap = new HashMap<>();
  
  /** オリジナル名もしくは永続名から現在のクラス名称への変換マップ。デシリアライズ時に用いる */
  protected Map<String, String>deserializeMap = new HashMap<>();
  
  public RelocSerializer() {
  }
  
  /**
   * 対象とするクラスをすべて指定する
   * @param clazz シリアライズされているクラス（デシリアライズ時）、もしくはシリアライズするクラス（シリアライズ時）
   * @param permanentName 永続的名称。clazzの名称に関わらず、常にこの名称でシリアライズされる。
   * @param originalName 既存のシリアライズストリームをデシリアライズする場合は、clazzの以前の名称を指定する。
   * 新たにシリアライズする場合には不要、nullでよい。
   */
  public void addTarget(Class<?>clazz, String permanentName, String originalName) {
    if (clazz == null || permanentName == null) throw new NullPointerException();

    // インターフェースではないこと
    if (clazz.isInterface())
      throw new IllegalArgumentException(clazz + " should not be an interface");
    
    // Serializableであること
    if (!Serializable.class.isAssignableFrom(clazz))
      throw new IllegalArgumentException(clazz + " should be Serializable");
    
    // serialVersionUIDが定義されていること
    checkSerialVersionUID(clazz);
    
    // シリアライズ用マップ。現在のクラス名称から永続的名称への変換用
    serializeMap.put(clazz.getName(),  permanentName);

    // デシリアライズ用マップ。旧クラス名称あるいは永続的名称から現在のクラス名称への変換用
    if (originalName != null && originalName.length() > 0) {
      // 旧システムでシリアライズされたオブジェクトについてのみ必要
      deserializeMap.put(originalName, clazz.getName());
    }
    deserializeMap.put(permanentName, clazz.getName());
  }
  
  protected void checkSerialVersionUID(Class<?>clazz) {
    while (Serializable.class.isAssignableFrom(clazz)) {
      Field field;
      try {
        field = clazz.getDeclaredField("serialVersionUID");
        int modifier = field.getModifiers();
        if ((modifier & Modifier.STATIC) == 0 ||
           field.getType() != long.class) throw new RuntimeException();
      } catch (Exception ex) {
        throw new IllegalArgumentException(clazz + " should have static long serialVersionUID");
      }
      clazz = clazz.getSuperclass();      
    }
  }
  
  /** 
   * オブジェクトを永続名でシリアライズしてバイト配列を返す。
   */
  public <T>byte[]serialize(T object) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new RelocOutput(bytes)) {
      out.writeObject(object);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return bytes.toByteArray();
  }

  /** 
   * {@link ObjectOutputStream}を改造し、シリアル化の際のクラス名称を永続名に変更する。
  　　*/
  class RelocOutput extends ObjectOutputStream {
    RelocOutput(OutputStream out) throws IOException {
      super(out);
    }
    
    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
      
      // シリアライズされようとするクラスの名称に対応する永続名を取得する
      TypeCodeReplacer replacer = new TypeCodeReplacer(desc.getName());
      String permanentName = serializeMap.get(replacer.getElement());
      
      // 永続名が無い場合。これはStringクラス等、もともとSerializableのクラス
      if (permanentName == null) {
        super.writeClassDescriptor(desc);
        return;
      }

      // 永続名がある場合
      if (log.isTraceEnabled()) log.trace("output changing " + replacer.getOriginal() + " to " + replacer.getReplaced(permanentName));
      try {
        try {                  
          oscNameField.set(desc, replacer.getReplaced(permanentName));                
          super.writeClassDescriptor(desc);
        } finally { 
          oscNameField.set(desc, replacer.getOriginal());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /** バイト配列をデシリアライズしてオブジェクトを返す */
  @SuppressWarnings("unchecked")
  public <T>T deserialize(byte[]bytes) {
    try (ObjectInputStream in = new RelocInput(new ByteArrayInputStream(bytes))) {
      Object object = in.readObject();
      if (log.isTraceEnabled()) log.trace("object " + object);
      return (T)object;
    } catch (NullPointerException ex) {
      log.warn("NullPointerException:" + ex);
      return null;
    } catch (InstantiationError ex) {
      log.warn("InstantiationError:" + ex);
      return null;
    } catch (ClassNotFoundException ex) {
      log.warn("ClassNotFoundException:" + ex);
      return null;
    } catch (InvalidClassException ex) {
      log.warn("InvalidClassException:" + ex);
      return null;
    } catch (IOException ex) {
      log.warn("IOException at reading:" + ex);
      return null;
    } 
  }
  
  /**
   * Javaの{@link ObjectInputStream}を変更したもの。
   * シリアライゼーションストリームを読み込みオブジェクトを再構築するのだが、しかし、ストリーム中にあるオブジェクトのクラス名は以下のケースがある。
   * <ul>
   * <li>オリジナル名である場合
   * <li>永続名の場合
   * </ul>
   * いずれの場合にも、現在のクラス名称に変換する。
   * @author ysugimura
   */
  class RelocInput extends ObjectInputStream {
    
    protected RelocInput(InputStream in) throws IOException {
      super(in);
    }    
    
    /** クラスデスクリプタの読み込み時に、古いFull-Qualified名称から新たな名称に変更する */
    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
      
      // ObjectStreamクラスを読む
      ObjectStreamClass osc = super.readClassDescriptor();
      TypeCodeReplacer replacer = new TypeCodeReplacer(osc.getName());
      
      // そのクラス名称が変更対象の場合には、変更先名称、つまり現在のクラス名を得る
      String currentClassName = deserializeMap.get(replacer.getElement());
      //if (log.isTraceEnabled()) log.trace"current " + currentClassName);
      
      // 変換対象ではない
      if (currentClassName == null) return osc;

      // クラス名を変換する
      if (log.isTraceEnabled()) log.trace("input changing " + replacer.getOriginal() + " to " + replacer.getReplaced(currentClassName));
      try {
        oscNameField.set(osc, replacer.getReplaced(currentClassName));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }      
      return osc;
    }
  }
}
