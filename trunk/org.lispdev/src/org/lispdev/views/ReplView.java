/**
 * 
 */
package org.lispdev.views;

import org.lispdev.LispdevPlugin;

import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * @author sk
 *
 */
public class ReplView extends ViewPart
{
  public static final String ID = LispdevPlugin.ID + ".replView";

  public Repl repl;
  private Label info;

  
  @Override
  public void createPartControl(Composite parent) {
    GridLayout layout = new GridLayout(1, false);
    layout.marginLeft = 1;
    layout.marginTop = 1;
    layout.marginRight = 1;
    layout.marginBottom = 1;
    parent.setLayout(layout);
    
    GridData gd;
    
   //TODO: probably it is better to show info in status bar
    info = new Label(parent, SWT.BORDER);
    gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = false;
    info.setLayoutData(gd);
    info.setText("Status label for Repl");

    // Put a border around our text viewer
    Composite comp = new Composite(parent, SWT.BORDER);
    layout = new GridLayout(1, false);
    layout.marginLeft = 0;
    layout.marginTop = 0;
    layout.marginRight = 0;
    layout.marginBottom = 0;
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    comp.setLayout(layout);
    gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    comp.setLayoutData(gd);
    
    repl = new Repl(comp, new VerticalRuler(10), 
        SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.LEFT | SWT.BORDER);
    repl.getControl().setLayoutData(gd);
    //repl.getTextWidget().setFont(newFont);

    repl.setEditable(true);
    repl.print("some text", null, 
        new StyleRange[]{new StyleRange(0, 3, null, null, SWT.BOLD)});
    repl.startEdit("Enter your command>", "this prompt",null);
    
  }


  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus()
  {
    // TODO Auto-generated method stub

  }

}
