package org.lispdev.test.swt;

import org.eclipse.swt.SWT;
import org.lispdev.LispdevPlugin;
import org.lispdev.views.ReplView;

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
    System.out.print(o);
  }
  
  public void testDoSomethingInterestingWithEclipse() throws Exception 
  {
  
    trace("starting\n");
    bot.menu("Help").menu("Dynamic Help").click();
    trace("Clicked Help->Dynamic Help");

    bot.menu("Window").menu("Show View").menu("Other...").click();
    trace("Clicked Window->Show View->Other...\n");
    
    bot.tree().setFocus();
    SWTBotTreeItem otherItem = bot.tree().getTreeItem("Other"); 
    otherItem.expand();
    otherItem.getNode("Repl View").select();
    bot.button("OK").click();
    
    SWTBotView replview = bot.view("Repl View"); 
    replview.show();
    replview.setFocus();

    /*
    ReplView.show();
    trace("Repl View is shown now\n");
    */
    
    bot.sleep(300);
    ReplView rv = LispdevPlugin.getDefault().getReplView();
    trace("Repl view = "+rv+"\n");
    if(rv == null)
    {
      trace("Repl view is null, try to increase sleep timeout\n");
      return;
    }
    // FIXME: the following works, but would rather use 
    // find styledText, which is not yet in SWTBot
    SWTBotStyledText rtxt = bot.styledTextWithLabel("");
    trace("Repl text = "+rtxt+"\n");
    rtxt.typeText("123");

    /*
    Event e = new Event();
    e.time = (int) System.currentTimeMillis();
    e.widget = rv.repl.getTextWidget();
    e.display = rv.repl.getTextWidget().getDisplay();
    e.character = SWT.LF;
    e.keyCode = SWT.LF;

    rv.inputTrigger.run(new VerifyEvent(e));
    */
    rtxt.notifyKeyboardEvent(SWT.NONE, SWT.LF);
    bot.sleep(1000);
    
   /* bot.view("Repl View").show();
	    bot.view("Package Explorer").close();
	    bot.editor("HelloWorld.java").save();
	    bot.editor("FooBar.java").close();

	    bot.activeEditor().typeText("public static void main ()...");
	    bot.activeEditor().quickfix("Rename in file");
	    
	    // will insert "System.out.println();" in the currently open editor
	    bot.activeEditor().autoCompleteProposal("sys", "sysout - print to standard out"); */
  }
		  
		  // stuff you can do with SWT
	/*	  public void testDoSomethingInterestingWithSWT() throws Exception {

		    // there are two parts to SWTBot:
		    // one to find a control (the subject)
		    // and the action to be performed on the control (the verb)
		    bot.shell("Address Book - Untitled").activate();
		    bot.button("Hello World").click();
		    bot.menu("File").menu("New").click();
		    bot.captureScreenshot("myscreenshot.png");

		    bot.listWithLabel("My Items").select(new String[] { "foo", "bar", "baz" });

		    // there are a lot of assertions that are very useful
		    assertEnabled(bot.button("Foo Bar"));
		    assertVisible(bot.checkBox("This should not visible"));
		    assertTextContains("I just love this!", bot.textWithLabel("Comments"));
		  } */
}
