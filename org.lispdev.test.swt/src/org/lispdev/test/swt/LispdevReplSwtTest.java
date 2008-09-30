package org.lispdev.test.swt;

import org.eclipse.swt.SWT;
import org.lispdev.LispdevPlugin;
import org.lispdev.views.ReplView;
import org.lispdev.views.repl.PartitionData;
import org.lispdev.views.repl.Repl;

import net.sf.swtbot.eclipse.finder.SWTBotEclipseTestCase;
import net.sf.swtbot.eclipse.finder.widgets.SWTBotView;
import net.sf.swtbot.widgets.SWTBotStyledText;
import net.sf.swtbot.widgets.SWTBotTreeItem;
import net.sf.swtbot.widgets.WidgetNotFoundException;

// to enable swtbot logging put this
// to vm variable in run configuration:
// -Dlog4j.configuration=file:${workspace_loc}/org.lispdev.test.swt/log4j.xml
// and Eclipse-RegisterBuddy: org.apache.log4j at the end of manifest.mf

public class LispdevReplSwtTest extends SWTBotEclipseTestCase
{
  private Repl repl;
  private SWTBotStyledText rtxt;
  
  private void trace(Object o)
  {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    System.out.print("("+ste.getFileName()+":"
        +String.valueOf(ste.getLineNumber())+")"
        +" "+o+"\n");
  }
  
  protected void setUp()
  {
    rtxt = null;
    try
    {
      rtxt = bot.styledText("Echo>");
    }
    catch(WidgetNotFoundException e1)
    {
      SWTBotView replview;
      try
      {
        /**
        trace(" ====== on eclipse start dismiss welcome screen"); 
        bot.menu("Help").menu("Dynamic Help").click();

        trace(" ====== open repl view via Show View menu");
        bot.menu("Window").menu("Show View").menu("Other...").click();
        
        bot.sleep(150);
        bot.tree().setFocus();
        SWTBotTreeItem otherItem = bot.tree().getTreeItem("Other"); 
        otherItem.expand();
        bot.sleep(150);
        otherItem.getNode("Repl View").select();
        bot.button("OK").click();
        
        /**/
        ReplView.show();
        /**/

        //bot.sleep(150);
        replview = bot.view("Repl View");
        //replview.show();
        //bot.sleep(150);
        replview.setFocus();
        
        rtxt = bot.styledText("Echo>");

      }
      catch (Exception e) {
        trace("ReplSWT is null, try to increase previous sleep timeout");
        e.printStackTrace();
      }
    }

    repl = null;
    if( rtxt != null )
    {
      ReplView rv = LispdevPlugin.getDefault().getReplView();
      if(rv == null)
      {
        trace("Repl view is null, try to increase previous sleep timeout");
        return;
      }
      repl = rv.repl;
      trace("repl = "+ repl);
    }
    
    assertEquals("Echo>",rtxt.getText());
    assertTrue(repl.sanityCheck());
  }
  
  protected void tearDown()
  {
    try
    {
      rtxt.contextMenu("Clear").click();
    }
    catch(WidgetNotFoundException e)
    {
      trace("Clear menu is not found");
      e.printStackTrace();
    }
  }
  
  public void testSimpleTypingTest()
  {
    trace(" ===== simple typing test");
    rtxt.typeText("123");
    assertEquals("123",repl.getEditText());
  }

  public void testEnterTrigger()
  {
    trace(" ===== testing enter trigger which stops edit mode");
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    // edit text became read only, echo line is printed,
    // new prompt is printed, in edit mode
    assertTrue(repl.sanityCheck());
    assertTrue(repl.getEditModeFlag());
    PartitionData pd = repl.getPartitionAt(7, Repl.NONE);//previous edit text
    assertEquals("123",repl.getText(pd));
    //new line context (single new line is entered)
    pd = repl.getPartitionAt(8, Repl.AFTER);
    assertEquals("\n",repl.getText(pd));
    assertEquals("_new_line_context__",pd.context);
    pd = repl.getPartitionAt(10, Repl.NONE);
    assertEquals("Printed: \"123\", in context: " +
        "\"echo_prompt._edit_context__\", with id 0",repl.getText(pd));
    
  }
  
  public void testTypeOnReadOnly()
  {
    trace(" ===== try to type on first line third column (read only part)");
    rtxt.typeText(0, 3, "abc");
    // partitions shouldn't be destroyed, text should appear in edit region
    assertTrue(repl.sanityCheck());
    PartitionData pd = repl.getPartitionAt(2, Repl.NONE);
    assertEquals("Echo>", repl.getText(pd));
    assertEquals("abc", repl.getEditText());    
  }

  public void testAddReadOnly()
  {
    trace(" ===== add read-only based on last edit region");
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    rtxt.typeText(1, 3, "abc");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    assertTrue(repl.sanityCheck());
    assertTrue(repl.getEditModeFlag());
    assertEquals("abc123", repl.getEditText());    
  }
  
  public void testMoreAddvancedReadOnly()
  {
    trace(" ===== more advanced test with read-only partitions in edit region");
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    rtxt.typeText(1, 3, "abc");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    //- move caret to start put one character (to move read-only)
    //- add read only again (same 123) - now have two read-only ranges
    //next to each other
    //- put caret into first range and type couple symbols
    //- these symbols should appear between two read-only

    rtxt.typeText(2, 0, "+");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText(2, 10, "--");
    assertEquals("+abc123--123", repl.getEditText());
    
    trace(" ===== del before read-only");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.DEL, SWT.DEL);
    assertEquals("+abc123--", repl.getEditText());
    
    trace(" ===== del after read-only");
    rtxt.selectRange(2,12,0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.DEL, SWT.DEL);
    assertEquals("+abc123-",repl.getEditText());
    
    trace(" ===== backspace before read-only");
    rtxt.selectRange(2, 9, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("+ab123-",repl.getEditText());

    trace(" ===== backspace after read-only");
    rtxt.selectRange(2, 11, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("+ab-", repl.getEditText());    
    
    trace(" ===== put read-only again and then hit <enter>");
    // check how new partition is created
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    assertTrue(repl.sanityCheck());
  }

  public void testPartialPromptSelection()
  {
    trace(" ===== if selection is partially on promt, delete just editable part");
    rtxt.typeText("123");
    rtxt.selectRange(0, 2, 4);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("23",repl.getEditText());    
  }
  
  public void testSelectionCoversReadOnly()
  {
    trace(" ===== if selection covers read-only delete it");
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);

    rtxt.typeText("23-");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("+++");
    rtxt.selectRange(2, 2, 18);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("++",repl.getEditText());
    assertTrue(repl.sanityCheck());    
  }
  
  public void testSelectionIntersectsReadOnly()
  {
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    
    trace(" ===== if selection intersects read-only keep it intact");
    rtxt.typeText("++");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("+++");
    rtxt.selectRange(2, 8, 8);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("++123123+++",repl.getEditText());
    assertTrue(repl.sanityCheck());    
  }
  
  public void testBug5()
  {
    trace(" ===== Bug#5: When I press backspace it works like delete");
    rtxt.typeText("123");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("23",repl.getEditText());
    
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    assertTrue(repl.sanityCheck());
  }
  
  public void testBug6case1()
  {
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    
    trace(" ===== Bug#6: Read only is not deleted, when select and start typing");
    rtxt.typeText("abc");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    rtxt.typeText("---");
    rtxt.selectRange(2, 7, 5);
    rtxt.typeText("aa");
    assertEquals("abaa--",repl.getEditText());
    

    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    assertTrue(repl.sanityCheck());    
  }
  
  public void testBug6case2() 
  {
    rtxt.typeText("123");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);

    trace(" ===== Bug#6: (case 2) Read only is not" +
    		" deleted, when select and start typing");
    rtxt.typeText("abc");
    rtxt.selectRange(0, 6, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    rtxt.typeText("---");
    rtxt.selectRange(2,7,2);
    rtxt.typeText("a");
    assertEquals("aba123---",repl.getEditText());
    
    /*
     * TODO: more tests
     * - bug: when undo - read only is destroyed (redo should put it back)
     * - copy-paste? (just copy-paste text, drop even formating)?
     * - bug: if part of read-only is selected and hit del - get exception - fixed
     * - make read only editable? (use copy paste)
     * - mouse events
     * - when select all and hit delete, need to remove read only partitions
     * from registry - to test, edit: put some test, then read-only, then
     * again text, select text around read-only and delete, place where
     * read-only was located still looks like read-only, also test partial
     * overlapping of some and complete overlapping of all
     */
    
    
    bot.sleep(500);
    
  }
		  
}
