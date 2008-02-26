// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.net;

import java.io.File;

public interface Observer {
  public void transfer(long value);
  public void setFile(File file);
}
