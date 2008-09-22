/**
 * 
 */
package org.lispdev.views.repl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
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
   * or last (if in read-only mode) edit region.
   */
  private PartitionData editPartition;
  private HashMap<PartitionData,Position> readOnlyPositions;
  private String READ_ONLY_CATEGORY = "repl.read.only.position.category";
  

  final TextViewerUndoManager undoManager;

  /**
   * @return Text in current edit region, or <code>null</code> if in read-only
   *         mode
   */
  public String getEditText()
  {
    logTraceEntry("getEditText","",7);
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
    logTraceReturn("getEditText",res,7);
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
      return -1;

    // process boundary cases
    if(partitionRegistry == null || partitionRegistry.get(from).start > offset
        || partitionRegistry.get(to).start + partitionRegistry.get(to).length < offset)
    {
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
    logTraceEntry("getPartitionAt",String.valueOf(offset),5);
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
    logTraceReturn("getPartitionAt",String.valueOf(res),5);
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
            logErr("getCurrentEditPartition: pos == null");
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
    logTraceEntry("getText",String.valueOf(p),7);    
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
    logTraceReturn("getText",res,7);
    return res;
  }

  public Repl(Composite parent, IVerticalRuler ruler, int styles)
  {
    super(parent, ruler, null, false, styles);
    setEditable(true);
    config = new ReplConfiguration(this);
    configure(config);

    doc = new Document();
    partitionRegistry = new ArrayList<PartitionData>();
    // LispDocumentProvider.connectPartitioner(doc);
    setDocument(doc, new AnnotationModel());
    showAnnotations(false);
    showAnnotationsOverview(false);
    readOnlyPositions = new HashMap<PartitionData,Position>();
    undoManager = new TextViewerUndoManager(MAX_UNDO);
    iniUndoManager();
    disconnectUndoManager();
    setEditModeFlag(false);
    setEditOffset(0);
    doc.addPositionUpdater(new DefaultPositionUpdater(READ_ONLY_CATEGORY));
    appendVerifyKeyListener(new VerifyKeyListener()/*ReadOnlyBackspaceDel(this)*/
    {
      public void verifyKey(VerifyEvent event)
      {
        int offset = getTextWidget().getCaretOffset();
        if( !getEditModeFlag() || offset < getEditOffset())
        {
          return;
        }
        if( event.keyCode == SWT.DEL )
        {
          PartitionData pd = getReadOnlyPartition(offset, Repl.AFTER);
          if( pd != null )
          {
            deletePartInEdit(pd);
            event.doit = false;
          }
          return;
        }
        if( event.keyCode == SWT.BS )
        {
          PartitionData pd = getReadOnlyPartition(offset, Repl.BEFORE);
          if( pd != null )
          {
            deletePartInEdit(pd);
            event.doit = false;
          }
          return;
        }
        return;        
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

    StyledText styledText = getTextWidget();
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
        if(isUndoKeyPress(e))
        {
          doOperation(ITextOperationTarget.UNDO);
        }
        else if(isRedoKeyPress(e))
        {
          doOperation(ITextOperationTarget.REDO);
        }
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

      public void keyReleased(KeyEvent e)
      {
        // do nothing
      }
    });
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
   * @param prompt - prompt string. 
   * If you want it to be on new line precede with "\n"
   * @param promptContext - string identifying prompt context
   * @param id - number that can be used to identify this particular prompt
   * @param promptStyle - style ranges specifying how prompt should be printed
   * @param onNewLine - if true starts prompt on new line
   * 
   * @notes  After calling this function don't forget to move
   * caret to end of document by calling:
   * repl.getTextWidget().setCaretOffset(repl.getDocument().getLength());
   */
  public void startEdit(String prompt, String promptContext, int id,
      StyleRange[] promptStyle, boolean onNewLine)
  {
    logTraceEntry("startEdit","\""+prompt+"\",\""+promptContext
        +"\","+String.valueOf(promptStyle),7);
    if( getEditModeFlag() )
    {
      logTrace("startEdit: called in edit mode",7);
      stopEdit();
    }
    appendText(prompt, promptContext, id, promptStyle, onNewLine);
    connectUndoManager();
    setEditModeFlag(true);
    editPartition = new PartitionData(getEditOffset(),0,
        promptContext+"."+EDIT_CONTEXT,id);
    logTrace("startEdit: Start edit mode at offset = " + String.valueOf(getEditOffset())
        + ", with prompt \"" + prompt + "\", and context \"" + promptContext
        + "\"",4);
    logTraceReturn("startEdit","",7);
  }


  /**
   * Prints prompt, creates and then switches to edit mode. If Repl is in edit
   * mode, this operation ends current edit session (by calling 
   * <code>stopEdit()</code>) and starts new edit session.
   * Prompt is printed using given style parameters. 
   * 
   * @param prompt - text to print
   * @param promptContext - context for the prompt
   * @param id - numeric identifier of the prompt
   * @param foreground - foreground color, <code>null</code> if none
   * @param background - background color, <code>null</code> if none
   * @param fontStyle - font style of the style, may be 
   * <code>SWT.NORMAL</code>, <code>SWT.ITALIC</code> or <code>SWT.BOLD</code>
   * @param onNewLine - if true, prompt is printed on new line
   */
  public void startEdit(String prompt, String promptContext, int id,
      Color foreground, Color background, int fontStyle, boolean onNewLine)
  {
    logTraceEntry("startEdit","\""+prompt+"\",\""+promptContext
        +"\","+String.valueOf(foreground)+","+String.valueOf(background)
        +","+String.valueOf(fontStyle)+","+String.valueOf(onNewLine),5);
    String pr = prompt;
    startEdit(pr,promptContext, id,
        new StyleRange[]{new StyleRange(0,pr.length(),foreground,
            background,fontStyle)},onNewLine);
    getTextWidget().setCaretOffset(getDocument().getLength());
    logTraceEntry("startEdit","",7);
  }
  
  /**
   * Switch to read-only mode.
   */
  public void stopEdit()
  {
    logTraceEntry("stopEdit","",7);
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
    logTraceReturn("stopEdit","",7);
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
    logTraceEntry("appendText","\""+str+"\","+String.valueOf(data)
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
          if(str.length() > 70)
          {
            traceMsg += ", printing:\n====\n" + str
                + "\n===\n";
          }
          else
          {
            traceMsg += ", printing \"" + str + "\"";
          }
        }
        if( onNewLine )
        {
          PartitionData pd = new PartitionData(offset,"\n".length(),
              NEW_LINE_CONTEXT,0);
          partitionRegistry.add(pd);
          doc.replace(offset, 0, "\n");
          ++offset;
        }
        PartitionData pd;
        if(data == null)
        {
          pd = new PartitionData(offset,str.length(), NULL_CONTEXT,0);
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
    logTraceReturn("appendText","",7);
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
  public void appendText(String str, String context, int id,
      StyleRange[] styles, boolean onNewLine)
  {
    logTraceEntry("appendText","\""+str+"\",\""+context+"\","
        +String.valueOf(styles),7);
    appendText(str, new PartitionData(0, str.length(), context, id, styles),
        onNewLine);
    logTraceReturn("appendText","",7);
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
    pdnew.start = insoffset - getEditOffset(); //relative to start of editRegion
    //apply styles
    applyPartitionStyles(getEditOffset(),pdnew);
    //add the partition to children
    if( editPartition.children == null )
    {
      editPartition.children = new ArrayList<PartitionData>();
    }
    editPartition.children.add(pdnew);
    //add read only markers to document
    Position pos = new Position(insoffset,txt.length());
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
    readOnlyPositions.put(pdnew, pos);
    getTextWidget().setCaretOffset(insoffset+pdnew.length);
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
      doc.replace(pos.getOffset(), pd.length, "");
    }
    catch(BadLocationException e)
    {
      logException("deletePartInEdit: could not delete text",e);
    }
    editPartition.children.remove(pd);
  }
  
  
  /**
   * clears all text, leaves editing mode unchanged
   */
  public void clear()
  {
    logTraceEntry("clear","",7);
    
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
    disconnectUndoManager();
    if(getEditModeFlag())
    {
      connectUndoManager();
    }
    setEditOffset(0);
    try
    {
      doc.replace(0, doc.getLength(), "");
    }
    catch(BadLocationException e)
    {
      logException("clear: should never end up here",e);
    }
    logTraceReturn("clear","",7);
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

    if(infoListeners == null)
      System.out.print("Repl info:\n  " + msg + "\n");
    else
    {
      for(ILogListener ll : infoListeners)
      {
        ll.log(msg);
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
    if(warnListeners == null)
      System.err.print("Repl warning:\n  " + msg + "\n");
    else
    {
      for(ILogListener ll : warnListeners)
      {
        ll.log(msg);
      }
    }
  }

  /**
   * Log error message. Used internally. Notifies log listeners.
   * 
   * @param msg
   *          Message to log.
   */
  public void logErr(String msg)
  {
    if(quietErr)
      return;
    if(errListeners == null)
      System.err.print("Repl error:\n  " + msg + "\n");
    else
    {
      for(ILogListener ll : errListeners)
      {
        ll.log(msg);
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
    if(traceListeners == null)
      System.out.print("Repl trace:\n  " + msg + "\n");
    else
    {
      for(ILogListener ll : traceListeners)
      {
        ll.log(msg);
      }
    }
  }
  
  /**
   * Convenience method to trace function entry.
   * @param name
   * @param params
   */
  public void logTraceEntry(String name, String params, int lvl)
  {
    logTrace("->"+name+"("+params+")",lvl);
  }

  /**
   * Convenience method to trace function return.
   * @param name
   * @param ret
   */
  public void logTraceReturn(String name, String ret, int lvl)
  {
    logTrace("<-"+name+"("+ret+")",lvl);
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
    if(eListeners == null)
    {
      System.err.print("Repl exception:\n  " + msg + "\n");
      e.printStackTrace();
    }
    else
    {
      for(ILogExceptionListener ll : eListeners)
      {
        ll.log(msg, e);
      }
    }
  }

}
