/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

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
    if( repl == null ) return;
    if( !repl.isInEditMode() )
    {
      repl.logWarning("Called Auto Edit command when repl is in read-only mode");
      return;
    }
    if( c.offset < repl.getEditOffset() )
    {
      repl.logTrace("ReplAutoEdit.customizeDocumentCommand: " +
      		"move carret from read-only to start of edit region",5);
      c.offset = repl.getEditOffset();
    }
    else
    {
      //FIXME: add delete and backspace keytrigger and process readonly parts
      PartitionData pd = repl.getReadOnlyPartition(c.offset,Repl.NONE); 
      if( pd != null )
      {
        c.offset = repl.getEditOffset() + pd.start + pd.length;
      }
    }
  }

}
