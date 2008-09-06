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
  private FilteredStream str;
  
  public StringPrintStream()
  {
    super(new FilteredStream(new ByteArrayOutputStream()));
    str = (FilteredStream)super.out;
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
