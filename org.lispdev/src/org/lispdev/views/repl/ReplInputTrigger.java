/**
 * 
 */
package org.lispdev.views.repl;

import java.util.ArrayList;

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

  private ArrayList<IReplInputListener> listeners;
  
  public ReplInputTrigger(Repl r)
  {
    repl = r;
  }
  
  public void addInputListener(IReplInputListener rl)
  {
    if(listeners == null)
    {
      listeners = new ArrayList<IReplInputListener>();      
    }
    listeners.add(rl);
  }
  
  private void run(VerifyEvent event)
  {
    if( listeners == null )
    {
      repl.logInfo("Input trigger has no listeners");
      return;
    }
    int offset = repl.getTextWidget().getCaretOffset();
    PartitionData pd = repl.getPartitionAt(offset);
    if( pd == null )
    {
      //repl.getTextWidget().
      //repl.getTextWidget().getCaretOffset()
      //repl.getTextWidget().print(printer, options)
      //repl.getTextWidget().scroll(destX, destY, x, y, width, height, all)
      //repl.getTextWidget().setCaretOffset(offset)
      //repl.getTextWidget().setFont(font)
      //repl.getTextWidget().setKeyBinding(key, action)
      //repl.getTextWidget().setMenu(menu)
      //repl.getTextWidget().setSelection(start, end)
      //repl.getTextWidget().showSelection()
      //repl.addTextInputListener(listener)
      //repl.addTextListener(listener)
      //repl.getUndoManager()
      //repl.prependAutoEditStrategy(strategy, contentType)
      //repl.prependVerifyKeyListener(listener)
      //repl.getUndoManager().
      //repl.revealRange(start, length)
      //repl.setAutoIndentStrategy(strategy, contentType)
      //repl.setTabsToSpacesConverter(converter)
      //repl.setTextColor(color)
      //repl.setTextColor(color, start, length, controlRedraw)
      //repl.setUndoManager(undoManager)
      //repl.CONTENTASSIST_CONTEXT_INFORMATION
      //repl.CONTENTASSIST_PROPOSALS
      //repl.DELETE
      //repl.FORMAT
      //repl.INFORMATION
      //repl.PRINT
      //repl.REDO
      //repl.SELECT_ALL
      //repl.SHIFT_LEFT
      //repl.SHIFT_RIGHT
      //repl.UNDO

      repl.logErr("Partition data for event at "+String.valueOf(offset)
          +" does not exist, although whole document supposed to be partitioned");
      return;
    }
    String msg = repl.getText(pd);
    for( IReplInputListener rl : listeners )
    {
      rl.run(msg,pd,event);
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
