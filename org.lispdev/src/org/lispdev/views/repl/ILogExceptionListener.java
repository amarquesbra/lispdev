/**
 * 
 */
package org.lispdev.views.repl;

/**
 * Listener for log messages. The messages are sent using log function.
 */
public interface ILogExceptionListener
{
  /**
   * Sends exception to listener.
   */
  public void log(String msg, Throwable e);
}
