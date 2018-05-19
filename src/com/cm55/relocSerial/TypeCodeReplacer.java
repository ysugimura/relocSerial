package com.cm55.relocSerial;

/**
 * いわゆるJavaのタイプコード中のクラス要素を変換する。
 * 配列以外の場合には、"int", "java.lang.Object"という文字列で表現された型コードになるが、
 * 配列の場合には、"[I", "[Ljava.lang.Object;"などとなる。
 * このクラスは、その要素を抜き出し、これを置換できるようにする。
 * @author ysugimura
 */
public class TypeCodeReplacer {
  
  private String prec = "";
  private String element;
  private String succ = "";
  private boolean replaceable;
  
  public TypeCodeReplacer(String name) {
    this.element = name;
    
    // '['が途切れるまでindexをすすめる
    int index = 0;
    while (name.charAt(index) == '[')
      index++;
    
    // 最初から'['ではない。つまり配列ではない。
    if (index == 0) {
      replaceable = true;
      return;
    }

    // 配列だが、要素はクラスではない。この場合は置き換え不可
    if (name.charAt(index) != 'L') return;
    
    // クラスの場合
    replaceable = true;
    prec = name.substring(0, index + 1);
    element = name.substring(index + 1, name.length() - 1);
    succ = name.substring(name.length() - 1);
  }

  /** 要素名を取得する */
  public String getElement() {
    return element;
  }

  /** 要素を置換したタイプコードを返す。ただし、置換できるのは、配列でないか、あるいはオブジェクトの配列のみ */
  public String getReplaced(String name) {
    if (!replaceable) throw new RuntimeException("not replaceable");    
    return prec + name + succ;
  }

  /** 元のタイプコードを返す */
  public String getOriginal() {
    return prec + element + succ;
  }
  
  @Override
  public String toString() {
    throw new RuntimeException();
  }
}
