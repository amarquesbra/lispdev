/**
 * 
 */
package org.lispdev.test;

import org.lispdev.views.ReplView;

import org.eclipse.ui.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author sk
 *
 */
public class LispdevReplTest
{

  private IWorkbenchPage getPage() {
    IWorkbench workbench= PlatformUI.getWorkbench();
    IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
    return window.getActivePage();
  }

  
  /**
   * Test method for {@link org.lispdev.views.ReplView}.
   */
  @Test
  public void viewShowHide() throws PartInitException
  {
    IViewPart view = getPage().showView(ReplView.ID);
    getPage().hideView(view);
  }
  
  
}
