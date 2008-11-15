This is a plugin for the Magellan2 application. The main feature 
is the analyzation of current and historic data of a faction, a 
region or a single unit.

This plugin tracks the positions and skill levels of all known 
units at all known times in a special file. Also it tracks the
region details.

Build 73
 - only analyze region which are visible to a unit.
 - added a more useful state tab which shows the analyze progress
 - new try to avoid flashing of dock
 
Build 57
 - problems with invisible tab if the selection changed

Build 46
 - take care about the report time to avoid useless analyze 
   processes
   
Build 40
 - we use Apache Derby and Apache Torque to save the statistics
   data in a local pure Java database which is much easier to use
   then a XML file.