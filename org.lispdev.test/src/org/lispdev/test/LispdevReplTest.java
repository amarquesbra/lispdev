/**
 * 
 */
package org.lispdev.test;

import org.lispdev.views.ReplView;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.*;
import junit.framework.*;

/**
 * @author sk
 *
 */
public class LispdevReplTest extends TestCase
{

  private IWorkbenchPage getPage() {
    IWorkbench workbench= PlatformUI.getWorkbench();
    IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
    return window.getActivePage();
  }

  
  /**
   * Test method for {@link org.lispdev.views.ReplView}.
   * Test that Repl view is available
   */
  public void testViewShowHide() throws PartInitException
  {
    IViewPart view = getPage().showView(ReplView.ID);
    getPage().hideView(view);
  }

  /**
   * Test printing to repl with style TODO: doesn't do any testing yet
   */
  public void testReplPrint() throws PartInitException
  {
    ReplView view = (ReplView)getPage().showView(ReplView.ID);
    view.repl.appendText("some text", "some context", 1,
        new StyleRange[]{new StyleRange(0, 3, null, null, SWT.BOLD)},true);
  }
  
}
