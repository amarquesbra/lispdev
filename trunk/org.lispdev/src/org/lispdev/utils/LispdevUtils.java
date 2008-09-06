/**
 * 
 */
package org.lispdev.utils;

import java.io.*;

/**
 * @author sk
 * 
 */
public class LispdevUtils
{

  public static String inputStreamAsString(InputStream stream)
      throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    StringBuilder sb = new StringBuilder();
    String line = null;

    while((line = br.readLine()) != null)
    {
      sb.append(line + "\n");
    }

    br.close();
    return sb.toString();
  }
}
