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
  public String id;
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

  public PartitionData(int start, int length, String context, String id)
  {
    this(start, length, context, id, null, null, null, null, null, null,
        null, null, null);
  }

  public PartitionData(int start, int length, String context, String id,
      StyleRange[] originalStyle)
  {
    this(start, length, context, id, originalStyle, null, null, null, null,
        null, null, null, null);
  }

  /**
   * @param start all style and children data is relative this this start
   * @param length
   * @param context
   * @param id
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
  public PartitionData(int start, int length, String context, String id,
      StyleRange[] originalStyle, StyleRange[] mouseOverStyle,
      StyleRange[] mouseOverCtrlStyle, IMouseAction actionClick,
      IMouseAction actionRightClick, IMouseAction actionCtrlClick,
      IMouseAction actionCtrlRightClick, IMouseAction actionOver,
      IMouseAction actionOverCtrl)
  {
    this.start = start;
    this.length = length;
    this.context = context;
    this.id = id;
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
    StyleRange[] new_originalStyle = null;
    if( originalStyle != null )
    {
      new_originalStyle = originalStyle.clone();
    }
    StyleRange[] new_mouseOverStyle = null;
    if( mouseOverStyle != null )
    {
      new_mouseOverStyle = mouseOverStyle.clone();
    }
    StyleRange[] new_mouseOverCtrlStyle = null;
    if( mouseOverCtrlStyle != null )
    {
      new_mouseOverCtrlStyle = mouseOverCtrlStyle.clone();
    }
    PartitionData pd = new PartitionData(start,length,context,id,
        new_originalStyle,new_mouseOverStyle,new_mouseOverCtrlStyle,
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
    String res = "{"+context+","+String.valueOf(id)+","
    +String.valueOf(start)+","+String.valueOf(length)+"}";
    return res;
  }
  
  public static boolean equal(PartitionData pd1, PartitionData pd2)
  {
    if( pd1 == null && pd2 == null )
      return true;
    if( pd1 == null && pd2 != null )
      return false;
    if( pd1 != null && pd2 == null ) 
      return false;
    //now both are not null
    if( pd1.start != pd2.start )
      return false;
    if( pd1.length != pd2.length )
      return false;
    if( pd1.context != null && !pd1.context.equals(pd2.context))
      return false;
    if( pd1.context == null && pd2.context != null )
      return false;
    
    return true;
  }
  
}
