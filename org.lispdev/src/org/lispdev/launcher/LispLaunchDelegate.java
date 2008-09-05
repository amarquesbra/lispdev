/**
 * 
 */
package org.lispdev.launcher;

import org.lispdev.LispdevDebug;
import org.lispdev.LispdevPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

/**
 * @author sk
 *
 */
public class LispLaunchDelegate implements ILaunchConfigurationDelegate
{

  /* (non-Javadoc)
   * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
   */
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    String program = configuration.getAttribute(LispdevPlugin.ATTR_LISP_EXE, (String)null);
    String flavor = configuration.getAttribute(LispdevPlugin.ATTR_LISP_FLAVOR, (String)null);
    LispdevDebug.trace(LispdevDebug.TRACE_LAUNCH,"<Lanching>\n program = "+program+"\n flavor = "+flavor);
    if (flavor == null) 
    {
      LispdevDebug.logWarning("Lisp is not specified");
    }

    String[] commandLine = null;
    String label = "";
    if( flavor.equalsIgnoreCase(LispdevPlugin.CL_FLAVOR_SBCL) )
    {
      commandLine = new String[] {"cmd", "/C", "sbcl"};
      label = "sbcl";
    }
    else if( flavor.equalsIgnoreCase(LispdevPlugin.CL_FLAVOR_CLISP) )
    {
      commandLine = new String[] {"cmd", "/C", "clisp"};
      label = "clisp";
    }
    try
    {
      Process process = DebugPlugin.exec(commandLine, null);
      IProcess p = DebugPlugin.newProcess(launch, process, label);
    }
    catch(Throwable e)
    {
      LispdevDebug.logException("Could not lanuch lisp "+flavor+" on path "+program, e);
    }
  }
  
}
