<?

 $usePOST = true;

 include_once "setup.php";
 include_once "db.php";
 include_once "auth.php";

 $mapname = urldecode($_POST["map"]);
 $version = $_POST["version"];
 $partId = $_POST["partId"];
 $round = $_POST["round"];

 $query = "SELECT map_part.* FROM map_part WHERE id=$partId LIMIT 0,1";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 while ($row = mysql_fetch_array($result)) {
   $map_part = $row;
 }

 if (!$map_part) {
   header("Authentification: Map not found",true,404);
   echo "Map not found\n$query";
   exit;
 }

 $sourceFile = $reports_path."/".$map_part[path];

 if (file_exists($sourceFile)) {
   rename($sourceFile,$sourceFile."_v$map_part[version]");
 }

 echo "username: $username\n";
 echo "OK\n";

 echo "name: ".$_FILES['crfile']['name']."\n";
 echo "size: ".$_FILES['crfile']['size']."\n";
 echo "tmpf: ".$_FILES['crfile']['tmp_name']."\n";
 echo "OK\n";

 $tmpfile = $_FILES['crfile']['name'];
 $dot = strrpos($tmpfile,".");
 if (dot === false) {
   // assume zip
   $extension = ".zip";
 } else {
   $extension = substr($tmpfile,$dot);
 }

 $destinationFile = $reports_path."/".$map_part[path];

 move_uploaded_file($_FILES['crfile']['tmp_name'],$destinationFile);
 chmod($destinationFile,0777);

 echo "src: ".$sourceFile."\n";
 echo "des: ".$destinationFile."\n";
 echo "OK\n";

if ($round == 0) $round = $map_part[round];

 $query = "UPDATE map_part SET version=version+1, lastchange=now(), round=$round WHERE id=$partId";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 $query = "UPDATE map SET lastchange=now() WHERE id=$map_part[map_id]";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());
 

?>
