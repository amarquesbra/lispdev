/**
 * 
 */
package org.lispdev;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author sk
 * 
 */
public class LispdevDebug
{

  // see LispdevPlugin about usage of traces
  public static final int TRACE_LAUNCH = 0;
  public static final int TRACE_SBCL = TRACE_LAUNCH + 1;

  private static String prepareTrace(int type, String msg)
  {
    String tag = null;
    if(type >= LispdevPlugin.TRACE_TAGS.length)
    {
      logError("Trace type " + String.valueOf(type) + " is not defined");
    }
    else
    {
      tag = LispdevPlugin.TRACE_TAGS[type];
    }
    return "<"+tag+"> "+msg;
  }
  
  public static void trace(int type, String msg)
  {
    LispdevPlugin.getDefault().trace(type, prepareTrace(type,msg));
  }

  // usage: in try block through error using abort (e can be set to null)
  // in catch block use showError to display what happened
  public static void abort(String message, Throwable e) throws CoreException
  {
    throw new CoreException(new Status(IStatus.ERROR, LispdevPlugin.ID,
        0, message, e));
  }

  // FIXME: tried to get shell by
  // PlatformUI.getWorkbench().getDisplay().getActiveShell()
  // and this didn't work, I am not sure if it will work with other shells
  public static void showError(Shell shell, String message, CoreException e,
      boolean doLog)
  {
    if(doLog)
    {
      LispdevDebug.logError(message);
    }
    if(e == null)
    {
      MessageDialog.openError(shell, "Error", message);
    }
    else
    {
      ErrorDialog.openError(shell, "Error", message, e.getStatus());
    }
  }

  /**
   * @param message
   * @param severity
   *          Integer constant from {@org.eclipse.core.runtime.IStatus}
   */
  private static void log(String message, int severity, Throwable e)
  {
    LispdevPlugin.getDefault().getLog().log(
        new Status(severity, LispdevPlugin.ID, 0, message, e));
  }

  public static void logInfo(String message)
  {
    log(message, IStatus.INFO, null);
  }

  public static void logWarning(String message)
  {
    log(message, IStatus.WARNING, null);
  }

  public static void logError(String message)
  {
    log(message, IStatus.ERROR, null);
  }

  public static void logException(String message, Throwable e)
  {
    log(message, IStatus.ERROR, e);
  }

}
