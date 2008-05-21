package magellan.plugin.statistics.torque.map;

import org.apache.torque.TorqueException;

/**
 * This is a Torque Generated class that is used to load all database map 
 * information at once.  This is useful because Torque's default behaviour
 * is to do a "lazy" load of mapping information, e.g. loading it only
 * when it is needed.<p>
 *
 * @see org.apache.torque.map.DatabaseMap#initialize() DatabaseMap.initialize() 
 */
public class StatisticsMapInit
{
    public static final void init()
        throws TorqueException
    {
        magellan.plugin.statistics.torque.ReportPeer.getMapBuilder();
        magellan.plugin.statistics.torque.BuildingStatisticsDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.BuildingStatisticsPeer.getMapBuilder();
        magellan.plugin.statistics.torque.FactionStatisticsDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.FactionStatisticsPeer.getMapBuilder();
        magellan.plugin.statistics.torque.ShipStatisticsDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.ShipStatisticsPeer.getMapBuilder();
        magellan.plugin.statistics.torque.RegionStatisticsDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.RegionStatisticsShipDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.RegionStatisticsPricesDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.RegionStatisticsResourcesDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.RegionStatisticsPeer.getMapBuilder();
        magellan.plugin.statistics.torque.UnitStatisticsDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.UnitStatisticsSkillDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.UnitStatisticsItemDataPeer.getMapBuilder();
        magellan.plugin.statistics.torque.UnitStatisticsPeer.getMapBuilder();
    }
}
