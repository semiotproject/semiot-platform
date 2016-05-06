<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>SemIoT Platform</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet"
          href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script
            src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script
            src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
<%String username = request.getRemoteUser();%>
<div class="navbar-form navbar-right" role="navigation">
    <button class="btn btn-primary btn-sm" onClick="logout()" name="logout" readonly>
        <%=username%>
        <i class="glyphicon glyphicon-log-out"></i>
    </button>
</div>
<script>
    function logout(){
        $.ajax({url: "${pageContext.request.contextPath}/logout",
            type: 'GET',
            success: function(){
                window.location.replace("/");
            },
            error: function () {
                window.location.reload();
            }
        });
    }
</script>
<div class="container">
    <h1>SemIoT Platform</h1>
    <b>Navigation:</b>
    <ul class="nav nav-pills nav-justified">
        <a href="/explorer">Go to Explorer</a> |
        <a href="/config">Go to Configuration</a>
    </ul>
</div>
</body>
</html>
