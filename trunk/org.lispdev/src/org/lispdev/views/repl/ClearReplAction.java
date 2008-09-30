package org.lispdev.views.repl;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ClearReplAction extends Action
{
  private Repl repl;
  
  public ClearReplAction(Repl repl)
  {
    this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
        .getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
    this.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
        .getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR_DISABLED));
    this.setToolTipText("Clear Repl");
    this.repl = repl;
  }
  
  public void run()
  {
    repl.clear();
    repl.startEdit();
  }
}
