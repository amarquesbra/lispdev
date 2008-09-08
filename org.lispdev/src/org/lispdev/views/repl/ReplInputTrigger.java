/**
 * 
 */
package org.lispdev.views.repl;

import java.util.List;

import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

/**
 * @author sk
 *
 */
public abstract class ReplInputTrigger implements VerifyKeyListener
{
  private Repl repl;
  protected Repl getRepl()
  {
    return repl;
  }

  private List<IReplInputListener> listeners;
  
  public ReplInputTrigger(Repl r)
  {
    repl = r;
  }
  
  public void registerInputListener(IReplInputListener rl)
  {
    listeners.add(rl);
  }
  
  private void run(VerifyEvent event)
  {
    if( listeners == null )
    {
      repl.logTrace("Input trigger has no listeners");
      return;
    }
    PartitionData pd = repl.getPartitionAt(event.start);
    if( pd == null )
    {
      repl.logErr("Partition data for event at "+String.valueOf(event.start)+" does not exist");
      return;
    }
    String msg = repl.getText(pd);
    String context = pd.context;
    for( IReplInputListener rl : listeners )
    {
      rl.run(msg,context);
    }
  }
  
  protected abstract boolean check(VerifyEvent event);
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
   */
  public void verifyKey(VerifyEvent event)
  {
    if(check(event)) run(event);
  }

}
