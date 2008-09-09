/**
 * 
 */
package org.lispdev.views.repl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 * @author sk
 *
 *make it abstract
 */
public class Repl extends SourceViewer
{
  private ArrayList<ILogListener> infoListeners;
  private ArrayList<ILogListener> warnListeners;
  private ArrayList<ILogListener> errListeners;
  private ArrayList<ILogListener> traceListeners;
  private ArrayList<ILogExceptionListener> eListeners;
  public boolean quietInfo = false;
  public boolean quietWarn = false;
  public boolean quietErr = false;
  public boolean quietTrace = false;
  
  private ReplConfiguration config;
  private boolean inEditMode = false;
  private IDocument doc;
  private int editOffset; //editable part is after this offset
  private final static String LAST_EDIT_CONTEXT = "__last_edit_context__";
  
  // it is going to grow in increasing order
  private List<PartitionData> partitionRegistry; 

  final TextViewerUndoManager undoManager;
  
  public void addLogInfoListener(ILogListener log)
  {
    logTrace("Added info listener:" + log.toString());
    if(infoListeners == null) infoListeners = new ArrayList<ILogListener>();
    infoListeners.add(log);
  }
  public void addLogWarnListener(ILogListener log)
  {
    logTrace("Added warn listener:" + log.toString());
    if(warnListeners == null) warnListeners = new ArrayList<ILogListener>();
    warnListeners.add(log);
  }
  public void addLogErrListener(ILogListener log)
  {
    logTrace("Added err listener:" + log.toString());
    if(errListeners == null) errListeners = new ArrayList<ILogListener>();
    errListeners.add(log);
  }
  public void addLogTraceListener(ILogListener log)
  {
    logTrace("Added trace listener:" + log.toString());
    if(traceListeners == null) traceListeners = new ArrayList<ILogListener>();
    traceListeners.add(log);
  }
  public void addLogExceptionListener(ILogExceptionListener log)
  {
    logTrace("Added exception listener:" + log.toString());
    if(eListeners == null) eListeners = new ArrayList<ILogExceptionListener>();
    eListeners.add(log);
  }
  
  public void logInfo(String msg)
  {
    if(quietInfo) return;
    
    if(infoListeners == null)
      System.out.print("<Repl info>: "+msg+"\n");
    else
    {
      for(ILogListener ll : warnListeners)
      {
        ll.log(msg);
      }
    }
  }
  public void logWarning(String msg)
  {
    if(quietWarn) return;
    if(warnListeners == null)
      System.err.print("<Repl warning>: "+msg+"\n");
    else
    {
      for(ILogListener ll : warnListeners)
      {
        ll.log(msg);
      }
    }
  }
  public void logErr(String msg)
  {
    if(quietErr) return;
    if(errListeners == null)
      System.err.print("<Repl error>: "+msg+"\n");
    else
    {
      for(ILogListener ll : errListeners)
      {
        ll.log(msg);
      }
    }
  }
  public void logTrace(String msg)
  {
    if(quietTrace) return;
    if(traceListeners == null)
      System.out.print("<Repl trace>\n"+msg+"\n</Repl trace>\n");
    else
    {
      for(ILogListener ll : traceListeners)
      {
        ll.log(msg);
      }
    }
  }
  public void logException(String msg, Throwable e)
  {
    if(warnListeners == null)
    {
      System.err.print("<Repl exception>: "+msg+"\n");
      e.printStackTrace();
    }
    else
    {
      for(ILogListener ll : warnListeners)
      {
        ll.log(msg);
      }
    }
  }
  
  public String getEditText()
  {
    String res = "";
    try
    {
      res = doc.get(editOffset, doc.getLength()-editOffset);
    }
    catch(BadLocationException e)
    {
      logException("Failed to get edit text", e);
    }
    return res;
  }
  
  // suppose have bounding indexes: offset[ilo] <= offset < offset[ihi]
  // if ihi = ilo + 1, return ilo
  // if: offset > offset[(ilo+ihi)/2], set ilo = (ilo+ihi)/2, else ihi = (ilo+ihi)/2
  private int getPartitionPosition(int offset, int ilo, int ihi)
  {
    if( doc == null || offset < 0 || offset > doc.getLength() )
      return -1;
    
    if( partitionRegistry == null || partitionRegistry.get(ilo).start > offset
        || partitionRegistry.get(ihi).start < offset )
    {
      return -1;
    }
    
   if( partitionRegistry.get(ilo).start == offset )
     return ilo;
   
   if( partitionRegistry.get(ilo).start == offset )
     return ihi;
    
    if( ilo + 1 == ihi )
    {
      return ilo;
    }

    int inew = (ilo+ihi)/2;
    if( offset >= partitionRegistry.get(inew).start )
    {
      return getPartitionPosition(offset,inew,ihi);
    }
    else
    {
      return getPartitionPosition(offset,ilo,inew);
    }
  }
  
  private PartitionData generateLastEditPartition()
  {
    return new PartitionData(editOffset,doc.getLength()-editOffset,LAST_EDIT_CONTEXT);    
  }
  
  public PartitionData getPartitionAt(int offset)
  {
    if( offset >= editOffset )
    {
      return generateLastEditPartition();
    }
    if( partitionRegistry == null )
    {
      return null;
    }
    logTrace("Finding partition for position "+String.valueOf(offset));
    int i = getPartitionPosition(offset,0,partitionRegistry.size()-1);
    if( i < 0 )
    {
      logTrace("Partition at position "+String.valueOf(offset)+" is not found");
      return null;
    }
    else
    {
      PartitionData pd = partitionRegistry.get(i);
      logTrace("Found partition "+pd.context+" at position "+String.valueOf(i));
      return pd;
    }
  }

  public String getText(PartitionData p)
  {
    String res = null;
    if( p == null || doc == null)
    {
      return res;
    }
    try
    {
      res = doc.get(p.start, p.length);
    }
    catch(BadLocationException e)
    {
      logException("Could not get text for partition",e);
    }
    return res;
  }
  
  public int getEditOffset()
  {
    return editOffset;
  }
  
  public boolean isInEditMode()
  {
    return inEditMode;
  }
  
  private void undoDisconnect()
  {
    undoManager.reset();
    undoManager.disconnect();
  }
  
  private void undoConnect()
  {
    undoManager.connect(this);
    undoManager.reset();
    undoManager.setMaximalUndoLevel(1000); //FIXME: make this into option
  }

  public Repl(Composite parent, IVerticalRuler ruler, int styles)
  {
    super(parent, ruler, styles);
    setEditable(true);
    config = new ReplConfiguration(this);
    configure(config);

    doc = new Document();
    partitionRegistry = new ArrayList<PartitionData>();
//  LispDocumentProvider.connectPartitioner(doc);
    setDocument(doc, new AnnotationModel());
    showAnnotations(false);
    showAnnotationsOverview(false);
    
    
    // undo manager
    
    undoManager = new TextViewerUndoManager(20000);
    // add listeners
    undoManager.connect(this);
    setUndoManager(undoManager);

    StyledText styledText = getTextWidget();
    styledText.addVerifyListener(new VerifyListener(){
        public void verifyText(VerifyEvent e){
         undoManager.endCompoundChange();
        }
     });
    
    styledText.addKeyListener(new KeyListener() {
     public void keyPressed(KeyEvent e) {
      if (isUndoKeyPress(e)) {
       doOperation(ITextOperationTarget.UNDO);
      } else if (isRedoKeyPress(e)) {
       doOperation(ITextOperationTarget.REDO);
      }
     }

     private boolean isUndoKeyPress(KeyEvent e) {
      // CTRL + z
      return ((e.stateMask & SWT.CONTROL) > 0)
        && ((e.keyCode == 'z') || (e.keyCode == 'Z'));
     }

     private boolean isRedoKeyPress(KeyEvent e) {
      // CTRL + y
      return ((e.stateMask & SWT.CONTROL) > 0)
        && ((e.keyCode == 'y') || (e.keyCode == 'Y'));
     }

     public void keyReleased(KeyEvent e) {
      // do nothing
     }
    });

  }

  // prints new line, prompt and then allow you edit repl
  public void startEdit(String prompt, String promptContext, StyleRange[] promptStyle)
  {
    appendText("\n"+prompt,promptContext,promptStyle);
    logTrace("Start edit mode at offset = "+String.valueOf(editOffset)
        +", with prompt \""+prompt+"\", and context \""+promptContext+"\"");
    undoConnect();
    inEditMode = true;
  }
  
  public void stopEdit()
  {
    undoDisconnect();
    partitionRegistry.add(generateLastEditPartition());
    inEditMode = false;
    editOffset = doc.getLength();
    logTrace("Stop edit mode at offset = "+String.valueOf(editOffset));
  }
  
  
  public void appendText(String str, String context, StyleRange[] styles)
  {
    appendText(str, new PartitionData(0,str.length(),context,styles));
  }
  
  
  // this function is used to print to repl
  public void appendText(String str, PartitionData data)
  {
    if(doc != null)
    {
      String traceMsg = "";
      if( !quietTrace )
      {
        traceMsg = "Printing at offset = " 
          + String.valueOf(doc.getLength());
      }
      boolean doSetStyles = true;
      int offset = doc.getLength();
      if(data != null)
      {
        data.start = offset;
      }
      // adjust style ranges by offset, also check that they are ok
      if(data == null || data.originalStyle == null)
      {
        if( !quietTrace )
          traceMsg += ", without styles";
        doSetStyles = false;
      }
      else
      {
        for(StyleRange st : data.originalStyle)
        {
          if( st.length > str.length() )
          {
            if( !quietTrace )
              traceMsg += ", without styles due to invalid style range";
            logWarning("Style range is outside of bounds");
            doSetStyles = false;
            break;
          }
          st.start += offset;
        }        
      }
      try
      {
        if( !quietTrace )
        {
          if(str.length() > 70)
          {
            traceMsg += ", printing:\n<Repl print content>\n"
              +str+"\n</Repl print content>\n";            
          }
          else
          {
            traceMsg += ", printing \""+str+"\"";
          }
        }
        doc.replace(offset, 0, str);
        partitionRegistry.add(data);
        editOffset = doc.getLength();
        if(doSetStyles)
        {
          for(StyleRange style : data.originalStyle)
          {
            if( !quietTrace )
              traceMsg += ", applying style "+style.toString();
            getTextWidget().setStyleRange(style);            
          }
        }
      }
      catch(BadLocationException e)
      {
        if( !quietTrace )
          traceMsg += ", producing exception.";
        logException("Failed to print to Repl", e);
      }
      logTrace(traceMsg);
    }
    else
    {
      logWarning("Tried to print to uninitialized Repl");
    }
  }

}