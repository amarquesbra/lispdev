/**
 * 
 */
package org.lispdev.views.repl;

import org.eclipse.swt.events.VerifyEvent;

/**
 * @author sk
 *
 */
public interface IReplInputListener
{
  void run(String msg, PartitionData pd, VerifyEvent event);
}
