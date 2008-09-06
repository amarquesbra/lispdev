/**
 * 
 */
package org.lispdev.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author sk
 *
 */
public class StringPrintStream extends PrintStream
{
  private StringFilteredStream str;
  
  public StringPrintStream()
  {
    super(new StringFilteredStream(new ByteArrayOutputStream()));
    str = (StringFilteredStream)super.out;
  }
  
  public void resetString()
  {
    str.resetString();
  }
  
  public String getString()
  {
    return str.getString();
  }
  
}
