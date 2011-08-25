<?

mb_http_output("UTF-8");
ob_start("mb_output_handler");

header("Content-Type: text/xml; charset=UTF-8");
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

?>
