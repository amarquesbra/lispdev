/**
 * 
 */
package org.lispdev.test;

import static org.junit.Assert.*;

import org.lispdev.*;
import org.lispdev.utils.*;
import org.junit.*;

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
   * Test method for {@link org.lispdev.LispdevDebug#abort(java.lang.String, java.lang.Throwable)}.
   */
  @Test
  public final void testAbort()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#showError(org.eclipse.swt.widgets.Shell, java.lang.String, org.eclipse.core.runtime.CoreException, boolean)}.
   */
  @Test
  public final void testShowError()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#log(java.lang.String, int, java.lang.Throwable)}.
   */
  @Test
  public final void testLog()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#logInfo(java.lang.String)}.
   */
  @Test
  public final void testLogInfo()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#logWarning(java.lang.String)}.
   */
  @Test
  public final void testLogWarning()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#logError(java.lang.String)}.
   */
  @Test
  public final void testLogError()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#logException(java.lang.String, java.lang.Throwable)}.
   */
  @Test
  public final void testLogException()
  {
    fail("Not yet implemented"); // TODO
  }
  
}
