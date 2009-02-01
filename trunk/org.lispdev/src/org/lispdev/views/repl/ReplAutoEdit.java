/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;

/**
 * @author sk
 *
 */
public class ReplAutoEdit implements IAutoEditStrategy
{
  private Repl repl; 

  public ReplAutoEdit(Repl repl)
  {
    this.repl = repl;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
   */
  public void customizeDocumentCommand(IDocument d, DocumentCommand c)
  {
    repl.logTrace("DocCommand = {"+c.length+","+c.offset+","+c.text+"}", 10);
    if( repl == null ) return;
    if( !repl.getEditModeFlag() )
    {
      //repl.logWarning("Called Auto Edit command when repl is in read-only mode");
      return;
    }

    Point sel = repl.getSelectedRange();
    if( sel.x < repl.getEditOffset() )
    {
      sel.y -= repl.getEditOffset() - sel.x;
      sel.x = repl.getEditOffset();
    }
    if(sel.y > 0)
    {
      //extend selection to cover overlapping read-only
      //remove read-only (without text)
      repl.logTrace("Sel = "+sel.y, 1);
      Point selnew = repl.computeExpandedEditSelection();
      if( selnew != null )
      {
        repl.toDeletePartitions(selnew);
        c.offset = selnew.x;
        c.length = selnew.y - selnew.x;
      }
    }
    else
    {
      if( c.offset < repl.getEditOffset() )
      {
        repl.logTrace("ReplAutoEdit.customizeDocumentCommand: " +
            "move carret from read-only to start of edit region",5);
        c.offset = repl.getEditOffset();
      }
      PartitionData pd = repl.getReadOnlyPartition(c.offset,Repl.NONE); 
      if( pd != null )
      {
        c.offset = repl.getEditOffset() + pd.start + pd.length;
      }        
    }
  }

}
