package org.lispdev;

import java.util.ResourceBundle;

import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

import java.io.IOException;
import java.util.*;

/**
 * The activator class controls the plug-in life cycle
 */
public class LispdevPlugin extends AbstractUIPlugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "org.lispdev";

  public static final String ATTR_LISP_EXE = PLUGIN_ID + ".ATTR_LISP_EXE";
  public static final String ATTR_LISP_FLAVOR = PLUGIN_ID + ".ATTR_LISP_FLAVOR";
  public static final String ID_LAUNCH_CONFIGURATION_TYPE = PLUGIN_ID
      + ".launchType";

  public static final String CL_FLAVOR_SBCL = "SBCL";
  public static final String CL_FLAVOR_CLISP = "CLISP";
  public static final String[] CL_FLAVORS = new String[]{CL_FLAVOR_SBCL, CL_FLAVOR_CLISP};

  // tracing. More natural place would be LispdevDebug, but I want to keep
  // functions in LispdevDebug static
  // Plugin can be static, but still make necessary initializations
  // To add test category: in the end of list, add public integer incremented by one (moved to LispdevDebug)
  // add string identifying trace to TRACE_TAGS, add TRACE_PATH+identifying string
  // to .options file
  // Usage in code: printTrace(integer representing trace type, message)
  // Usage in debug: Go-to launch dialog and turn tracing on/off
  // Usage during run: start eclipse like this: eclipse.exe -debug c:/eclipse/plugins/org.lispdev/.options
  // not tested yet. maybe need to replace debug with trace
  private static final String TRACE_PATH = LispdevPlugin.PLUGIN_ID + "/trace/";
  
  // collection of identifying strings
  public static final String[] TRACE_TAGS = new String[]{"launch","sbcl"};
  private boolean[] B_TRACES = new boolean[TRACE_TAGS.length];
  
  private void initTraces()
  {
    for(int i = 0; i < TRACE_TAGS.length; ++i)
    {
      String val = Platform.getDebugOption(TRACE_PATH+TRACE_TAGS[i]);
      B_TRACES[i] = (val != null && val.equalsIgnoreCase("true"));
    }
  }
  
  public void trace(int type, String msg)
  {
    if( B_TRACES[type] )
    {
      System.out.print(msg);
    }
  }
  
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

  public static boolean isOSWindows()
  {
    return Platform.getOS().equals(Constants.OS_WIN32);
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
        resourceBundle = ResourceBundle.getBundle("org.lispdev.Resources");
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
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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

}
