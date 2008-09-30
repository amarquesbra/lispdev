package org.lispdev.views.repl;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class Prompt
{
  public String prompt;
  public PartitionData partition;
  public boolean onNewLine;
  
  public Prompt(String prompt, String promptContext, String id,
      Color foreground, Color background, int fontStyle, boolean onNewLine)
  {
    this.prompt = prompt;
    this.onNewLine = onNewLine;
    this.partition = new PartitionData(0,prompt.length(), promptContext, id,
        new StyleRange[]{new StyleRange(0,prompt.length(),foreground,
            background,fontStyle)});
  }
  
  public Prompt(String prompt, String promptContext, String id,
      StyleRange[] style)
  {
    this.prompt = prompt;
    this.partition = new PartitionData(0,prompt.length(),
        promptContext,id,style);
    
  }
  
  public Prompt(String prompt, PartitionData partition, boolean onNewLine)
  {
    this.prompt = prompt;
    this.partition = partition;
    this.onNewLine = onNewLine;
  }
  
  public String toString()
  {
    return "{\""+prompt+"\","+partition+","
      +String.valueOf(onNewLine)+"}";
  }

}
