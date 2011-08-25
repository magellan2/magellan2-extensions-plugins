<?
 include_once "setup.php";
 include_once "db.php";
 include_once "auth.php";
 include_once "xml-utf8.php";

 echo "<odyssey version=\"1.0\">\n";
 echo "  <servername>".htmlspecialchars($servername)."</servername>\n";
 echo "  <alliance>\n";
 echo "    <name>".htmlspecialchars($alliance)."</name>\n";
 $query = "SELECT faction.* FROM faction,alliance_faction_relation WHERE alliance_faction_relation.alliance_id=$allied[id] AND alliance_faction_relation.faction_id=faction.id ORDER BY faction.name";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 echo "    <members>\n";
 while ($row = mysql_fetch_array($result)) {
   echo "      <faction id=\"$row[eressea_id]\" mail=\"$row[email]\">".htmlspecialchars($row[name])."</faction>\n";
 }
 echo "    </members>\n";

 $query = "SELECT map.* FROM map WHERE map.alliance_id = $allied[id]";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 while ($map = mysql_fetch_array($result)) {
   echo "    <map name=\"$map[name]\" lastchange=\"$map[lastchange]\">\n";

   $query = "SELECT map_part.* FROM map_part WHERE map_part.map_id = $map[id]";
   $result1 = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

   while ($map_part = mysql_fetch_array($result1)) {
     $size = @filesize($reports_path."/".$map_part[path]);
     echo "      <part id=\"$map_part[id]\" version=\"$map_part[version]\" lastchange=\"$map_part[lastchange]\" round=\"$map_part[round]\" size=\"$size\">".htmlspecialchars($map_part[name])."</part>\n";
   }
   echo "    </map>\n";
 }
 echo "  </alliance>\n";
 echo "</odyssey>\n";
?>
