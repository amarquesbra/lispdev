/**
 * 
 */
package org.lispdev.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author sk
 *
 */
public class FilteredStream extends FilterOutputStream
{
  private String str = "";

  public FilteredStream(OutputStream aStream)
  {
    super(aStream);
  }
  
  public void resetString()
  {
    str = "";
  }
  
  public String getString()
  {
    return str;
  }

  public void write(byte b[]) throws IOException
  {
    String aString = new String(b);
    str += aString;
  }

  public void write(byte b[], int off, int len) throws IOException
  {
    String aString = new String(b, off, len);
    str += aString;
  }

}
