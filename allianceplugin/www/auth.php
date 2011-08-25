<?

 if ($usePOST) {
   $username = $_POST['user'];
   $password = $_POST['pass'];
 } else {
   $username = $_GET['user'];
   $password = $_GET['pass'];
 }

 if ($username == "" || $password == "") {
   header("Authentification: Authorization required",true,403);
   echo "Authorization required";
   exit;
 }

 $username = urldecode($username);
 $password = urldecode($password);

 $query = "SELECT faction.* FROM faction,user WHERE user.username='$username' AND user.password='$password' AND user.faction_id = faction.id LIMIT 0,1";
 $result = @mysql_query($query) or die("Query failed:\n".mysql_error());
 while ($row = mysql_fetch_array($result)) {
   $faction = $row;
 }

 if ($faction[eressea_id] == "") {
   header("Authentification: User not found",true,403);
   echo "User not found";
   exit;
 }

 $query = "SELECT alliance.* FROM alliance,alliance_faction_relation WHERE alliance_faction_relation.faction_id=$faction[id] AND alliance_faction_relation.alliance_id = alliance.id LIMIT 0,1";
 $result = @mysql_query($query) or die("Query $query failed:\n".mysql_error());
 while ($row = mysql_fetch_array($result)) {
   $allied = $row;
 }

 if ($allied[id] == "") {
   header("Authentification: User has no alliance",true,403);
   echo "User has no alliance";
   exit;
 }

?>
