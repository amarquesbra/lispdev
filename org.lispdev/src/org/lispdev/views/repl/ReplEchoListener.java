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
  public ReplEchoListener(Repl r)
  {
    repl = r;
  }

  /* (non-Javadoc)
   * @see org.lispdev.views.repl.IReplInputListener#run(java.lang.String, java.lang.String)
   */
  public void run(String msg, String context, VerifyEvent e)
  {
    repl.stopEdit();
    e.doit = false;
    String str = "\nPrinted: \""+msg+"\", in context: \""+context+"\"";
    StyleRange pr = new StyleRange();
    pr.start = 0;
    pr.length = "Printed: ".length();
    pr.fontStyle = SWT.BOLD;
    StyleRange cn = new StyleRange();
    cn.start = "Printed: \"".length()+msg.length()+"\", in ".length();
    cn.length = "context:".length();
    cn.fontStyle = SWT.BOLD;
    repl.appendText(str, 
        new PartitionData(0,str.length(),"echo_context",new StyleRange[]{pr,cn}));
    repl.startEdit("Echo>", "echo_prompt", new StyleRange[]{new StyleRange(0,5,null,null,SWT.BOLD)});
    repl.getTextWidget().setCaretOffset(repl.getDocument().getLength());
  }
  
}

