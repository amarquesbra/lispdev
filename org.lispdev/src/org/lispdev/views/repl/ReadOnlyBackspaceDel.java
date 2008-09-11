/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

/**
 * @author sk
 *
 */
public class ReadOnlyBackspaceDel implements VerifyKeyListener
{
  private Repl repl;
  
  public ReadOnlyBackspaceDel(Repl repl)
  {
    this.repl = repl;
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
   */
  public void verifyKey(VerifyEvent event)
  {
    int offset = repl.getTextWidget().getCaretOffset();
    if(repl == null || !repl.isInEditMode() || offset < repl.getEditOffset())
    {
      return;
    }
    if( event.keyCode == SWT.DEL )
    {
      PartitionData pd = repl.getReadOnlyPartition(offset, Repl.AFTER);
      if( pd != null )
      {
        repl.deletePartInEdit(pd);
        event.doit = false;
      }
      return;
    }
    if( event.keyCode == SWT.BS )
    {
      PartitionData pd = repl.getReadOnlyPartition(offset, Repl.BEFORE);
      if( pd != null )
      {
        repl.deletePartInEdit(pd);
        event.doit = false;
      }
      return;
    }
    return;
  }

}
