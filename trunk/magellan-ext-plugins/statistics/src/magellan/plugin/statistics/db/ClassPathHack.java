// class magellan.plugin.statistics.db.ClassPathHack
// created on 26.05.2008
//
// Copyright 2003-2008 by Thoralf Rickert
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.plugin.statistics.db;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is an idea from antony_miguel. 
 * @see http://forum.java.sun.com/thread.jspa?threadID=300557&range=15&start=0&q=&hilite=false&forumID=32
 *
 * @author ...
 * @version 1.0, 26.05.2008
 */
public class ClassPathHack {

  private static final Class[] parameters = new Class[]{URL.class};
 
  public static void addFile(String s) throws IOException {
    File f = new File(s);
    addFile(f);
  }//end method
   
  public static void addFile(File f) throws IOException {
    addURL(f.toURI().toURL());
  }//end method
   
   
  public static void addURL(URL u) throws IOException {
      
    URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
   
    try {
      Method method = sysclass.getDeclaredMethod("addURL",parameters);
      method.setAccessible(true);
      method.invoke(sysloader,new Object[]{ u });
    } catch (Throwable t) {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader");
    }//end try catch
      
  }//end method
   
}//end class
