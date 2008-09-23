/**
 * 
 */
package org.lispdev.test;

import org.lispdev.views.ReplView;
import org.lispdev.views.repl.PartitionData;
import org.lispdev.views.repl.Repl;

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
   * Test printing to repl with style
   */
  public void testReplPrint() throws PartInitException
  {
    ReplView view = (ReplView)getPage().showView(ReplView.ID);
    view.repl.clear();
    view.repl.appendText("some text", "some context", "1",
        new StyleRange[]{new StyleRange(0, 3, null, null, SWT.BOLD)},true);
    assertEquals(true, view.repl.sanityCheck());
    // how to test: get partition at position 4 and check its text and
    // style
    PartitionData pd = view.repl.getPartitionAt(4, Repl.NONE);
    assertEquals("some text",view.repl.getText(pd));
    assertEquals("some context",pd.context);
    assertEquals(SWT.BOLD,pd.originalStyle[0].fontStyle);
    assertEquals(0,pd.originalStyle[0].start);
    assertEquals(3,pd.originalStyle[0].length);
  }
  
  /**
   * Test method for {@link org.lispdev.views.Repl#clear()}
   */
  public void testClear() throws PartInitException
  {
    ReplView view = (ReplView)getPage().showView(ReplView.ID);
    view.repl.appendText("some text", "some context", "1",
        new StyleRange[]{new StyleRange(0, 3, null, null, SWT.BOLD)},true);
    view.repl.clear();
    assertEquals("",view.repl.getDocument().get());
    assertEquals(true,view.repl.sanityCheck());
  }
  
  
}
