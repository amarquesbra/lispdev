/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class ReplConfiguration extends TextSourceViewerConfiguration
{
  private Repl repl;
  
  public ReplConfiguration(Repl r)
  {
    repl = r;
  }
  
  public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer,
      String contentType)
  {
    return new IAutoEditStrategy[]{new ReplAutoEdit(repl)};
  }


}
