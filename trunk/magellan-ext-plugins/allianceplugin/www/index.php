<?
 include_once "setup.php";
 include_once "db.php";
?>
<html>
 <head>
  <title>odyssey - <? echo $alliance; ?></title>
 </head>
 <body>
  <p>
   <center>
    <p>
    <u><b><pre>Welcome to Odyssey</pre></b></u>
    </p>
    <p>
    <table>
     <tr>
      <td>
       <pre>
        This is the home page of the <? echo $alliance; ?>.

        You can find here some informations about this alliance
        and some public announcements.

        Members:
<?

 $query = "SELECT faction.* FROM faction,alliance,alliance_faction_relation WHERE alliance.name='$alliance' AND alliance.id=alliance_faction_relation.alliance_id AND alliance_faction_relation.faction_id=faction.id ORDER BY faction.name";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 while ($row = mysql_fetch_array($result)) {
   echo "         - $row[name] ($row[eressea_id])\n";
 }
?>

        Announcements:
         - no announcements available at the moment.
       </pre>
      </td>

     </tr>
    </table>
    </p>

   </center>
  </p>
 </body>
</html>
