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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.text.Position;

/**
 * Console-like viewer with facilities to implement lisp repl similar to
 * slime-repl.
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
  private boolean inEditMode = false;
  private IDocument doc;
  /**
   * Start of editable part in edit mode. Undefined in read-only mode.
   */
  private int editOffset;
  /**
   * Last edit context = Last prompt context + "." + this string
   */
  private final static String EDIT_CONTEXT = "_edit_context__";

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
    if(inEditMode)
    {
      try
      {
        res = doc.get(editOffset, doc.getLength() - editOffset);
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
   *       <code>generateLastEditPartition</code>. The following algorithm is
   *       used (where <code>offset[i]</code> - is offset of 
   *       <code>i</code>'th partition):<br>
   *       - Ensure that <code>offset[from] < offset < offset[to]</code><br>
   *       - If <code>to == from + 1</code> return <code>from</code><br>
   *       - If <code>offset > offset[(from + to)/2]</code>, set 
   *       <code>from = (from + to)/2</code>,<br>
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
  private int getPartitionPosition(int offset, int from, int to)
  {
    if(doc == null || offset < 0 || offset > doc.getLength())
      return -1;

    if(partitionRegistry == null || partitionRegistry.get(from).start > offset
        || partitionRegistry.get(to).start < offset)
    {
      return -1;
    }

    if(partitionRegistry.get(from).start == offset)
      return from;

    if(partitionRegistry.get(to).start == offset)
      return to;

    if(from + 1 == to)
    {
      return from;
    }

    int inew = (from + to) / 2;
    if(offset >= partitionRegistry.get(inew).start)
    {
      return getPartitionPosition(offset, inew, to);
    }
    else
    {
      return getPartitionPosition(offset, from, inew);
    }
  }
  
  /**
   * @return partition containing <code>offset</code>
   */
  public PartitionData getPartitionAt(int offset)
  {
    logTraceEntry("getPartitionAt",String.valueOf(offset),5);
    PartitionData res;
    if(offset >= editOffset)
    {
      logTrace("getPartitionAt: editPartition",7);
      res = getCurrentEditPartition();
    }
    else if(partitionRegistry == null)
    {
      logWarning("getPartitionAt: empty registry");
      res = null;
    }
    else
    {
      int i = getPartitionPosition(offset, 0, partitionRegistry.size() - 1);
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
    if( inEditMode )
    {
      editPartition.length = doc.getLength() - editOffset;
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

  /**
   * @return starting offset of editing region
   */
  public int getEditOffset()
  {
    return editOffset;
  }

  /**
   * @return <code>true</code> if in edit mode, or <code>false</code> otherwise
   */
  public boolean isInEditMode()
  {
    return inEditMode;
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
   * Prints prompt and then switches to edit mode. If Repl is in edit mode,
   * this operation ends current edit session (by calling 
   * <code>stopEdit()</code>) and starts new edit session.
   * @param prompt - prompt string. 
   * If you want it to be on new line precede with "\n"
   * @param promptContext - string identifying prompt context
   * @param promptStyle - style ranges specifying how prompt should be printed
   * 
   * @notes  After calling this function don't forget to move
   * caret to end of document by calling:
   * repl.getTextWidget().setCaretOffset(repl.getDocument().getLength());
   */
  public void startEdit(String prompt, String promptContext,
      StyleRange[] promptStyle)
  {
    logTraceEntry("startEdit","\""+prompt+"\",\""+promptContext
        +"\","+String.valueOf(promptStyle),7);
    if( inEditMode )
    {
      logTrace("startEdit: called in edit mode",7);
      stopEdit();
    }
    appendText(prompt, promptContext, promptStyle);
    connectUndoManager();
    inEditMode = true;
    editPartition = new PartitionData(editOffset,0,
        promptContext+"."+EDIT_CONTEXT);
    logTrace("startEdit: Start edit mode at offset = " + String.valueOf(editOffset)
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
   * @param foreground - foreground color, <code>null</code> if none
   * @param background - background color, <code>null</code> if none
   * @param fontStyle - font style of the style, may be 
   * <code>SWT.NORMAL</code>, <code>SWT.ITALIC</code> or <code>SWT.BOLD</code>
   * @param onNewLine - if true, prompt is printed on new line
   */
  public void startEdit(String prompt, String promptContext,
      Color foreground, Color background, int fontStyle, boolean onNewLine)
  {
    logTraceEntry("startEdit","\""+prompt+"\",\""+promptContext
        +"\","+String.valueOf(foreground)+","+String.valueOf(background)
        +","+String.valueOf(fontStyle)+","+String.valueOf(onNewLine),5);
    String pr = prompt;
    if(onNewLine)
    {
      pr = "\n"+pr;
    }
    startEdit(pr,promptContext,
        new StyleRange[]{new StyleRange(0,pr.length(),foreground,
            background,fontStyle)});
    getTextWidget().setCaretOffset(getDocument().getLength());
    logTraceEntry("startEdit","",7);
  }
  
  /**
   * Switch to read-only mode.
   */
  public void stopEdit()
  {
    logTraceEntry("stopEdit","",7);
    if( inEditMode )
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
      readOnlyPositions.clear();
      disconnectUndoManager();
      partitionRegistry.add(getCurrentEditPartition());
      inEditMode = false;
      editOffset = doc.getLength();
      logTrace("stopEdit: stop edit mode at offset = " 
          + String.valueOf(editOffset),5);      
    }
    else
    {
      logTrace("stopEdit: called stopEdit in read-only mode",5);
    }
    logTraceReturn("stopEdit","",7);
  }
  
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
  public void appendText(String str, PartitionData data)
  {
    logTraceEntry("appendText","\""+str+"\","+String.valueOf(data),7);
    stopEdit();
    if(doc != null)
    {
      String traceMsg = "";
      if(!quietTrace)
      {
        traceMsg = "appendText: at offset = " + String.valueOf(doc.getLength());
      }
      int offset = doc.getLength();
      if(data != null)
      {
        data.start = offset;
      }
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
        doc.replace(offset, 0, str);
        partitionRegistry.add(data);
        editOffset = doc.getLength();
        applyPartitionStyles(0,data);
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
   * @param styles - styles to format the text. Can be <code>null</code>
   */
  public void appendText(String str, String context, StyleRange[] styles)
  {
    logTraceEntry("appendText","\""+str+"\",\""+context+"\","
        +String.valueOf(styles),7);
    appendText(str, new PartitionData(0, str.length(), context, styles));
    logTraceReturn("appendText","",7);
  }

  /**
   * If <code>inEditMode</code> and <code>offset</code> is inside read-only
   * partition of current edit region, returns that partition.
   * Otherwise returns <code>null</code>
   * @param offset - checks for read-only partition here
   */
  private PartitionData getReadOnlyPartition(int offset)
  {
    if( offset < editOffset )
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
      if( pos.includes(offset) )
      {
        return pdc;
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
    if( !inEditMode || pd == null || txt == null || txt.length() != pd.length )
    {
      return;
    }
    //get actual position where to insert
    int insoffset = offset;
    if( offset < editOffset )
    {
      insoffset = editOffset;
    }
    else
    {
      PartitionData pdAtOffset = getReadOnlyPartition(insoffset);
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
    }
    //create new partition: cloning, since we don't want to conflict with
    //existing one when updating offsets
    PartitionData pdnew = pd.clone();
    pdnew.start = insoffset - editOffset; //relative to start of editRegion
    //apply styles
    applyPartitionStyles(editOffset,pd);
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
