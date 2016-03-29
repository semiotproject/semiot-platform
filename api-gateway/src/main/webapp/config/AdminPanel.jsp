<%@page import="java.util.List"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="ru.semiot.platform.apigateway.utils.Credentials"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>        
        <title>Administration panel</title>
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
        <%String username = request.getRemoteUser();%>
        <div class="text-right">
            <button class="btn btn-primary btn-sm" onClick="logout()" name="logout" readonly>
                <%=username%>  
                <i class="glyphicon glyphicon-log-out"></i>
            </button>                
        </div>
        <script>
            function logout(){
                 $.ajax({url: "${pageContext.request.contextPath}/config/AdminPanel?logout",
                    type: 'GET',
                    success: function(){
                        window.location.replace("/");
                    },
                    error: function () {
                        window.location.replace("/config/AdminPanel");
                    }
                });
            }
        </script>
                    
        <!--<form  action="${pageContext.request.contextPath}/config/AdminPanel"
                method="get">
            <div class="text-right">
                <button class="btn btn-primary btn-sm" type="submit" name="logout" readonly>
                    <%=username%>  
                    <i class="glyphicon glyphicon-log-out"></i>
                </button>                
            </div>
        </form>-->
        <script>
            sessionStorage.setItem("counter", "1");
            function CreateTableLine(id, name, passw, role) {
                var element = document.getElementById("table");
                var counter = sessionStorage.getItem("counter");                
                var line = document.createElement("tr");
                line.setAttribute("id", "line" + id);
                var num = document.createElement("td");
                num.innerHTML = counter;
                sessionStorage.setItem("counter", ++counter);
                var user = document.createElement("td");
                var pass = document.createElement("td");
                var sel = document.createElement("td");
                var rmB = document.createElement("td");
                
                /*var user_input = $("<input type='text' >").on('click', function() {
                    selectAll();
                })*/
                
                var user_input = document.createElement("input");
                user_input.setAttribute("type", "text");
                user_input.setAttribute("id", "login" + id);
                user_input.setAttribute("name", "login");
                user_input.setAttribute("onClick", "selectAll(\"login" + id + "\");");
                user_input.setAttribute("onChange", "changeSaveIcon();");
                user_input.setAttribute("class", "input-small");
                user_input.setAttribute("placeholder", "Login");
                user_input.setAttribute("value", name);
                user_input.setAttribute("style", "height: 26px");
                var pass_input = document.createElement("input");
                pass_input.setAttribute("type", "text");
                pass_input.setAttribute("id", "password" + id);
                pass_input.setAttribute("name", "password");
                pass_input.setAttribute("onClick", "selectAll(\"password" + id + "\");");
                pass_input.setAttribute("onChange", "changeSaveIcon();");
                pass_input.setAttribute("class", "input-small");
                pass_input.setAttribute("placeholder", "Password");
                pass_input.setAttribute("value", passw);
                pass_input.setAttribute("style", "height: 26px");
                var select = document.createElement("select");
                select.setAttribute("id", "role" + id);
                select.setAttribute("name", "role");
                select.setAttribute("style", "height: 26px");
                select.setAttribute("onChange", "changeSaveIcon();");
                if (id === 1) {
                    select.setAttribute("disabled", "true");
                }
                
                var opt1 = document.createElement("option");
                var opt2 = document.createElement("option");
                opt1.setAttribute("value", "admin");
                opt2.setAttribute("value", "user");
                opt1.innerHTML = "admin";
                opt2.innerHTML = "user";
                if ("admin" === role) {
                    opt1.setAttribute("selected", "true");
                }
                else {
                    opt2.setAttribute("selected", "true");
                }
                var rmvBtn = document.createElement("button");
                rmvBtn.setAttribute("class", "btn btn-primary btn-xs glyphicon glyphicon-minus");
                //<!--rmvBtn.setAttribute("class", "btn btn-primary btn-sm");-->
                if(name===""){
                    rmvBtn.setAttribute("onClick", "removeUser(" + id + ");");
                }
                else
                {
                    rmvBtn.setAttribute("onClick", "removeUser(" + id + ");");
                }
                rmvBtn.setAttribute("readonly", "true");
                var hid = document.createElement("input");
                hid.setAttribute("type", "hidden");
                hid.setAttribute("id", "id");
                hid.setAttribute("value", "" + id);
                hid.setAttribute("name", "id");
                user.appendChild(user_input);
                pass.appendChild(pass_input);
                select.appendChild(opt1);
                select.appendChild(opt2);
                sel.appendChild(select);
                if (id !== 1) {
                    rmB.appendChild(rmvBtn);
                }

                line.appendChild(num);
                line.appendChild(user);
                line.appendChild(pass);
                line.appendChild(select);
                line.appendChild(rmB);
                line.appendChild(hid);                
                element.appendChild(line);
            }
        </script> 
        <div class="container">
            <h3>Administration Panel</h3>
            <ul class="nav nav-pills nav-justified">
                <li class="active"><a href="/config/AdminPanel">Administration Panel</a></li>
                <li><a href="/config/SystemSettings">System
                        Settings</a></li>
                <li><a href="/config/DriversInstalled">Drivers</a></li>
            </ul>
            <form
                action="${pageContext.request.contextPath}/config/AdminPanel"
                method="post">
                <div class="small-table">
                    <h2>Administration Panel</h2>
                    <table class="table table-hover" id="table">
                        <tr>
                        <tr>
                            <th>#</th>
                            <th>Login</th>
                            <th>Password</th>
                            <th>Role</th>
                            <th/>
                        </tr>

                        <%
                            int max = -1;
                            List<Credentials> list = (List<Credentials>) request.getAttribute("credentials");
                            if (list != null && !list.isEmpty())
                                for (Credentials user : list) {
                                    if (max < user.getId()) {
                                        max = user.getId();
                                    }

                        %>                                                            
                        <script type="text/javascript">
                            CreateTableLine(<%=user.getId()%>, "<%=user.getLogin()%>", "<%=user.getPassword()%>", "<%=user.getRole()%>");</script>
                            <%}%>
                        <script>sessionStorage.setItem("max",<%=max%>);</script>
                    </table>
                    <div class="text-center">
                        <a class="btn btn-primary " onClick="addNewUser()" readonly id="add"/>
                            <i class="glyphicon glyphicon-plus"> </i> 
                            Append new user
                        </a>
                    </div>
                </div>

                <div class="text-right">
                    <button class="btn btn-primary btn-sm" type="submit" name="save" id="save" readonly>
                        <i class="glyphicon glyphicon-floppy-saved"></i>
                        Save
                    </button>
                </div>
            </form>
        </div>
        <script>
            function selectAll(id) {
                document.getElementById(id).focus();
                document.getElementById(id).select();
            }
            
            function addNewUser() {
                var max = sessionStorage.getItem("max");
                max++;
                CreateTableLine(max, "", "", "user");
                sessionStorage.setItem("max", max);
                changeSaveIcon();
            }
            
            function removeUser(id) {                
                var line = "line" + id;                
                var element2 = document.getElementById(line);
                var root = document.getElementById("table");
                var counter = element2.children[0].innerHTML;                
                element2.parentNode.removeChild(element2);
                for (; counter < root.childElementCount; counter++) {
                    root.children[counter].children[0].innerHTML = counter;
                }
                sessionStorage.setItem("counter", counter);
                changeSaveIcon();
            }

            function getUserId(element) {
                for (var i = 0; i < element.childElementCount; i++) {
                    if (element.children[i].id === "id") {
                        return element.children[i].value;
                    }
                }
                return null;
            }
            
            function changeSaveIcon(){
                document.getElementById("save").children[0].setAttribute("class","glyphicon glyphicon-floppy-disk");
            }
        </script>
    </body>
</html>