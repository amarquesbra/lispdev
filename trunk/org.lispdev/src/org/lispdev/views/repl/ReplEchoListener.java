/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.VerifyEvent;

/**
 * @author sk
 *
 */
public class ReplEchoListener implements IReplInputListener
{
  private Repl repl;
  public ReplEchoListener(Repl repl)
  {
    this.repl = repl;
  }
  
  /* (non-Javadoc)
   * @see org.lispdev.views.repl.IReplInputListener
   */
  public void run(String msg, int offset, PartitionData pd, VerifyEvent e)
  {
    e.doit = false;
    if( offset < repl.getEditOffset() ) // append read-only to repl
    {
      repl.insertPartInEdit(repl.getDocument().getLength(), msg, pd);
    }
    else
    {
      repl.stopEdit();
      String str = "Printed: \""+msg+"\", in context: \""+pd.context
        +"\", with id "+String.valueOf(pd.id);
      StyleRange pr = new StyleRange();
      pr.start = 0;
      pr.length = "Printed: ".length();
      pr.fontStyle = SWT.BOLD;
      StyleRange cn = new StyleRange();
      cn.start = "Printed: \"".length()+msg.length()+"\", in ".length();
      cn.length = "context: ".length();
      cn.fontStyle = SWT.BOLD;
      repl.appendText(str, 
          new PartitionData(0,str.length(),"echo_context","0",
              new StyleRange[]{pr,cn}),true);
      repl.startEdit();      
    }
  }
  
}
