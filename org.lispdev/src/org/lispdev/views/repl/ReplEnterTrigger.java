/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.swt.events.VerifyEvent;

/**
 * @author sk
 *
 */
public class ReplEnterTrigger extends ReplInputTrigger
{
  private int stateMask;
  
  /**
   * @param r - repl to connect with
   * @param stateMask - one of the following: SWT.NONE, or combination of
   * SWT.ALT, SWT.CTRL, SWT.SHIFT (combination performed using |, example:
   * SWT.ALT | SWT.CTRL
   */
  public ReplEnterTrigger(Repl repl, int stateMask)
  {
    super(repl);
    this.stateMask = stateMask;
  }

  /* (non-Javadoc)
   * @see org.lispdev.views.repl.ReplInputTrigger#check(org.eclipse.swt.events.VerifyEvent)
   */
  @Override
  protected boolean check(VerifyEvent event)
  {
    return (event.stateMask == stateMask &&
        (event.keyCode == '\r' || event.keyCode == '\n'));
  }

}
