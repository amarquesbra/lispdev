/**
 * 
 */
package org.lispdev.views.repl;

import java.util.ArrayList;

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
  public ArrayList<PartitionData> children;

  public PartitionData(int start, int length, String context)
  {
    this(start, length, context, null, null, null, null, null, null, null,
        null, null);
  }

  public PartitionData(int start, int length, String context,
      StyleRange[] originalStyle)
  {
    this(start, length, context, originalStyle, null, null, null, null, null,
        null, null, null);
  }

  /**
   * @param start all style and children data is relative this this start
   * @param length
   * @param context
   * @param originalStyle
   * @param mouseOverStyle
   * @param mouseOverCtrlStyle
   * @param actionClick
   * @param actionRightClick
   * @param actionCtrlClick
   * @param actionCtrlRightClick
   * @param actionOver
   * @param actionOverCtrl
   */
  public PartitionData(int start, int length, String context,
      StyleRange[] originalStyle, StyleRange[] mouseOverStyle,
      StyleRange[] mouseOverCtrlStyle, IMouseAction actionClick,
      IMouseAction actionRightClick, IMouseAction actionCtrlClick,
      IMouseAction actionCtrlRightClick, IMouseAction actionOver,
      IMouseAction actionOverCtrl)
  {
    this.start = start;
    this.length = length;
    this.context = context;
    this.originalStyle = originalStyle;
    this.mouseOverStyle = mouseOverStyle;
    this.mouseOverCtrlStyle = mouseOverCtrlStyle;
    this.actionClick = actionClick;
    this.actionRightClick = actionRightClick;
    this.actionCtrlClick = actionCtrlClick;
    this.actionCtrlRightClick = actionCtrlRightClick;
    this.actionOver = actionOver;
    this.actionOverCtrl = actionOverCtrl;
    this.children = null;
  }
  
  public PartitionData clone()
  {
    PartitionData pd = new PartitionData(start,length,context,
        originalStyle.clone(),mouseOverStyle.clone(),mouseOverCtrlStyle.clone(),
        actionClick,actionRightClick,actionCtrlClick,actionCtrlRightClick,
        actionOver,actionOverCtrl);
    if( children != null )
    {
      pd.children = new ArrayList<PartitionData>(children.size());
      for( PartitionData pdc : children )
      {
        pd.children.add(pdc.clone());
      }
    }
    return pd;
  }
  
  public String toString()
  {
    String res = "{"+context+","
    +String.valueOf(start)+","+String.valueOf(length)+"}";
    return res;
  }
}
