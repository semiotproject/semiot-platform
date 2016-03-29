<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Upload Driver</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script
	src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
	<div class="container">
		<h3>Upload Driver</h3>
		<ul class="nav nav-pills nav-justified">
		<li><a href="/config/AdminPanel">Administration Panel</a></li>
			<li><a href="/config/SystemSettings">System Settings</a></li>
			<li class="active"><a href="/config/UploadDriver">Drivers</a></li>
		</ul>
		<div class="text-center">
			<br />
			<div class="btn-group">
				<a href="/config/DriversInstalled" class="btn btn-primary"
					role="button">Installed</a> <a href="/config/AvailableDrivers"
					class="btn btn-primary" role="button">Available</a> <a
					href="/config/UploadDriver" class="btn btn-primary active"
					role="button">Upload</a>
			</div>
		</div>
		<form action="${pageContext.request.contextPath}/UploadDriverHandler"
			method="post" enctype="multipart/form-data">
			<br class="form-control">Upload a .jar bundle file</br> <input
				type="file" name="bundlefile" accept="jar|war" /> <input
				type="submit" class="btn btn-primary" />
		</form>
	</div>
</body>
</html>