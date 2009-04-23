/**
 * 
 */
package org.lispdev;

import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides logging and tracing facilities (run time debugging).
 * @author sk
 */
public class LispdevDebug
{
  // To add new trace tag do the following:
  // 1. add tag to the end of TRACE_TAGS 
  //    (e.g. TRACE_TAGS = new String[]{"launch","sbcl","new-tag"};)
  // 2. add TRACE_ integer in the end of list by incrementing previous by 1
  //    (e.g. after TRACE_SBCL declaration put: public static final int TRACE_NEW_TAG = TRACE_SBCL + 1;)
  
  /**
   * Identifies trace path for Eclipse tracing facility.
   */
  public static final String TRACE_PATH = LispdevPlugin.ID + "/trace/";
  
  /**
   * Tags that added to trace path depending on TRACE_ integers used
   * in trace() function.
   */
  public static final String[] TRACE_TAGS = new String[]{"launch","sbcl"};
 
  public static final int TRACE_LAUNCH = 0;
  public static final int TRACE_SBCL = TRACE_LAUNCH + 1;

  private static String prepareTrace(int type, String msg)
  {
    String tag = null;
    if(type >= TRACE_TAGS.length)
    {
      logError("Trace type " + String.valueOf(type) + " is not defined");
    }
    else
    {
      tag = TRACE_TAGS[type];
    }
    return "<"+tag+"> "+msg;
  }
  
  /**
   * Prints trace message. To see messages the tracing for particular message
   * type should be enabled:
   * <p>
   * When starting plug-in in run-time Eclipse workbench
   * (through Run Eclipse Application) select which types of messages to trace.
   * <p>
   * When plug-in is installed in stand-alone running Eclipse do the following
   * to switch on tracing: Create .option file containing particular
   * TRACE_PATH + trace tag you want to trace (e.g. org.lispdev/trace/sbcl).
   * Then start eclipse with -debug .options command line (you might need to
   * specify full path to the .option file, e.g: eclipse.exe -debug c:/temp/.options
   * TODO: not tested yet. maybe need to replace -debug with -trace
   * 
   * @param type Type of trace message (one of TRACE_ integers).
   * @param msg Text to print.
   */
  @SuppressWarnings("deprecation")
  public static void trace(int type, String msg)
  {
    LispdevPlugin.getDefault().trace(type, prepareTrace(type,msg));
  }

  /**
   * By default trace prints to System.out. Use this function to redirect it
   * to another stream.
   */
  @SuppressWarnings("deprecation")
  public static void setTraceStream(PrintStream pr)
  {
    LispdevPlugin.getDefault().setTraceStream(pr);
  }
  
  /**
   * Returns stream where trace is printed.
   */
  @SuppressWarnings("deprecation")
  public static PrintStream getTraceStream()
  {
    return LispdevPlugin.getDefault().getTraceStream();
  }
 
  /**
   * Throws exception with custom message.
   * @param message Message that assigned to the exception
   * @param e a low-level exception or <tt>null</tt> if not applicable
   * @throws CoreException
   * 
   * <p>
   * This is a convenience function to throw an exception with custom
   * message. 
   */
  public static void abort(String message, Throwable e) throws CoreException
  {
    throw new CoreException(new Status(IStatus.ERROR, LispdevPlugin.ID,
        0, message, e));
  }

  // FIXME: tried to get shell by
  // PlatformUI.getWorkbench().getDisplay().getActiveShell()
  // and this didn't work, I am not sure if it will work with other shells
  /**
   * Displays and optionally logs error.
   * @param shell the parent shell or <tt>null</tt>
   * @param message the message to display 
   * @param e exception
   * @param doLog if <tt>true</tt> put message to log
   */
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
