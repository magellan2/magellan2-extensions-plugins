<?
 include_once "setup.php";
 include_once "db.php";
 include_once "auth.php";

 $mapname = urldecode($_GET["map"]);
 $version = $_GET["version"];

 $query = "SELECT map_part.* FROM map_part WHERE name='$mapname' AND version=$version LIMIT 0,1";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());

 while ($row = mysql_fetch_array($result)) {
   $map_part = $row;
 }

 $filename = $reports_path."/".$map_part[path];

 if (!$map_part || !file_exists($filename)) {
   header("Authentification: File not found",true,404);
   echo "File not found\n$filename\n$query";
   exit;
 }

 header("Cache-Control: public, must-revalidate");
 header("Pragma: hack");
 header("Content-Type: application/octed-stream");
 header("Content-Length: " .(string)(filesize($filename)) );
 header('Content-Disposition: attachment; filename=map.cr.bz2"');
 header("Content-Transfer-Encoding: binary");

 readfile($filename);
?>
