// class magellan.plugin.statistics.IdentifiableType
// created on 03.05.2008
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
package magellan.plugin.statistics;

/**
 * Contains identifiable types.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public enum IdentifiableType {
  UNIT,
  REGION,
  FACTION,
  SHIP,
  BUILDING,
  UNKNOWN;
  
  public static IdentifiableType getType(String name) {
    try {
      return valueOf(name);
    } catch (Exception e) {
      return UNKNOWN;
    }
  }
}
