/**
 * 
 */
package org.lispdev.views;

import java.util.HashMap;

import org.lispdev.*;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;

/**
 * @author sk
 *
 *make it abstract
 */
public class Repl extends SourceViewer
{
  private ReplConfiguration config;
  private boolean inEditMode = false;
  private IDocument doc;
  private int editOffset; //editable part is after this offset
  
  // it is going to grow in increasing order
  private HashMap<Integer,PartitionData> partitionRegistry; 
  
  public int getEditOffset()
  {
    return editOffset;
  }
  
  public boolean isInEditMode()
  {
    return inEditMode;
  }

  public Repl(Composite parent, IVerticalRuler ruler, int styles)
  {
    super(parent, ruler, styles);
    setEditable(true);
    config = new ReplConfiguration(this);
    configure(config);

    doc = new Document();
    partitionRegistry = new HashMap<Integer,PartitionData>();
//  LispDocumentProvider.connectPartitioner(doc);
    setDocument(doc, new AnnotationModel());
    showAnnotations(false);
    showAnnotationsOverview(false);
  }

  // prints new line, prompt and then allow you edit repl
  public void startEdit(String prompt, String promptContext, StyleRange[] promptStyle)
  {
    print("\n"+prompt,promptContext,promptStyle);
    inEditMode = true;
  }
  
  public void stopEdit()
  {
    inEditMode = false;
  }
  
  
  public void print(String str, String context, StyleRange[] styles)
  {
    print(str, new PartitionData(0,str.length(),context,styles,
        null,null,null,null,null,null,null,null));
  }
  
  
  // this function is used by listeners to print to repl
  public void print(String str, PartitionData data)
  {
    if(doc != null)
    {
      boolean stylesOK = true;
      int offset = doc.getLength();
      // adjust style ranges by offset, also check that they are ok
      if(data == null || data.originalStyle == null)
      {
        stylesOK = false;
      }
      else
      {
        for(StyleRange st : data.originalStyle)
        {
          if( st.length > str.length() )
          {
            LispdevDebug.logWarning("Style range is outside of bounds");          
            stylesOK = false;
            break;
          }
          st.start += offset;
        }        
      }
      try
      {
        partitionRegistry.put(new Integer(offset), data);
        doc.replace(offset, 0, str);
        editOffset = doc.getLength();
      }
      catch(BadLocationException e)
      {
        LispdevDebug.logException("Failed to print to Repl", e);
        e.printStackTrace();
      }
      if(stylesOK)
      {
        for(StyleRange style : data.originalStyle)
        {
          getTextWidget().setStyleRange(style);            
        }
      }
    }
    else
    {
      LispdevDebug.logWarning("Tried to print to uninitialized Repl");
    }
  }
  
}
