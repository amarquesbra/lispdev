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

// to enable swtbot logging put this
// to vm variable in run configuration: -Dlog4j.configuration=file:${workspace_loc}/org.lispdev.test.swt/log4j.xml
// and Eclipse-RegisterBuddy: org.apache.log4j at the end of manifest.mf

public class LispdevReplSwtTest extends SWTBotEclipseTestCase
{
  
  private void trace(Object o)
  {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    System.out.print("("+ste.getFileName()+":"
        +String.valueOf(ste.getLineNumber())+")"
        +" "+o+"\n");
  }
  
  public void testBasicReplTest() throws Exception 
  {
  
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
    
    bot.sleep(150);
    SWTBotView replview = bot.view("Repl View"); 
    replview.show();
    bot.sleep(150);
    replview.setFocus();

    /*
    ReplView.show();
    */

    trace(" ====== check repl startup and get necessary handles");
    ReplView rv = LispdevPlugin.getDefault().getReplView();
    trace("Repl view = "+rv);
    if(rv == null)
    {
      trace("Repl view is null, try to increase previous sleep timeout");
      return;
    }
    SWTBotStyledText rtxt = bot.styledText("start>");
    
    assertEquals("start>",rtxt.getText());
    assertTrue(rv.repl.sanityCheck());
    
    trace(" ===== simple typing test");    
    rtxt.typeText("123");
    assertEquals("123",rv.repl.getEditText());
    
    trace(" ===== testing enter trigger which stops edit mode");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    // edit text became read only, echo line is printed,
    // new prompt is printed, in edit mode
    assertTrue(rv.repl.sanityCheck());
    assertTrue(rv.repl.getEditModeFlag());
    PartitionData pd = rv.repl.getPartitionAt(8, Repl.NONE);//previous edit text
    assertEquals("123",rv.repl.getText(pd));
    //new line context (single new line is entered)
    pd = rv.repl.getPartitionAt(9, Repl.AFTER);
    assertEquals("\n",rv.repl.getText(pd));
    assertEquals("_new_line_context__",pd.context);
    pd = rv.repl.getPartitionAt(11, Repl.NONE);
    assertEquals("Printed: \"123\", in context: " +
    		"\"this prompt._edit_context__\", with id 0",rv.repl.getText(pd));
    
    trace(" ===== try to type on first line third column (read only part)");
    rtxt.typeText(1, 3, "abc");
    // partitions shouldn't be destroyed, text should appear in edit region
    assertTrue(rv.repl.sanityCheck());
    pd = rv.repl.getPartitionAt(3, Repl.NONE);
    assertEquals("start>", rv.repl.getText(pd));
    assertEquals("abc", rv.repl.getEditText());
    
    trace(" ===== add read-only based on last edit region");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    assertTrue(rv.repl.sanityCheck());
    assertTrue(rv.repl.getEditModeFlag());
    assertEquals("abc123", rv.repl.getEditText());
    
    trace(" ===== more advanced test with read-only partitions in edit region");
    //- move caret to start put one character (to move read-only)
    //- add read only again (same 123) - now have two read-only ranges
    //next to each other
    //- put caret into first range and type couple symbols
    //- these symbols should appear between two read-only
    rtxt.typeText(2, 0, "+");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText(2, 10, "--");
    assertEquals("+abc123--123", rv.repl.getEditText());
    
    trace(" ===== del before read-only");
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.DEL, SWT.DEL);
    assertEquals("+abc123--", rv.repl.getEditText());
    
    trace(" ===== del after read-only");
    rtxt.selectRange(2,12,0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.DEL, SWT.DEL);
    assertEquals("+abc123-",rv.repl.getEditText());
    
    trace(" ===== backspace before read-only");
    rtxt.selectRange(2, 9, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("+ab123-",rv.repl.getEditText());

    trace(" ===== backspace after read-only");
    rtxt.selectRange(2, 11, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("+ab-", rv.repl.getEditText());
    
    trace(" ===== put read-only again and then hit <enter>");
    // check how new partition is created
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);
    assertTrue(rv.repl.sanityCheck());
    
    trace(" ===== if selection is partially on promt, delete just editable part");
    rtxt.typeText("123");
    rtxt.selectRange(4, 2, 4);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("23",rv.repl.getEditText());
    
    trace(" ===== if selection covers read-only delete it");
    rtxt.typeText("-");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("+++");
    rtxt.selectRange(4, 2, 18);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("++",rv.repl.getEditText());
    assertTrue(rv.repl.sanityCheck());
    
    trace(" ===== if selection intersects read-only delete it");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("-");
    rtxt.selectRange(0, 7, 0);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF, SWT.LF);    
    rtxt.typeText("+++");
    rtxt.selectRange(4, 8, 8);
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.BS, SWT.BS);
    assertEquals("+++++",rv.repl.getEditText());
    assertTrue(rv.repl.sanityCheck());
    
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
