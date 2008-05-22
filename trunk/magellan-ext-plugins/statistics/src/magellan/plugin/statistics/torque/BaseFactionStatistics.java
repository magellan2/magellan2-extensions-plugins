package magellan.plugin.statistics.torque;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.torque.TorqueException;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.DateKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.om.StringKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;





/**
 * Contains informations about factions
 *
 * This class was autogenerated by Torque on:
 *
 * [Thu May 22 14:33:00 CEST 2008]
 *
 * You should not use this class directly.  It should not even be
 * extended all references should be to FactionStatistics
 */
public abstract class BaseFactionStatistics extends BaseObject
{
    /** Serial version */
    private static final long serialVersionUID = 1211459580847L;

    /** The Peer class */
    private static final FactionStatisticsPeer peer =
        new FactionStatisticsPeer();


    /** The value for the iD field */
    private long iD;

    /** The value for the reportId field */
    private long reportId;

    /** The value for the factionNumber field */
    private String factionNumber;


    /**
     * Get the ID
     *
     * @return long
     */
    public long getID()
    {
        return iD;
    }


    /**
     * Set the value of ID
     *
     * @param v new value
     */
    public void setID(long v) throws TorqueException
    {

        if (this.iD != v)
        {
            this.iD = v;
            setModified(true);
        }



        // update associated FactionStatisticsData
        if (collFactionStatisticsDatas != null)
        {
            for (int i = 0; i < collFactionStatisticsDatas.size(); i++)
            {
                ((FactionStatisticsData) collFactionStatisticsDatas.get(i))
                        .setFactionId(v);
            }
        }
    }

    /**
     * Get the ReportId
     *
     * @return long
     */
    public long getReportId()
    {
        return reportId;
    }


    /**
     * Set the value of ReportId
     *
     * @param v new value
     */
    public void setReportId(long v) throws TorqueException
    {

        if (this.reportId != v)
        {
            this.reportId = v;
            setModified(true);
        }


        if (aReport != null && !(aReport.getID() == v))
        {
            aReport = null;
        }

    }

    /**
     * Get the FactionNumber
     *
     * @return String
     */
    public String getFactionNumber()
    {
        return factionNumber;
    }


    /**
     * Set the value of FactionNumber
     *
     * @param v new value
     */
    public void setFactionNumber(String v) 
    {

        if (!ObjectUtils.equals(this.factionNumber, v))
        {
            this.factionNumber = v;
            setModified(true);
        }


    }

    



    private Report aReport;

    /**
     * Declares an association between this object and a Report object
     *
     * @param v Report
     * @throws TorqueException
     */
    public void setReport(Report v) throws TorqueException
    {
        if (v == null)
        {
            setReportId( 0);
        }
        else
        {
            setReportId(v.getID());
        }
        aReport = v;
    }


    /**
     * Returns the associated Report object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated Report object
     * @throws TorqueException
     */
    public Report getReport()
        throws TorqueException
    {
        if (aReport == null && (this.reportId != 0))
        {
            aReport = ReportPeer.retrieveByPK(SimpleKey.keyFor(this.reportId));
        }
        return aReport;
    }

    /**
     * Return the associated Report object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated Report object
     * @throws TorqueException
     */
    public Report getReport(Connection connection)
        throws TorqueException
    {
        if (aReport == null && (this.reportId != 0))
        {
            aReport = ReportPeer.retrieveByPK(SimpleKey.keyFor(this.reportId), connection);
        }
        return aReport;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setReportKey(ObjectKey key) throws TorqueException
    {

        setReportId(((NumberKey) key).longValue());
    }
   


    /**
     * Collection to store aggregation of collFactionStatisticsDatas
     */
    protected List<FactionStatisticsData> collFactionStatisticsDatas;

    /**
     * Temporary storage of collFactionStatisticsDatas to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
    protected void initFactionStatisticsDatas()
    {
        if (collFactionStatisticsDatas == null)
        {
            collFactionStatisticsDatas = new ArrayList<FactionStatisticsData>();
        }
    }


    /**
     * Method called to associate a FactionStatisticsData object to this object
     * through the FactionStatisticsData foreign key attribute
     *
     * @param l FactionStatisticsData
     * @throws TorqueException
     */
    public void addFactionStatisticsData(FactionStatisticsData l) throws TorqueException
    {
        getFactionStatisticsDatas().add(l);
        l.setFactionStatistics((FactionStatistics) this);
    }

    /**
     * Method called to associate a FactionStatisticsData object to this object
     * through the FactionStatisticsData foreign key attribute using connection.
     *
     * @param l FactionStatisticsData
     * @throws TorqueException
     */
    public void addFactionStatisticsData(FactionStatisticsData l, Connection con) throws TorqueException
    {
        getFactionStatisticsDatas(con).add(l);
        l.setFactionStatistics((FactionStatistics) this);
    }

    /**
     * The criteria used to select the current contents of collFactionStatisticsDatas
     */
    private Criteria lastFactionStatisticsDatasCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of
     * getFactionStatisticsDatas(new Criteria())
     *
     * @return the collection of associated objects
     * @throws TorqueException
     */
    public List<FactionStatisticsData> getFactionStatisticsDatas()
        throws TorqueException
    {
        if (collFactionStatisticsDatas == null)
        {
            collFactionStatisticsDatas = getFactionStatisticsDatas(new Criteria(10));
        }
        return collFactionStatisticsDatas;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this FactionStatistics has previously
     * been saved, it will retrieve related FactionStatisticsDatas from storage.
     * If this FactionStatistics is new, it will return
     * an empty collection or the current collection, the criteria
     * is ignored on a new object.
     *
     * @throws TorqueException
     */
    public List<FactionStatisticsData> getFactionStatisticsDatas(Criteria criteria) throws TorqueException
    {
        if (collFactionStatisticsDatas == null)
        {
            if (isNew())
            {
               collFactionStatisticsDatas = new ArrayList<FactionStatisticsData>();
            }
            else
            {
                criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID() );
                collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelect(criteria);
            }
        }
        else
        {
            // criteria has no effect for a new object
            if (!isNew())
            {
                // the following code is to determine if a new query is
                // called for.  If the criteria is the same as the last
                // one, just return the collection.
                criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID());
                if (!lastFactionStatisticsDatasCriteria.equals(criteria))
                {
                    collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelect(criteria);
                }
            }
        }
        lastFactionStatisticsDatasCriteria = criteria;

        return collFactionStatisticsDatas;
    }

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of
     * getFactionStatisticsDatas(new Criteria(),Connection)
     * This method takes in the Connection also as input so that
     * referenced objects can also be obtained using a Connection
     * that is taken as input
     */
    public List<FactionStatisticsData> getFactionStatisticsDatas(Connection con) throws TorqueException
    {
        if (collFactionStatisticsDatas == null)
        {
            collFactionStatisticsDatas = getFactionStatisticsDatas(new Criteria(10), con);
        }
        return collFactionStatisticsDatas;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this FactionStatistics has previously
     * been saved, it will retrieve related FactionStatisticsDatas from storage.
     * If this FactionStatistics is new, it will return
     * an empty collection or the current collection, the criteria
     * is ignored on a new object.
     * This method takes in the Connection also as input so that
     * referenced objects can also be obtained using a Connection
     * that is taken as input
     */
    public List<FactionStatisticsData> getFactionStatisticsDatas(Criteria criteria, Connection con)
            throws TorqueException
    {
        if (collFactionStatisticsDatas == null)
        {
            if (isNew())
            {
               collFactionStatisticsDatas = new ArrayList<FactionStatisticsData>();
            }
            else
            {
                 criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID());
                 collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelect(criteria, con);
             }
         }
         else
         {
             // criteria has no effect for a new object
             if (!isNew())
             {
                 // the following code is to determine if a new query is
                 // called for.  If the criteria is the same as the last
                 // one, just return the collection.
                 criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID());
                 if (!lastFactionStatisticsDatasCriteria.equals(criteria))
                 {
                     collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelect(criteria, con);
                 }
             }
         }
         lastFactionStatisticsDatasCriteria = criteria;

         return collFactionStatisticsDatas;
     }











    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this FactionStatistics is new, it will return
     * an empty collection; or if this FactionStatistics has previously
     * been saved, it will retrieve related FactionStatisticsDatas from storage.
     *
     * This method is protected by default in order to keep the public
     * api reasonable.  You can provide public methods for those you
     * actually need in FactionStatistics.
     */
    protected List<FactionStatisticsData> getFactionStatisticsDatasJoinFactionStatistics(Criteria criteria)
        throws TorqueException
    {
        if (collFactionStatisticsDatas == null)
        {
            if (isNew())
            {
               collFactionStatisticsDatas = new ArrayList<FactionStatisticsData>();
            }
            else
            {
                criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID());
                collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelectJoinFactionStatistics(criteria);
            }
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            criteria.add(FactionStatisticsDataPeer.FACTION_ID, getID());
            if (!lastFactionStatisticsDatasCriteria.equals(criteria))
            {
                collFactionStatisticsDatas = FactionStatisticsDataPeer.doSelectJoinFactionStatistics(criteria);
            }
        }
        lastFactionStatisticsDatasCriteria = criteria;

        return collFactionStatisticsDatas;
    }



        
    private static List<String> fieldNames = null;

    /**
     * Generate a list of field names.
     *
     * @return a list of field names
     */
    public static synchronized List<String> getFieldNames()
    {
        if (fieldNames == null)
        {
            fieldNames = new ArrayList<String>();
            fieldNames.add("ID");
            fieldNames.add("ReportId");
            fieldNames.add("FactionNumber");
            fieldNames = Collections.unmodifiableList(fieldNames);
        }
        return fieldNames;
    }

    /**
     * Retrieves a field from the object by field (Java) name passed in as a String.
     *
     * @param name field name
     * @return value
     */
    public Object getByName(String name)
    {
        if (name.equals("ID"))
        {
            return new Long(getID());
        }
        if (name.equals("ReportId"))
        {
            return new Long(getReportId());
        }
        if (name.equals("FactionNumber"))
        {
            return getFactionNumber();
        }
        return null;
    }

    /**
     * Set a field in the object by field (Java) name.
     *
     * @param name field name
     * @param value field value
     * @return True if value was set, false if not (invalid name / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByName(String name, Object value )
        throws TorqueException, IllegalArgumentException
    {
        if (name.equals("ID"))
        {
            if (value == null || ! (Long.class.isInstance(value)))
            {
                throw new IllegalArgumentException("setByName: value parameter was null or not a Long object.");
            }
            setID(((Long) value).longValue());
            return true;
        }
        if (name.equals("ReportId"))
        {
            if (value == null || ! (Long.class.isInstance(value)))
            {
                throw new IllegalArgumentException("setByName: value parameter was null or not a Long object.");
            }
            setReportId(((Long) value).longValue());
            return true;
        }
        if (name.equals("FactionNumber"))
        {
            // Object fields can be null
            if (value != null && ! String.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setFactionNumber((String) value);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.  The String must be one of the static
     * Strings defined in this Class' Peer.
     *
     * @param name peer name
     * @return value
     */
    public Object getByPeerName(String name)
    {
        if (name.equals(FactionStatisticsPeer.ID))
        {
            return new Long(getID());
        }
        if (name.equals(FactionStatisticsPeer.REPORT_ID))
        {
            return new Long(getReportId());
        }
        if (name.equals(FactionStatisticsPeer.FACTION_NUMBER))
        {
            return getFactionNumber();
        }
        return null;
    }

    /**
     * Set field values by Peer Field Name
     *
     * @param name field name
     * @param value field value
     * @return True if value was set, false if not (invalid name / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByPeerName(String name, Object value)
        throws TorqueException, IllegalArgumentException
    {
      if (FactionStatisticsPeer.ID.equals(name))
        {
            return setByName("ID", value);
        }
      if (FactionStatisticsPeer.REPORT_ID.equals(name))
        {
            return setByName("ReportId", value);
        }
      if (FactionStatisticsPeer.FACTION_NUMBER.equals(name))
        {
            return setByName("FactionNumber", value);
        }
        return false;
    }

    /**
     * Retrieves a field from the object by Position as specified
     * in the xml schema.  Zero-based.
     *
     * @param pos position in xml schema
     * @return value
     */
    public Object getByPosition(int pos)
    {
        if (pos == 0)
        {
            return new Long(getID());
        }
        if (pos == 1)
        {
            return new Long(getReportId());
        }
        if (pos == 2)
        {
            return getFactionNumber();
        }
        return null;
    }

    /**
     * Set field values by its position (zero based) in the XML schema.
     *
     * @param position The field position
     * @param value field value
     * @return True if value was set, false if not (invalid position / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByPosition(int position, Object value)
        throws TorqueException, IllegalArgumentException
    {
    if (position == 0)
        {
            return setByName("ID", value);
        }
    if (position == 1)
        {
            return setByName("ReportId", value);
        }
    if (position == 2)
        {
            return setByName("FactionNumber", value);
        }
        return false;
    }
     
    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     *
     * @throws Exception
     */
    public void save() throws Exception
    {
        save(FactionStatisticsPeer.DATABASE_NAME);
    }

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     * Note: this code is here because the method body is
     * auto-generated conditionally and therefore needs to be
     * in this file instead of in the super class, BaseObject.
     *
     * @param dbName
     * @throws TorqueException
     */
    public void save(String dbName) throws TorqueException
    {
        Connection con = null;
        try
        {
            con = Transaction.begin(dbName);
            save(con);
            Transaction.commit(con);
        }
        catch(TorqueException e)
        {
            Transaction.safeRollback(con);
            throw e;
        }
    }

    /** flag to prevent endless save loop, if this object is referenced
        by another object which falls in this transaction. */
    private boolean alreadyInSave = false;
    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     *
     * @param con
     * @throws TorqueException
     */
    public void save(Connection con) throws TorqueException
    {
        if (!alreadyInSave)
        {
            alreadyInSave = true;



            // If this object has been modified, then save it to the database.
            if (isModified())
            {
                if (isNew())
                {
                    FactionStatisticsPeer.doInsert((FactionStatistics) this, con);
                    setNew(false);
                }
                else
                {
                    FactionStatisticsPeer.doUpdate((FactionStatistics) this, con);
                }
            }


            if (collFactionStatisticsDatas != null)
            {
                for (int i = 0; i < collFactionStatisticsDatas.size(); i++)
                {
                    ((FactionStatisticsData) collFactionStatisticsDatas.get(i)).save(con);
                }
            }
            alreadyInSave = false;
        }
    }


    /**
     * Set the PrimaryKey using ObjectKey.
     *
     * @param key iD ObjectKey
     */
    public void setPrimaryKey(ObjectKey key)
        throws TorqueException
    {
        setID(((NumberKey) key).longValue());
    }

    /**
     * Set the PrimaryKey using a String.
     *
     * @param key
     */
    public void setPrimaryKey(String key) throws TorqueException
    {
        setID(Long.parseLong(key));
    }


    /**
     * returns an id that differentiates this object from others
     * of its class.
     */
    public ObjectKey getPrimaryKey()
    {
        return SimpleKey.keyFor(getID());
    }
 

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     */
    public FactionStatistics copy() throws TorqueException
    {
        return copy(true);
    }

    /**
     * Makes a copy of this object using connection.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     *
     * @param con the database connection to read associated objects.
     */
    public FactionStatistics copy(Connection con) throws TorqueException
    {
        return copy(true, con);
    }

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * If the parameter deepcopy is true, it then fills all the
     * association collections and sets the related objects to
     * isNew=true.
     *
     * @param deepcopy whether to copy the associated objects.
     */
    public FactionStatistics copy(boolean deepcopy) throws TorqueException
    {
        return copyInto(new FactionStatistics(), deepcopy);
    }

    /**
     * Makes a copy of this object using connection.
     * It creates a new object filling in the simple attributes.
     * If the parameter deepcopy is true, it then fills all the
     * association collections and sets the related objects to
     * isNew=true.
     *
     * @param deepcopy whether to copy the associated objects.
     * @param con the database connection to read associated objects.
     */
    public FactionStatistics copy(boolean deepcopy, Connection con) throws TorqueException
    {
        return copyInto(new FactionStatistics(), deepcopy, con);
    }
  
    /**
     * Fills the copyObj with the contents of this object.
     * The associated objects are also copied and treated as new objects.
     *
     * @param copyObj the object to fill.
     */
    protected FactionStatistics copyInto(FactionStatistics copyObj) throws TorqueException
    {
        return copyInto(copyObj, true);
    }

  
    /**
     * Fills the copyObj with the contents of this object using connection.
     * The associated objects are also copied and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param con the database connection to read associated objects.
     */
    protected FactionStatistics copyInto(FactionStatistics copyObj, Connection con) throws TorqueException
    {
        return copyInto(copyObj, true, con);
    }
  
    /**
     * Fills the copyObj with the contents of this object.
     * If deepcopy is true, The associated objects are also copied
     * and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param deepcopy whether the associated objects should be copied.
     */
    protected FactionStatistics copyInto(FactionStatistics copyObj, boolean deepcopy) throws TorqueException
    {
        copyObj.setID(iD);
        copyObj.setReportId(reportId);
        copyObj.setFactionNumber(factionNumber);

        copyObj.setID( 0);

        if (deepcopy)
        {


        List<FactionStatisticsData> vFactionStatisticsDatas = getFactionStatisticsDatas();
        if (vFactionStatisticsDatas != null)
        {
            for (int i = 0; i < vFactionStatisticsDatas.size(); i++)
            {
                FactionStatisticsData obj =  vFactionStatisticsDatas.get(i);
                copyObj.addFactionStatisticsData(obj.copy());
            }
        }
        else
        {
            copyObj.collFactionStatisticsDatas = null;
        }
        }
        return copyObj;
    }
        
    
    /**
     * Fills the copyObj with the contents of this object using connection.
     * If deepcopy is true, The associated objects are also copied
     * and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param deepcopy whether the associated objects should be copied.
     * @param con the database connection to read associated objects.
     */
    protected FactionStatistics copyInto(FactionStatistics copyObj, boolean deepcopy, Connection con) throws TorqueException
    {
        copyObj.setID(iD);
        copyObj.setReportId(reportId);
        copyObj.setFactionNumber(factionNumber);

        copyObj.setID( 0);

        if (deepcopy)
        {


        List<FactionStatisticsData> vFactionStatisticsDatas = getFactionStatisticsDatas(con);
        if (vFactionStatisticsDatas != null)
        {
            for (int i = 0; i < vFactionStatisticsDatas.size(); i++)
            {
                FactionStatisticsData obj =  vFactionStatisticsDatas.get(i);
                copyObj.addFactionStatisticsData(obj.copy(con), con);
            }
        }
        else
        {
            copyObj.collFactionStatisticsDatas = null;
        }
        }
        return copyObj;
    }
    
    

    /**
     * returns a peer instance associated with this om.  Since Peer classes
     * are not to have any instance attributes, this method returns the
     * same instance for all member of this class. The method could therefore
     * be static, but this would prevent one from overriding the behavior.
     */
    public FactionStatisticsPeer getPeer()
    {
        return peer;
    }

    /**
     * Retrieves the TableMap object related to this Table data without
     * compiler warnings of using getPeer().getTableMap().
     *
     * @return The associated TableMap object.
     */
    public TableMap getTableMap() throws TorqueException
    {
        return FactionStatisticsPeer.getTableMap();
    }


    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("FactionStatistics:\n");
        str.append("ID = ")
           .append(getID())
           .append("\n");
        str.append("ReportId = ")
           .append(getReportId())
           .append("\n");
        str.append("FactionNumber = ")
           .append(getFactionNumber())
           .append("\n");
        return(str.toString());
    }
}
