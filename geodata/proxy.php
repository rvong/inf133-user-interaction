<?php
	$url = $_SERVER['QUERY_STRING'];
	
	$ch = curl_init($url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);

	$response = curl_exec($ch);
	$cType = curl_getinfo($ch, CURLINFO_CONTENT_TYPE);

	header('Content-type: '. $cType);
	print $response;
	curl_close($ch);
?>
