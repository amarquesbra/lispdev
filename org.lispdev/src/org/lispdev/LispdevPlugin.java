package org.lispdev;

import java.util.ResourceBundle;

import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.lispdev.views.ReplView;
import org.osgi.framework.BundleContext;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

import java.io.*;
import java.util.*;

/**
 * The activator class controls the plug-in life cycle
 */
public class LispdevPlugin extends AbstractUIPlugin
{

  /**
   * The plug-in's ID
   */
  public static final String ID = "org.lispdev";

  public static final String ATTR_LISP_EXE = ID + ".ATTR_LISP_EXE";
  public static final String ATTR_LISP_FLAVOR = ID + ".ATTR_LISP_FLAVOR";
  public static final String ID_LAUNCH_CONFIGURATION_TYPE = ID
      + ".launchType";

  public static final String CL_FLAVOR_SBCL = "SBCL";
  public static final String CL_FLAVOR_CLISP = "CLISP";
  public static final String[] CL_FLAVORS = new String[]{CL_FLAVOR_SBCL, CL_FLAVOR_CLISP};
  
  private ReplView replView = null;
  public void setReplView(ReplView replView)
  {
    this.replView = replView;
  }
  public ReplView getReplView()
  {
    return replView;
  }

  // ============================================================
  // Some tracing code to keep functions from LispdevDebug static
  // ============================================================
  
  private boolean[] B_TRACES = new boolean[LispdevDebug.TRACE_TAGS.length];
  
  private void initTraces()
  {
    for(int i = 0; i < LispdevDebug.TRACE_TAGS.length; ++i)
    {
      String val = Platform.getDebugOption(LispdevDebug.TRACE_PATH
          +LispdevDebug.TRACE_TAGS[i]);
      B_TRACES[i] = (val != null && val.equalsIgnoreCase("true"));
    }
  }
  
  private PrintStream traceStream;
  
   /**
   * This is a convenience function for LispdevDebug.setTraceStream(). So use that one. 
   */
  @Deprecated
  public void setTraceStream(PrintStream pr)
  {
    traceStream = pr;
  }
  
   /**
   * This is a convenience function for LispdevDebug.getTraceStream(). So use that one. 
   */
  @Deprecated
  public PrintStream getTraceStream()
  {
    if( traceStream == null )
    {
      return System.out;
    }
    else
    {
      return traceStream;      
    }
  }
  
  /**
   * This is a convenience function for LispdevDebug.trace(). So use that one. 
   */
  @Deprecated
  public void trace(int type, String msg)
  {
    if( B_TRACES[type] )
    {
      if( traceStream != null )
      {
        traceStream.print(msg);
      }
      else
      {
        System.out.print(msg); 
      }
    }
  }
  // =========== end of tracing code ==========================
  
  // The shared instance
  private static LispdevPlugin plugin;
  // Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * The constructor
   */
  public LispdevPlugin()
  {
    super();
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    plugin = this;
    initTraces();
    traceStream = System.out;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception
  {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static LispdevPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = LispdevPlugin.getDefault().getResourceBundle();
    try
    {
      return (bundle != null) ? bundle.getString(key) : key;
    }
    catch(MissingResourceException e)
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  private ResourceBundle getResourceBundle()
  {
    try
    {
      if(resourceBundle == null)
        resourceBundle = ResourceBundle.getBundle(ID+".Resources");
    }
    catch(MissingResourceException x)
    {
      resourceBundle = null;
    }
    return resourceBundle;
  }
  
  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  private static ImageDescriptor getImageDescriptor(String path)
  {
    return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
  }
  
  public static String getPluginPath()
  {
    try
    {
      String path = FileLocator.resolve(getDefault().getBundle().getEntry("/")).getFile();
      if(isOSWindows())
      {
        if(path.matches("/\\w:/.*"))
        {
          path = path.substring(1);
        }
      }
      return path;
    }
    catch(IOException e)
    {
      LispdevDebug.logException("Could not get plugin path:", e);
    }
    return "";
  }

  // shell is used everywhere - FIXME:doesn't work?, see showError for explanation
  public static Shell getShell()
  {
    return PlatformUI.getWorkbench().getDisplay().getActiveShell();
  }

  public static boolean isOSWindows()
  {
    return Platform.getOS().equals(Constants.OS_WIN32);
  }
  
  public static boolean isOSLinux()
  {
    return Platform.getOS().equals(Constants.OS_LINUX);
  }
  
  public static boolean isOSMacOSX()
  {
    return Platform.getOS().equals(Constants.OS_MACOSX);
  }

}
