// class magellan.plugin.statistics.db.CriteriaPopulator
// created on 30.01.2009
//
// Copyright 2003-2009 by Thoralf Rickert
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

import java.sql.Connection;
import java.util.List;

import org.apache.torque.util.Criteria;

/**
 * Dieses Interface dient dazu, Anfragen in Torque zu stellen
 *
 * @author <a href="mailto:alexander.teuscher@cadooz.de">Alexander Teuscher</a>
 * @author <a href="mailto:marcus.linke@cadooz.de">Marcus Linke</a>
 * @author <a href="mailto:thoralf.rickert@cadooz.de">Thoralf Rickert</a>
 * @version 1.0, erstellt am 09.03.2007
 */
public interface CriteriaPopulator<E> {
  public List<E> getRows(Connection connection, Criteria criteria);
}
