/**
 * 
 */
package org.lispdev.views.repl;

/**
 * Listener for log messages. The messages are sent using log function.
 */
public interface ILogListener
{
  /**
   * Sends message to log listener.
   * @param msg
   */
  public void log(String msg);
}
