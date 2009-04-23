/**
 * 
 */
package org.lispdev.test;

import org.lispdev.*;
import org.lispdev.utils.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import junit.framework.*;

/**
 * @author sk
 * 
 */
public class LispdevTest extends TestCase
{
  
  /**
   * Test method for {@link org.lispdev.LispdevPlugin#ID}.
   * Test that platform supplied ID is same as hardcoded in plugin.
   * All functions in pluggin assume that hardcoded is correct one.
   */
  public void testPluginID()
  {
    
    assertEquals(LispdevPlugin.getDefault().getBundle().getSymbolicName(),
        LispdevPlugin.ID);
  }

  private final String getTraceString(int type, String msg)
  {
    // redirect trace to string
    StringPrintStream str = new StringPrintStream();
    LispdevDebug.setTraceStream(str);
    
    // run trace
    LispdevDebug.trace(type, msg);
    LispdevDebug.getTraceStream().flush();
    String res = str.getString();
    LispdevDebug.setTraceStream(System.out);
    
    return res;
  }
  
  /**
   * Test method for {@link org.lispdev.LispdevDebug#trace}.
   * for this to pass set tracing. go to Run Configurations...->Tracing
   * Set Enable Tracing to On and enable tracing for org.lispdev
   */
  public final void testTraceOK()
  {
    assertEquals("<launch> testing launch trace",
        getTraceString(LispdevDebug.TRACE_LAUNCH, "testing launch trace"));
    assertEquals("<sbcl> testing sbcl trace",
        getTraceString(LispdevDebug.TRACE_SBCL, "testing sbcl trace"));
  }
  
  /**
   * Test exception in {@link org.lispdev.LispdevDebug#prepareTrace}.
   * Test checks exception when asked for non-registered test category
   */
  //@Test(expected=ArrayIndexOutOfBoundsException.class)
  public final void testTraceError() 
  {
    try
    {
      assertEquals("<launch> testing launch trace",
          getTraceString(10000, "testing launch trace"));      
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      
    }
  }

  /**
   * Test method for {@link org.lispdev.LispdevDebug#abort}.
   * Check throw of assertion when abort is called
   */
  public final void testAbort()
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
