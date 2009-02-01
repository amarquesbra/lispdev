/**
 * 
 */
package org.lispdev.views.repl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.jface.text.Position;

/**
 * <p>Console-like viewer with facilities to implement lisp repl similar to
 * slime-repl.</p>
 * 
 * <p><b>Usage:</b></p>
 * <b>TODO</b> Probably some API is open for other unintended ways.<br/>
 * API will be extended to accommodate functionality necessary for slime style repl.
 * <p><b>Logging facility:</b> add listeners, if no listener of particular type is
 * added then prints to console, traces have level, by setting appropriate
 * variable we can say if we want trace to print, or set clear not to print
 * (TODO: describe API better, but this is a minor point)</p>
 * 
 * <p>All text in Repl is related to a partition, which has context, id, etc.
 * It is possible to append text without context, etc., but then it is in null 
 * context</p>
 * 
 * <ul>
 * <li/> Repl starts empty in read-only mode
 * <li/> {@link #startEdit()} functions print prompt and switch Repl to edit mode.
 * All editing is done within prompt context. Only text after prompt can
 * be edited. It is possible to insert readOnly partitions in edit text
 * (for example copy from Repl history). Use {@link #insertPartInEdit()} function.
 * These partitions are read only and while editing behave like a single character
 * (i.e. del and backspace removes whole partition).
 * (TODO: also deal in similar way with copy-paste)
 * <li/> {@link #stopEdit()} stops editing mode, rendering all just edited mode 
 * read-only and collecting it in single partition. If called when Repl is
 * in read-only mode doesn't do anything.
 * <li/> {@link #appendText()} functions call stopEdit and then appends text
 *  with given partition data
 * <li/> undo works only in last editing region
 * <li/> {@link #clear()} clears all Repl history, and puts it in read-only mode 
 * (like when Repl starts)
 * <li/> {@link #getPartitionAt()} returns partition at particular offset
 * <li/> {@link #getEditText()} returns text that is currently being edited
 * <li/> {@link #getText()} returns text given partition
 * </ul>
 * <p><b>Key listeners:</b> API provides special key listeners that are attached
 * using standard appendVerifyKeyListener (prepend.. etc.) functions
 * (from parent ProjectionViewer).
 * Repl keyListener API facilitates a way of working with Repl partitions.
 * It consists of two components: triggers and listeners.</p>
 * <ul>
 * <li/> Triggers: {@code ReplInputTrigger} - abstract class that implements
 * VerifyKeyListener
 * <li/> Listeners: {@code IReplInputListener}
 * <li/> a trigger waits for key event for which it is registered and then calls
 * all {@code IReplInputListeners} that are registered with it
 * <li/> {@code ReplInputTrigger} is extended by specifying abstract {@code check}
 * method, which checks for key combination which fires this trigger
 * <li/> listeners are registered with trigger using function 
 * {@code ReplInputTrigger#addInputListener}. When trigger is fired, 
 * it calls all listeners in turn 
 * (TODO: should make listeners return boolean and stop at first true (handled))
 * <li/> One implementation of trigger is provided by {@code ReplEnterTrigger}
 * <li/> Example implementation of listener: {@code ReplEchoListener}
 * </ul>
 * 
 * <p><b>Example:</b></p>
 * <pre>

package org.lispdev.views;

import org.lispdev.views.repl.Repl;
import org.lispdev.views.repl.ReplEchoListener;
import org.lispdev.views.repl.ReplEnterTrigger;
import org.lispdev.views.repl.ReplInputTrigger;

import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class ReplView extends ViewPart
{
  public static final String ID = "test.replView";

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

    ReplInputTrigger it = new ReplEnterTrigger(repl,SWT.NONE,Repl.BEFORE);
    ReplEchoListener echo = new ReplEchoListener(repl);
    it.addInputListener(echo);
    repl.appendVerifyKeyListener(it);
    repl.startEdit("start>", "this prompt",0,
        new StyleRange[]{new StyleRange(0, "start>".length(),
            null, null, SWT.BOLD)},false);

  }

  @Override
  public void setFocus()
  {
  }

}
 * </pre>
 * 
 * 
 * @author sk
 * 
 */
public class Repl extends ProjectionViewer
{
  private ClearReplAction clearAction;
  private ReplConfiguration config;
  private int MAX_UNDO = 1000;
  /**
   * Set maximal number of undo operations. 
   * Becomes active in next edit mode session.
   */
  public void setMaxUndoLevels(int m)
  {
    MAX_UNDO = m;
  }
  
  /**
   * Edit mode flag. If true - can append text using keyboard. Otherwise can
   * append text only using appendText functions.
   */
  private boolean editFlag = false;
  private void setEditModeFlag(boolean inEditMode)
  {
    this.editFlag = inEditMode;
  }
  /**
   * @return <code>true</code> if in edit mode, or <code>false</code> otherwise
   */
  public boolean getEditModeFlag()
  {
    return editFlag;
  }

  private IDocument doc;
  /**
   * Start of editable part in edit mode. Undefined in read-only mode.
   */
  private int editOffset;
  private void setEditOffset(int offset)
  {
    editOffset = offset;
  }
  /**
   * @return starting offset of editing region
   */
  public int getEditOffset()
  {
    return editOffset;
  }
  
  
  private Prompt prompt;
  
  public void setPrompt(Prompt prompt)
  {
    this.prompt = prompt;
  }
  
  /**
   * Last edit context = Last prompt context + "." + this string
   */
  private final static String EDIT_CONTEXT = "_edit_context__";
  /**
   * Dummy new line context
   */
  private final static String NEW_LINE_CONTEXT = "_new_line_context__";
  /**
   * Null context (when data is not given)
   */
  private final static String NULL_CONTEXT = "_null_context__";

  /**
   * Registry that holds top level partitions.
   */
  private List<PartitionData> partitionRegistry;
  /**
   * Hold style and other information of current (if in edit mode),
   * or last (if in read-only mode) edit region. In edit mode
   * each time this partition is accessed all offsets
   * in this partition should be updated using {@code readOnlyPositions}
   * hash map.
   */
  private PartitionData editPartition;
  
  /**
   * Positions hold offsets associated with partition data.
   */
  private HashMap<PartitionData,Position> readOnlyPositions;
  
  private String printReadOnlyPositions()
  {
    String res = "{";
    if(readOnlyPositions != null)
    {
      for( PartitionData pd : readOnlyPositions.keySet() )
      {
        Position pos = readOnlyPositions.get(pd);
        res += "\n{"+pd+"= Pos {"+pos.offset+","+pos.length+","+pos.isDeleted()+"}";
      }
    }
    res += "}";
    return res;
  }
  
  private String READ_ONLY_CATEGORY = "repl.read.only.position.category";
  
  private Stack<DeletedPartitionData> deletedPartitions;

  final TextViewerUndoManager undoManager;

  /**
   * @return Text in current edit region, or <code>null</code> if in read-only
   *         mode
   */
  public String getEditText()
  {
    logTraceEntry("",7);
    String res = null;
    if(getEditModeFlag())
    {
      try
      {
        res = doc.get(getEditOffset(), doc.getLength() - getEditOffset());
      }
      catch(BadLocationException e)
      {
        logException("getEditText(): Failed", e);
      }
    }
    logTraceReturn(res,7);
    return res;
  }

  /**
   * Finds Position of a partition in <code>partitionRegistry</code>, which
   * contains <code>offset</code> and assuming that this partition is between
   * (and including) <code>from</code> and <code>to</code>.
   * 
   * @note This is a helper function for recursive algorithm in public function
   *       <code>getPartitionPosition</code>. The following algorithm is
   *       used (where <code>offset[i]</code> - is offset of 
   *       <code>i</code>'th partition):<br>
   *       - Ensure that <code>offset[from] < offset < offset[to]</code><br>
   *       - If <code>to == from + 1</code> return <code>from</code><br>
   *       - Define <code>midOffset = offset[(from + to)/2]</code><br>
   *       - If <code>offset == midOffset</code><br>
   *           - if <code>partitionResolutionFlag == NONE</code> return -1
   *           - if <code>partitionResolutionFlag == AFTER</code> 
   *             return (from + to)/2
   *           - else (when <code>partitionResolutionFlag == BEFORE</code>)
   *             return (from + to)/2 - 1
   *       - If <code>offset > offset[(from + to)/2]</code>, set 
   *         <code>from = (from + to)/2</code>,<br>
   *       - otherwise set <code>to = (from + to)/2</code>
   * 
   * @param offset -
   *          search for partition containing this offset
   * @param from -
   *          lower bound on index
   * @param to -
   *          upper bound on index
   * @return -1 if no <code>partition</code> found, or index in
   *         <code>partitionRegistry</code> otherwise
   */
  private int getPartitionPosition(int offset, int from, int to,
      int partitionResolutionFlag)
  {
    if(doc == null || offset < 0 || offset > doc.getLength())
    {
      logTrace("getPartitionPosition:Invalid offset",6);
      return -1;
    }

    // process boundary cases
    if(partitionRegistry == null )
    {
      logTrace("getPartitionPosition:partitionRegistry is empty",6);
      return -1;
    }
    if( partitionRegistry.get(from).start > offset )
    {
      logTrace("getPartitionPosition: offset = "+offset
          +" is outside of starting partition: "+ partitionRegistry.get(from).start,6);
      return -1;
    }
    if( partitionRegistry.get(to).start + partitionRegistry.get(to).length < offset)
    {
      logTrace("getPartitionPosition: offset = "+offset
          +" is outside of ending partition: "
          + partitionRegistry.get(to).start + partitionRegistry.get(to).length,6);
      return -1;
    }

    if(partitionRegistry.get(from).start == offset)
    {
      return resolveBoundary(from,partitionResolutionFlag);
    }
    
    if(partitionRegistry.get(to).start < offset)
    {
      return to;
    }

    if(partitionRegistry.get(to).start == offset)
    {
      return resolveBoundary(to,partitionResolutionFlag);
    }

    // we now know that offset[from] < offset < offset[to]
    if(from + 1 == to)
    {
      return from;
    }

    int inew = (from + to) / 2;
    int midOffset = partitionRegistry.get(inew).start;
    if(offset == midOffset)
    {
      return resolveBoundary(inew,partitionResolutionFlag);      
    }
    if(offset > partitionRegistry.get(inew).start)
    {
      return getPartitionPosition(offset, inew, to,
          partitionResolutionFlag);
    }
    else
    {
      return getPartitionPosition(offset, from, inew,
          partitionResolutionFlag);
    }
  }

  private int resolveBoundary(int i, int partitionResolutionFlag)
  {
    if( partitionResolutionFlag == NONE )
    {
      return -1;
    }
    if( partitionResolutionFlag == BEFORE )
    {
      if( i > 0 )
      {
        return i - 1;
      }
      else
      {
        return -1;
      }
    }
    //if( partitionResolutionFlag == AFTER )
    {
      return i;
    }
  }
  
  /**
   * Partition resolution flag signaling that if offset falls
   * between partitions, return null
   */
  static final public int NONE = 0;
  /**
   * Partition resolution flag signaling that if offset falls
   * between partitions, return the one that is before offset, or null if offset
   * corresponds to the beginning of the document
   */
  static final public int BEFORE = 1;
  /**
   * Partition resolution flag signaling that if offset falls
   * between partitions, return the one that is after offset, or null if offset
   * corresponds to the end of the document
   */
  static final public int AFTER = 2;
  
  /**
   * @return partition containing <code>offset</code>. When <code>offset</code>
   * falls right between two partition <code>partitionResolutionFlag</code>
   * is used: 
   * BEFORE - returns partition before offset, 
   * or null if in the beginning of document
   * AFTER - returns partition after offset, or null if in the end of document
   * NONE - returns null
   */
  public PartitionData getPartitionAt(int offset, int partitionResolutionFlag)
  {
    logTraceEntry(String.valueOf(offset),5);
    PartitionData res;
    // first check boundary conditions
    if( (partitionResolutionFlag == BEFORE || partitionResolutionFlag == NONE) &&
        0 == offset )
    {
      res = null;
    }
    else if( (partitionResolutionFlag == AFTER || partitionResolutionFlag == NONE) &&
        doc.getLength() == offset )
    {
      res = null;
    }
    else if(offset > getEditOffset())
    {
      logTrace("getPartitionAt: editPartition",7);
      res = getCurrentEditPartition();
    }
    else if( offset == getEditOffset() && partitionResolutionFlag == NONE )
    {
      res = null;
    }
    else if ( offset == getEditOffset() && partitionResolutionFlag == AFTER )
    {
      res = getCurrentEditPartition();
    }
    else if(partitionRegistry == null)
    {
      logWarning("getPartitionAt: empty registry");
      res = null;
    }
    // now we sure that 0 < offset < editOffset (in case of NONE and AFTER)
    // and 0 < offset <= editOffset (in case of BEFORE)
    else
    {
      int i = getPartitionPosition(offset, 0, partitionRegistry.size() - 1,
          partitionResolutionFlag);
      if(i < 0)
      {
        logWarning("getPartitionAt: no partition at "+ String.valueOf(offset));
        res = null;
      }
      else
      {
        res = partitionRegistry.get(i);
        logTrace("getPartitionAt: found partition",7);
      }
    }
    logTraceReturn(String.valueOf(res),5);
    return res;
  }

  /**
   * @return current edit partition data if <code>inEditMode = true</code>, or
   * <code>null</code> otherwise
   */
  private PartitionData getCurrentEditPartition()
  {
    if( getEditModeFlag() )
    {
      editPartition.length = doc.getLength() - getEditOffset();
      if( editPartition.children != null )
      {
        for( PartitionData pdc : editPartition.children )
        {
          Position pos = readOnlyPositions.get(pdc);
          if( pos == null )
          {
            logError("getCurrentEditPartition: pos == null");
          }
          else
          {
            pdc.start = pos.getOffset() - getEditOffset();            
          }
        }
      }
      return editPartition;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * @return text corresponding to partition <code>p</code>,
   * or <code>null</code> if partition is invalid.
   */
  public String getText(PartitionData p)
  {
    logTraceEntry(String.valueOf(p),7);    
    String res = null;
    if(p == null && doc == null)
    {
      logWarning("getText: cannot get text when p = "
          +String.valueOf(p)+", and doc = " + String.valueOf(doc));
    }
    else
    {
      try
      {
        res = doc.get(p.start, p.length);
      }
      catch(BadLocationException e)
      {
        logException("Could not get text for partition", e);
      }      
    }
    logTraceReturn(res,7);
    return res;
  }

  public ClearReplAction getClearAction()
  {
    return clearAction;
  }

  /**
   * Find how to extend selection (get new offsets for selection), 
   * so that it doesn't half cover any read-only.
   * @return new selection
   */
  public Point computeExpandedEditSelection()
  {
    final int tracelvl = 10;
    Point sel = getTextWidget().getSelection();
    logTrace("editOffset = "+getEditOffset(), tracelvl);
    logTrace("selection = "+sel.toString(),tracelvl);
    // 1. adjust x
    if( sel.x < getEditOffset() )
    {
      if( sel.y <= getEditOffset() )
      {
        return null;
      }
      else
      {
        sel.x = getEditOffset();
      }
    }
    else
    {
      PartitionData pdx = getReadOnlyPartition(sel.x, Repl.AFTER);
      if( pdx != null )
      {
        sel.x = getEditOffset() + pdx.start;
      }
    }
    
    // 2. adjust y
    if( sel.y <= sel.x )
    {
      sel.y = sel.x;
    }
    PartitionData pdy = getReadOnlyPartition(sel.y, Repl.BEFORE);
    if( pdy != null )
    {
      sel.y = getEditOffset() + pdy.start + pdy.length;
    }
    
    return sel;
  }

  /**
   * Given selection offset, create entry in deletedPartitions for corresponding
   * partitions in the selection and remove these partitions from
   * edit area.
   * @param selection region from which to delete partitions (it is assumed
   * that this selection is a result of {@link #computeExandedEditSelection()})
   */
  public void toDeletePartitions(Point selection)
  {
    if( selection == null || editPartition == null
        || editPartition.children == null )
    {
      return;
    }
    
    int x = selection.x - getEditOffset();
    int y = selection.y - getEditOffset();
    DeletedPartitionData dp = new DeletedPartitionData();
    dp.txt = getEditText().substring(x,y);
    dp.offset = x;
    dp.partitions = new ArrayList<PartitionData>();
    for( PartitionData pd : getCurrentEditPartition().children )
    {
      if( pd.start >= x && pd.start + pd.length <= y )
      {
        dp.partitions.add(pd);
        Position pos = readOnlyPositions.get(pd);
        try
        {
          doc.removePosition(READ_ONLY_CATEGORY, pos);
        }
        catch(BadPositionCategoryException e1)
        {
          logException("deletePartInEdit: could not delete position",e1);
        }
        readOnlyPositions.remove(pd);
      }
    }
    for( PartitionData pd : dp.partitions )
    {
      editPartition.children.remove(pd);
    }
    deletedPartitions.push(dp);
  }
  
  /**
   * Extends selection to be in edit region and cover read-only
   * partitions. Then delete text and partitions.
   * Change this function to do the following:
   * 1. Find how to extend selection (get new offsets for selection), 
   *    so that it doesn't half cover any read-only.
   * 2. Given selection offset, delete all read-only partitions in it
   * 3. Delete text.
   */
  public void deleteEditSelectionPartitions()
  {
    Point sel = computeExpandedEditSelection();
    if( sel == null )
    {
      return;
    }
    
    toDeletePartitions(sel);
    try
    {
      doc.replace(sel.x, sel.y - sel.x, "");
    }
    catch(BadLocationException e)
    {
      logException("Should not get here", e);
    }
    getTextWidget().setSelection(sel.x,sel.x);
  }

  public Repl(Composite parent, IVerticalRuler ruler, int styles)
  {
    super(parent, ruler, null, false, styles);
    setEditable(true);
    config = new ReplConfiguration(this);
    configure(config);

    clearAction = new ClearReplAction(this);
    
    doc = new Document();
    partitionRegistry = new ArrayList<PartitionData>();
    // LispDocumentProvider.connectPartitioner(doc);
    setDocument(doc, new AnnotationModel());
    showAnnotations(false);
    showAnnotationsOverview(false);
    readOnlyPositions = new HashMap<PartitionData,Position>();
    deletedPartitions = new Stack<DeletedPartitionData>();
    undoManager = new TextViewerUndoManager(MAX_UNDO);
    iniUndoManager();
    disconnectUndoManager();
    setEditModeFlag(false);
    setEditOffset(0);
    // context menu
    Menu menu = new Menu(getTextWidget());
    MenuItem clear = new MenuItem(menu,SWT.PUSH);
    clear.setText("Clear");
    clear.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        clearAction.run();
      }
    });
    getTextWidget().setMenu(menu);
    // end context menu

    doc.addPositionUpdater(new DefaultPositionUpdater(READ_ONLY_CATEGORY));
    appendVerifyKeyListener(new VerifyKeyListener()/*ReadOnlyBackspaceDel(this)*/
    {
      public void verifyKey(VerifyEvent event)
      {
        final int tracelvl = 1;
        if( !getEditModeFlag() )
        {
          event.doit = false;
          return;
        }
        logTrace(event.toString(),tracelvl);
        if( !(event.keyCode == SWT.DEL || event.keyCode == SWT.BS
            || event.character != '\0') )
        {
          logTrace("Not text changing",tracelvl);
          return;
        }
        if( isUndoKeyPress(event) || isRedoKeyPress(event) )
        {
          logTrace("Undo or redo",tracelvl);
          return;
        }
        Point sel = getTextWidget().getSelection();
        if( sel.x < getEditOffset() && sel.y < getEditOffset() )
        {
          return;
        }
        else if ( sel.x == sel.y )
        {
          if( event.keyCode == SWT.DEL )
          {
            PartitionData pd = getReadOnlyPartition(sel.x, Repl.AFTER);
            if( pd != null )
            {
              deletePartInEdit(pd);
              event.doit = false;
            }
            return;
          }
          else if( event.keyCode == SWT.BS )
          {
            if( sel.x == getEditOffset() )
            {
              event.doit = false;
              return;
            }
            PartitionData pd = getReadOnlyPartition(sel.x, Repl.BEFORE);
            if( pd != null )
            {
              deletePartInEdit(pd);
              event.doit = false;
            }
            return;
          }          
        }
        else
        {
          /*
          sel = computeExpandedEditSelection();
          toDeletePartitions(sel);
          getTextWidget().setSelection(sel);
          */          
        }        
      }
      
    });
  }

  /**
   * Initializes undo manager.
   */
  private void iniUndoManager()
  {
    // add listeners
    undoManager.connect(this);
    setUndoManager(undoManager);

    final StyledText styledText = getTextWidget();
    styledText.addVerifyListener(new VerifyListener()
    {
      public void verifyText(VerifyEvent e)
      {
        undoManager.endCompoundChange();
      }
    });

    styledText.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        final int tracelvl = 10;
        logTrace(e.toString(),tracelvl);
        logTrace("Selection = {"+getTextWidget().getSelection().toString()+"}",tracelvl);
        String before = null;
        if(isUndoKeyPress(e) && canDoOperation(ITextOperationTarget.UNDO))
        {
          logTrace("before undo - edit: "+editPartition,tracelvl);
          logTrace("before undo - readonly: "+printReadOnlyPositions(),tracelvl);
          before = getEditText();
          doOperation(ITextOperationTarget.UNDO);
          logTrace("after undo - edit: "+editPartition,tracelvl);
          logTrace("after undo - readonly: "+printReadOnlyPositions(),tracelvl);
        }
        else if(isRedoKeyPress(e) && canDoOperation(ITextOperationTarget.REDO))
        {
          logTrace("before redo - edit: "+editPartition,tracelvl);
          logTrace("before redo - readonly: "+printReadOnlyPositions(),tracelvl);
          before = getEditText();
          doOperation(ITextOperationTarget.REDO);
          logTrace("after redo - edit: "+editPartition,tracelvl);
          logTrace("after redo - readonly: "+printReadOnlyPositions(),tracelvl);
        }
        if(before != null)
        {
          String sel = styledText.getSelectionText();
          Point selection = styledText.getSelectionRange();
          logTrace("sel = "+sel,tracelvl);
          logTrace("selection = "+selection,tracelvl);
          if( sel.length() > 0 ) // restored text
          {
            DeletedPartitionData last = 
              deletedPartitions.peek();
            logTrace("last deleted partition = "+last,tracelvl);
            if( last.txt.equals(sel) 
                && selection.x == last.offset + getEditOffset() )
            {
              for( PartitionData pd : last.partitions )
              {
                pd.start -= getEditOffset();
                logTrace("creating read only partition: "+pd,tracelvl);
                createReadOnlyPartition(selection.x, selection.y, pd);                
              }
              deletedPartitions.pop();
            }
          }
          else // potentially removed text
          {
            String after = getEditText();
            int length = before.length() - after.length();
            logTrace("length = "+length,tracelvl);
            if( length > 0 ) //text was removed, check if need to delete partitions
            {
              int offset = selection.x - getEditOffset();
              String diff = before.substring(offset,offset + length);
              logTrace("offset = "+offset,tracelvl);
              logTrace("diff = "+diff,tracelvl);
              logTrace("editPartition = "+editPartition,tracelvl);
              logTrace("readOnlyPos = "+printReadOnlyPositions(),tracelvl);
              // find if there is inside the deleted text
              if( editPartition != null && editPartition.children != null
                  && editPartition.children.size() > 0 )
              {
                List<PartitionData> dp = new ArrayList<PartitionData>();
                for( PartitionData pd : getCurrentEditPartition().children )
                {
                  if( pd.start >= offset && pd.start < offset + diff.length() )
                  {
                    logTrace("pd = "+pd,tracelvl);
                    Position pos = readOnlyPositions.get(pd);
                    try
                    {
                      doc.removePosition(READ_ONLY_CATEGORY, pos);
                    }
                    catch(BadPositionCategoryException e1)
                    {
                      logException("deletePartInEdit: could not delete position",e1);
                    }
                    dp.add(pd);
                    readOnlyPositions.remove(pd);
                  }
                }
                for( PartitionData pd : dp )
                {
                  editPartition.children.remove(pd);                  
                }
                if( dp.size() > 0 )
                {
                  DeletedPartitionData dpd = new DeletedPartitionData();
                  dpd.offset = offset;
                  dpd.partitions = dp;
                  dpd.txt = diff;
                  deletedPartitions.push(dpd);
                }
              }
            }
          }
          logTrace("after processing - edit: "+editPartition,tracelvl);
          logTrace("after processing - readonly: "+printReadOnlyPositions(),tracelvl);
        }
      }

      public void keyReleased(KeyEvent e)
      {
        // do nothing
      }
    });
  }

  // TODO: should the combination be customizable?
  private boolean isUndoKeyPress(KeyEvent e)
  {
    // CTRL + z
    return ((e.stateMask & SWT.CONTROL) > 0)
        && ((e.keyCode == 'z') || (e.keyCode == 'Z'));
  }

  private boolean isRedoKeyPress(KeyEvent e)
  {
    // CTRL + y
    return ((e.stateMask & SWT.CONTROL) > 0)
        && ((e.keyCode == 'y') || (e.keyCode == 'Y'));
  }

  public void setCaret(int offset)
  {
    setSelectedRange(Math.min(offset, doc.getLength()), 0);
  }
  
  public void setCaretToEnd()
  {
    setCaret(doc.getLength());
  }
  
  /**
   * Handles disconnection of undo manager (to prevent collecting undo
   * operations when editor is not in editing mode)
   */
  private void disconnectUndoManager()
  {
    undoManager.reset();
    undoManager.disconnect();
  }

  /**
   * Handles connection to undo manager 
   */
  private void connectUndoManager()
  {
    undoManager.connect(this);
    undoManager.reset();
    undoManager.setMaximalUndoLevel(MAX_UNDO);
  }

  /**
   * Prints prompt and then switches to edit mode. If Repl is in edit mode,
   * this operation ends current edit session (by calling 
   * <code>stopEdit()</code>) and starts new edit session.
   * @param prompt - prompt data describing what to print and how to format. 
   */
  public void startEdit(Prompt prompt)
  {
    logTraceEntry(prompt.toString(),7);
    if( getEditModeFlag() )
    {
      logTrace("startEdit: called in edit mode",7);
      stopEdit();
    }
    appendText(prompt.prompt, prompt.partition.clone(), prompt.onNewLine);
    connectUndoManager();
    setEditModeFlag(true);
    editPartition = new PartitionData(getEditOffset(),0,
        prompt.partition.context+"."+EDIT_CONTEXT,prompt.partition.id);
    logTrace("startEdit: at offset = " + String.valueOf(getEditOffset())
        + ", with prompt \"" + prompt + "\", and context \"" 
        + prompt.partition.context
        + "\"",4);
    getTextWidget().setCaretOffset(getDocument().getLength());
    logTraceReturn("",7);
  }
  
  public void startEdit()
  {
    startEdit(prompt);
  }

  /**
   * Checks that all text is partitioned
   * and partitions and styles do not overlap.
   * @return true if pass sanity check.
   */
  public boolean sanityCheck()
  {
    if( doc.getLength() == 0 )
    { 
      //empty document but some partitions
      if(partitionRegistry != null && partitionRegistry.size() > 0)
      {
        logError("Empty Repl, but nonempty partition list");
        return false;        
      }
      else
      {
        return true;        
      }
    }
    // document is non empty, but no partitions
    if(partitionRegistry == null || partitionRegistry.size() == 0)
    {
      //FIXME: assumes that always starts with prompt
      logError("Empty Repl, but no partitions.");
      return false;
    }
    int offset = 0;
    for( PartitionData pd : partitionRegistry )
    {
      if( pd.start != offset )
      {
        logError("Gap between partitions");
        return false;        
      }
      if( pd.length + pd.start > doc.getLength() )
      {
        logError("Partition extends beyond document");
        return false;        
      }
      offset = pd.start + pd.length;
    }
    if( getEditModeFlag() )
    {
      if( offset != getEditOffset() )
      {
        logError("Gap between partition and edit region");
        return false;        
      }
    }
    else
    {
      if( offset != doc.getLength() )
      {
        logError("Unpartitioned part at the end of the document");
        return false;        
      }
    }
    if( !editSanityCheck() )
    {
      return false;
    }
    
    return true;
  }
  
  /**
   * Checks edit partition.
   * TODO: add check of deleted partitions
   * @return true if pass sanity check 
   */
  private boolean editSanityCheck()
  {
    if( getEditOffset() > doc.getLength() )
    {
      logError("Edit region is beyond Repl length");
      return false;
    }
    if( !getEditModeFlag() ) // in read only mode
    {
      if (doc.containsPositionCategory(READ_ONLY_CATEGORY))
      {
        logError("In read-only mode Repl should not have any read-only positions");
        return false;
      }
      if( readOnlyPositions.size() > 0 )
      {
        logError("In read-only mode there should be no read-only positions");
        return false;
      }
      if ( editPartition != null && editPartition.children != null
          && editPartition.children.size() > 0 )
      {
        logError("In read-only mode editPartition should not " +
        		"contain read-only partitions");
        return false;
      }
      return true;
    }
    else // in edit mode
    {
      if( doc.getLength() == getEditOffset() ) // empty edit partition
      {
        if (doc.containsPositionCategory(READ_ONLY_CATEGORY))
        {
          logError("Empty edit region, but nonempty position category");
          return false;
        }
        if( readOnlyPositions.size() > 0 )
        {
          logError("Empty edit region, but have some read-only positions");
          return false;
        }
        if ( editPartition != null && editPartition.children != null 
            && editPartition.children.size() > 0 )
        {
          logError("Empty edit region, but have read-only partitions");
          return false;
        }
        return true;
      }
      else // nonempty edit partition 
      {
        int npart = 0;
        int npos = 0;
        int ndocpos = 0;
        if( editPartition != null && editPartition.children != null )
        {
          npart = editPartition.children.size();
        }
        if( doc.containsPositionCategory(READ_ONLY_CATEGORY) )
        {
          try
          {
            ndocpos = doc.getPositions(READ_ONLY_CATEGORY).length;
          }
          catch(BadPositionCategoryException e)
          {
            logException("Bad category: should never get here", e);
          }
        }
        if( readOnlyPositions != null )
        {
          npos = readOnlyPositions.size();
        }
        if( npart != npos || npart != ndocpos )
        {
          logError("Number of read-only partitions should be same " +
          		"as number of positions");
        }
        
        if( npart > 0 )
        {
          int totLength = 0;
          for( PartitionData pd : editPartition.children )
          {
            totLength += pd.length;
            Position pos = readOnlyPositions.get(pd);
            if( pos == null )
            {
              logError("No position for read-only partition");
              return false;
            }
            if( pos.isDeleted() )
            {
              logError("Position for read-only partition is marked as deleted");
              return false;
            }
            if( pos.length != pd.length )
            {
              logError("Position length != partition length");
              return false;
            }
            // check if any other partition overlaps this one
            for( Position p : readOnlyPositions.values() )
            {
              if( p != pos && p != null 
                  && p.overlapsWith(pos.offset, pos.length))
              {
                logError("Read only partitions overlap");
                return false;                
              }
            }
          }
          if( totLength > doc.getLength() - getEditOffset() )
          {
            logError("Total length of read only partitions > " +
            		"length of edit region in Repl");
            return false;            
          }
        }
      }
      
    }
    
    return true;
  }
  
  /**
   * Switch to read-only mode.
   */
  public void stopEdit()
  {
    logTraceEntry("",7);
    if( getEditModeFlag() )
    {
      try
      {
        if( doc.containsPositionCategory(READ_ONLY_CATEGORY) )
        {
          doc.removePositionCategory(READ_ONLY_CATEGORY);
        }
      }
      catch(BadPositionCategoryException e)
      {
        logException("stopEdit: should never get here...",e);
      }
      if( doc.getLength() > 0 )
      {
        partitionRegistry.add(getCurrentEditPartition());        
      }
      readOnlyPositions.clear();
      deletedPartitions.clear();
      disconnectUndoManager();
      setEditModeFlag(false);
      setEditOffset(doc.getLength());
      logTrace("stopEdit: stop edit mode at offset = " 
          + String.valueOf(getEditOffset()),5);      
    }
    else
    {
      logTrace("stopEdit: called stopEdit in read-only mode",5);
    }
    logTraceReturn("",7);
  }
  
  /**
   * Applies styles from <code>PartitionData</code>, assuming
   * that PartitionData is set relative to 
   * @param offset
   * @param pd
   */
  private void applyPartitionStyles(int offset, PartitionData pd)
  {
    if(doc == null || pd == null || pd.originalStyle == null 
        || pd.start + pd.length > doc.getLength())
    {
      return;
    }
    int pdoffset = offset + pd.start;
    for(StyleRange style : pd.originalStyle)
    {
      style.start += pdoffset;
      getTextWidget().setStyleRange(style);
      style.start -= pdoffset;
    }
    if( pd.children != null )
    {
      for(PartitionData pdc : pd.children)
      {
        applyPartitionStyles(pdoffset,pdc);
      }
    }
  }
  
  /**
   * Appends text and partition to repl.
   */
  public void appendText(String str, PartitionData data, boolean onNewLine)
  {
    logTraceEntry("\""+str+"\","+String.valueOf(data)
        +","+String.valueOf(onNewLine),7);
    stopEdit();
    if(doc != null)
    {
      String traceMsg = "";
      if(!quietTrace)
      {
        traceMsg = "appendText: at offset = " + String.valueOf(doc.getLength());
      }
      int offset = doc.getLength();
      try
      {
        if(!quietTrace)
        {
          if(str.length() > 80)
          {
            traceMsg += ", printing:\n" + str + "\n";
          }
          else
          {
            traceMsg += ", printing: " + str;
          }
        }
        if( onNewLine && doc.getLength() > 0 )
        {
          PartitionData pd = new PartitionData(offset,"\n".length(),
              NEW_LINE_CONTEXT,"0");
          partitionRegistry.add(pd);
          doc.replace(offset, 0, "\n");
          ++offset;
        }
        PartitionData pd;
        if(data == null)
        {
          pd = new PartitionData(offset,str.length(), NULL_CONTEXT,"0");
        }
        else
        {
          pd = data;
          pd.start = offset;
        }
        doc.replace(offset, 0, str);
        partitionRegistry.add(pd);
        setEditOffset(doc.getLength());
        applyPartitionStyles(0,pd);
      }
      catch(BadLocationException e)
      {
        if(!quietTrace)
          traceMsg += ", producing exception.";
        logException("Failed to print to Repl", e);
      }
      logTrace(traceMsg,4);
    }
    else
    {
      logWarning("appendText: Tried to print to uninitialized Repl");
    }
    logTraceReturn("",7);
  }

  /**
   * Appends text and creates partition data using <code>context</code> and
   * <code>styles</code>
   * @param str - text to append
   * @param context - context for appended text
   * @param id - id for appended text
   * @param styles - styles to format the text. Can be <code>null</code>
   * @param onNewLine - if true starts prompt on new line
   */
  public void appendText(String str, String context, String id,
      StyleRange[] styles, boolean onNewLine)
  {
    logTraceEntry("\""+str+"\",\""+context+"\","
        +String.valueOf(styles),7);
    appendText(str, new PartitionData(0, str.length(), context, id, styles),
        onNewLine);
    logTraceReturn("",7);
  }

  /**
   * If <code>inEditMode</code> and <code>offset</code> is inside read-only
   * partition of current edit region, returns that partition.
   * Otherwise returns <code>null</code>
   * @param offset - checks for read-only partition here
   * @param partitionResolutionFlag - NONE, BEFORE, AFTER
   * - this is used in functions internal to Repl, so should not be actually
   * public 
   */
  public PartitionData getReadOnlyPartition(int offset, 
      int partitionResolutionFlag)
  {
    if( offset < getEditOffset() )
    {
      return null;
    }
    PartitionData pd = getCurrentEditPartition();
    if( pd == null || pd.children == null )
    {
      return null;
    }
    for( PartitionData pdc : pd.children)
    {
      Position pos = readOnlyPositions.get(pdc);
      Assert.isTrue(pos != null && !pos.isDeleted());
      int offset0 = pos.getOffset();
      int offset1 = offset0 + pos.getLength();
      if( partitionResolutionFlag == NONE )
      {
        if(offset0 == offset || offset1 == offset)
        {
          return null;            
        }
        else if( offset0 < offset && offset < offset1 )
        {
          pdc.start = offset0 - getEditOffset();
          return pdc; 
        }
      }
      if( partitionResolutionFlag == BEFORE )
      {
        if( offset0 == offset )
        {
          return null;
        } else if( offset0 < offset && offset <= offset1 )
        {
          pdc.start = offset0 - getEditOffset();
          return pdc;
        }
      }
      if( partitionResolutionFlag == AFTER )
      {
        if( offset1 == offset )
        {
          return null;
        }
        else if(offset0 <= offset && offset < offset1 )
        {
          pdc.start = offset0 - getEditOffset();
          return pdc;
        }
      }
    }
    return null;
  }
  
  
  /**
   * Inserts partition into edit area (if in edit mode).
   * The partition is marked as read-only.
   * @param offset - where to put the partition. If <code>offset</code> is
   * not in edit region, the partition is inserted in the beginning of the
   * edit region. If in another read-only partition in edit region,
   * the partition is inserted after that partition. 
   * @param txt - underlying text for the partition
   * @param pd - partition to insert
   */
  public void insertPartInEdit(int offset, String txt, PartitionData pd)
  {
    if( !getEditModeFlag() || pd == null || txt == null || txt.length() != pd.length )
    {
      return;
    }
    //get actual position where to insert
    int insoffset = offset;
    if( offset < getEditOffset() )
    {
      insoffset = getEditOffset();
    }
    else
    {
      PartitionData pdAtOffset = getReadOnlyPartition(insoffset,NONE);
      if( pdAtOffset != null )
      {
        Position pos = readOnlyPositions.get(pdAtOffset);
        insoffset = pos.getOffset() + pos.getLength();
      }
    }
    //insert text
    try
    {
      doc.replace(insoffset, 0, txt);
    }
    catch(BadLocationException e)
    {
      logException("insertPartInEdit: could not insert text",e);
      return;
    }
    //create new partition: cloning, since we don't want to conflict with
    //existing one when updating offsets
    PartitionData pdnew = pd.clone();
    createReadOnlyPartition(insoffset, txt.length(), pdnew);
    getTextWidget().setCaretOffset(insoffset+pdnew.length);
  }
  
  private void createReadOnlyPartition(int offset, int length,
      PartitionData pd)
  {
    pd.start = offset - getEditOffset(); //relative to start of editRegion
    //apply styles
    applyPartitionStyles(getEditOffset(),pd);
    //add the partition to children
    if( editPartition.children == null )
    {
      editPartition.children = new ArrayList<PartitionData>();
    }
    editPartition.children.add(pd);
    //add read only markers to document
    Position pos = new Position(offset,length);
    try
    {
      if( !doc.containsPositionCategory(READ_ONLY_CATEGORY) )
      {
        doc.addPositionCategory(READ_ONLY_CATEGORY);          
      }
      doc.addPosition(READ_ONLY_CATEGORY, pos);
    }
    catch(BadLocationException e)
    {
      logException("insertPartEdit: could not insert position",e);
    }
    catch(BadPositionCategoryException e)
    {
      logException("insertPartEdit: could not insert position",e);
    }
    //finally connect markers with partition through HashMap
    readOnlyPositions.put(pd, pos);
  }
  
  private void deletePartInEdit(PartitionData pd)
  {
    if( !getEditModeFlag() || pd == null || !readOnlyPositions.containsKey(pd)
        || editPartition.children == null 
        || !editPartition.children.contains(pd))
    {
      return;
    }
    
    Position pos = readOnlyPositions.get(pd);
    pd.start = pos.offset;
    try
    {
      doc.removePosition(READ_ONLY_CATEGORY, pos);
    }
    catch(BadPositionCategoryException e)
    {
      logException("deletePartInEdit: could not delete position",e);
    }
    readOnlyPositions.remove(pd);
    try
    {
      String txt = doc.get(pd.start, pd.length);
      DeletedPartitionData dpd = new DeletedPartitionData();
      dpd.txt = txt;
      dpd.offset = pos.offset - getEditOffset();
      dpd.partitions = new ArrayList<PartitionData>();
      pd.start -= getEditOffset();
      dpd.partitions.add(pd);      
      deletedPartitions.push(dpd);
      doc.replace(pos.getOffset(), pd.length, "");
    }
    catch(BadLocationException e)
    {
      logException("deletePartInEdit: could not delete text",e);
    }
    editPartition.children.remove(pd);
  }
  
  
  /**
   * clears all text, and sets mode to read only
   */
  public void clear()
  {
    logTraceEntry("",7);
    
    stopEdit();
    try
    {
      if( doc.containsPositionCategory(READ_ONLY_CATEGORY) )
      {
        doc.removePositionCategory(READ_ONLY_CATEGORY);
      }
    }
    catch(BadPositionCategoryException e)
    {
      logException("clear: should never get here...",e);
    }
    partitionRegistry.clear();
    readOnlyPositions.clear();
    deletedPartitions.clear();
    disconnectUndoManager();
    setEditOffset(0);
    try
    {
      doc.replace(0, doc.getLength(), "");
    }
    catch(BadLocationException e)
    {
      logException("clear: should never end up here",e);
    }
    stopEdit();
    logTraceReturn("",7);
  }
  
  /*
   * ================= Trivial Logging facility ===================
   * 
   */

  private ArrayList<ILogListener> infoListeners;
  private ArrayList<ILogListener> warnListeners;
  private ArrayList<ILogListener> errListeners;
  private ArrayList<ILogListener> traceListeners;
  private ArrayList<ILogExceptionListener> eListeners;
  /**
   * Set this flag to false if you want to see Info messages.
   */
  public boolean quietInfo = false;
  /**
   * Set this flag to false if you want to see Warning messages.
   */
  public boolean quietWarn = false;
  /**
   * Set this flag to false if you want to see Error messages.
   */
  public boolean quietErr = false;
  /**
   * Set this flag to false if you want to see Trace messages.
   */
  public boolean quietTrace = false;
  /**
   *  to make number of trace messages smaller, reduce this number
   */
  public int trace_level = 5;

  /**
   * Add listener for Info log messages
   */
  public void addLogInfoListener(ILogListener log)
  {
    logTrace("addLogInfoListener: " + String.valueOf(log),10);
    if(infoListeners == null)
      infoListeners = new ArrayList<ILogListener>();
    infoListeners.add(log);
  }

  /**
   * Add listener for Warning log messages
   */
  public void addLogWarnListener(ILogListener log)
  {
    logTrace("addLogWarnListener: " + String.valueOf(log),10);
    if(warnListeners == null)
      warnListeners = new ArrayList<ILogListener>();
    warnListeners.add(log);
  }

  /**
   * Add listener for Error log messages
   */
  public void addLogErrListener(ILogListener log)
  {
    logTrace("addLogErrListener: " + String.valueOf(log),10);
    if(errListeners == null)
      errListeners = new ArrayList<ILogListener>();
    errListeners.add(log);
  }

  /**
   * Add listener for Trace log messages
   */
  public void addLogTraceListener(ILogListener log)
  {
    logTrace("addLogTraceListener: " + String.valueOf(log),10);
    if(traceListeners == null)
      traceListeners = new ArrayList<ILogListener>();
    traceListeners.add(log);
  }

  /**
   * Add listener for Exception log messages
   */
  public void addLogExceptionListener(ILogExceptionListener log)
  {
    logTrace("addLogExceptionListener: " + String.valueOf(log),10);
    if(eListeners == null)
      eListeners = new ArrayList<ILogExceptionListener>();
    eListeners.add(log);
  }
  
  /*
  private static final String DATE_FORMAT_NOW = "MM-dd HH:mm:ss:ms";

  private static String now() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    return sdf.format(cal.getTime());
  }
  */

  private String getLocation()
  {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
    return "("+ste.getFileName()+":"
        +String.valueOf(ste.getLineNumber())+")";    
  }
  
  private String methodName()
  {
    return Thread.currentThread().getStackTrace()[3].getMethodName();
  }
  
  /**
   * Log info message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   */
  public void logInfo(String msg)
  {
    if(quietInfo)
      return;
    String txt = getLocation() + " "+msg;
    if(infoListeners == null)
      System.out.print(txt + "\n");
    else
    {
      for(ILogListener ll : infoListeners)
      {
        ll.log(txt);
      }
    }
  }

  /**
   * Log warning message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   */
  public void logWarning(String msg)
  {
    if(quietWarn)
      return;
    String txt = getLocation() + " "+msg;
    if(warnListeners == null)
      System.err.print(txt + "\n");
    else
    {
      for(ILogListener ll : warnListeners)
      {
        ll.log(txt);
      }
    }
  }

  /**
   * Log error message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   */
  public void logError(String msg)
  {
    if(quietErr)
      return;
    String txt = getLocation() + " "+msg;
    if(errListeners == null)
      System.err.print(txt + "\n");
    else
    {
      for(ILogListener ll : errListeners)
      {
        ll.log(txt);
      }
    }
  }

  /**
   * Log trace message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   */
  public void logTrace(String msg, int lvl)
  {
    if(quietTrace || lvl >= trace_level)
      return;
    String txt = getLocation() + " "+msg;
    if(traceListeners == null)
      System.out.print(txt + "\n");
    else
    {
      for(ILogListener ll : traceListeners)
      {
        ll.log(txt);
      }
    }
  }
  
  /**
   * Convenience method to trace function entry.
   * @param params
   */
  public void logTraceEntry(String params, int lvl)
  {
    if(quietTrace || lvl >= trace_level)
      return;
    String txt = getLocation() + " ->"+methodName()+"("+params+")";
    if(traceListeners == null)
      System.out.print(txt + "\n");
    else
    {
      for(ILogListener ll : traceListeners)
      {
        ll.log(txt);
      }
    }
  }

  /**
   * Convenience method to trace function return.
   * @param name
   * @param ret
   */
  public void logTraceReturn(String ret, int lvl)
  {
    if(quietTrace || lvl >= trace_level)
      return;
    String txt = getLocation() + " <-"+methodName()+"("+ret+")";
    if(traceListeners == null)
      System.out.print(txt + "\n");
    else
    {
      for(ILogListener ll : traceListeners)
      {
        ll.log(txt);
      }
    }
  }

  /**
   * Log exception message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   * @param e
   *          Exception object to log.
   */
  public void logException(String msg, Throwable e)
  {
    String txt = getLocation() + " "+msg;
    if(eListeners == null)
    {
      System.err.print(txt + "\n");
      e.printStackTrace();
    }
    else
    {
      for(ILogExceptionListener ll : eListeners)
      {
        ll.log(txt, e);
      }
    }
  }

}
