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

  public ReplAutoEdit(Repl r)
  {
    repl = r;
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
      repl.logTrace("Carret is in read-only region, moving to start of write region");
      c.offset = repl.getEditOffset();
    }
  }

}
