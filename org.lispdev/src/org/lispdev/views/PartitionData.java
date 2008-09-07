/**
 * 
 */
package org.lispdev.views;

import org.eclipse.swt.custom.StyleRange;

/**
 * @author sk
 *
 */
public class PartitionData
{
  public int start;
  public int length;
  public String context;
  public StyleRange[] originalStyle;
  public StyleRange[] mouseOverStyle;
  public StyleRange[] mouseOverCtrlStyle;
  public IMouseAction actionClick;
  public IMouseAction actionRightClick;
  public IMouseAction actionCtrlClick;
  public IMouseAction actionCtrlRightClick;
  public IMouseAction actionOver;
  public IMouseAction actionOverCtrl;
  
  public PartitionData(int istart, int ilength, String context, 
      StyleRange[] original,
      StyleRange[] mouseOver, StyleRange[] mouseOverCtrl,
      IMouseAction click, IMouseAction ctrlClick, 
      IMouseAction rightClick, IMouseAction ctrlRightClick,
      IMouseAction over, IMouseAction overCtrl)
  {
    start = istart;
    length = ilength;
    originalStyle = original;
    mouseOverStyle = mouseOver;
    mouseOverCtrlStyle = mouseOverCtrl;
    actionClick = click;
    actionCtrlClick = ctrlClick;
    actionRightClick = rightClick; 
    actionCtrlRightClick = ctrlRightClick;
    actionOver = over;
    actionOverCtrl = overCtrl;
  }
}
