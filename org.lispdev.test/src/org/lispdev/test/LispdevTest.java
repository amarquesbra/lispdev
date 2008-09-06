/**
 * 
 */
package org.lispdev.test;

import org.lispdev.*;
import org.lispdev.utils.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author sk
 * 
 */
public class LispdevTest
{
  
  /**
   * Test method for {@link org.lispdev.LispdevPlugin#PLUGIN_ID}.
   */
  @Test
  public void pluginID()
  {
    
    assertEquals(LispdevPlugin.getDefault().getBundle().getSymbolicName(),
        LispdevPlugin.PLUGIN_ID);
  }

  private final String getTraceString(int type, String msg)
  {
    // redirect trace to string
    StringPrintStream str = new StringPrintStream();
    LispdevPlugin.getDefault().setTraceStream(str);
    
    // run trace
    LispdevDebug.trace(type, msg);
    LispdevPlugin.getDefault().getTraceStream().flush();
    String res = str.getString();
    LispdevPlugin.getDefault().setTraceStream(System.out);
    
    return res;
  }
  
  /**
   * Test method for {@link org.lispdev.LispdevDebug#trace}.
   */
  @Test
  public final void traceOK()
  {
    assertEquals("<launch> testing launch trace",
        getTraceString(LispdevDebug.TRACE_LAUNCH, "testing launch trace"));
    assertEquals("<sbcl> testing sbcl trace",
        getTraceString(LispdevDebug.TRACE_SBCL, "testing sbcl trace"));
  }
  
  /**
   * Test exception in {@link org.lispdev.LispdevDebug#prepareTrace}.
   */
  @Test(expected=ArrayIndexOutOfBoundsException.class)
  public final void traceError()
  {
    assertEquals("<launch> testing launch trace",
        getTraceString(10000, "testing launch trace"));
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#abort}.
   */
  @Test
  public final void abort()
  {
    try
    {
      LispdevDebug.abort("error", null);
    }
    catch(CoreException e)
    {
      assertEquals(IStatus.ERROR,e.getStatus().getSeverity());
      assertEquals("error",e.getLocalizedMessage());
    }
  }

}
