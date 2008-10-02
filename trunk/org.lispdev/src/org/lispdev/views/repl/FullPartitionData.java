package org.lispdev.views.repl;

public class FullPartitionData
{
  public String txt;
  public PartitionData partition;
  
  public FullPartitionData(String txt, PartitionData pd){
    this.txt = txt;
    partition = pd;    
  }
  
  public String toString()
  {
    return "txt: "+txt+"; partition = "+partition;
  }  
}
