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
                var user_input = document.createElement("input");
                user_input.setAttribute("type", "text");
                user_input.setAttribute("id", "login" + id);
                user_input.setAttribute("onClick", "SelectAll(\"login" + id + "\");");
                user_input.setAttribute("onChange", "changeSaveBtn(true)");
                user_input.setAttribute("class", "input-small");
                user_input.setAttribute("placeholder", "Login");
                user_input.setAttribute("value", name);
                user_input.setAttribute("style", "height: 26px");
                var pass_input = document.createElement("input");
                pass_input.setAttribute("type", "text");
                pass_input.setAttribute("id", "password" + id);                
                pass_input.setAttribute("onClick", "SelectAll(\"password" + id + "\");");
                pass_input.setAttribute("onChange", "changeSaveBtn(true)");
                pass_input.setAttribute("class", "input-small");
                pass_input.setAttribute("placeholder", "Password");
                pass_input.setAttribute("value", passw);
                pass_input.setAttribute("style", "height: 26px");
                var select = document.createElement("select");
                select.setAttribute("id", "role" + id);
                select.setAttribute("style", "height: 26px");
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
                rmvBtn.setAttribute("class", "btn-primary btn-small glyphicon glyphicon-minus");
                //<!--rmvBtn.setAttribute("class", "btn btn-primary btn-sm");-->
                if(name===""){
                    rmvBtn.setAttribute("onClick", "RemoveUser(" + id + ", true);");
                }
                else
                {
                    rmvBtn.setAttribute("onClick", "RemoveUser(" + id + ", false);");
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
                        <a class="btn btn-primary " onClick="AddNewUser()" readonly id="add"/>
                            <i class="glyphicon glyphicon-plus"> </i> 
                            Append new user
                        </a>
                    </div>
                </div>

                <div class="text-right">
                    <a class="btn btn-primary btn-sm" onClick ="Update()" name="save" id="save" readonly/>
                        <i class="glyphicon glyphicon-floppy-saved"></i>
                        Save
                    </a>
                </div>
            </form>
        </div>
        <script>
            function SelectAll(id) {
                document.getElementById(id).focus();
                document.getElementById(id).select();
            }
            
            function AddNewUser() {
                var max = sessionStorage.getItem("max");
                max++;
                CreateTableLine(max, "", "", "user");
                /*
                $.ajax({url: "${pageContext.request.contextPath}/config/AdminPanel",
                    type: 'post',
                    contentType: 'application/json; charset=utf-8',
                    data: {"id": max, "login": "default", "password": "default", "role": "user"},
                    error: function () {
                        window.location.replace("/config/AdminPanel");
                    }
                });
                */
                changeSaveBtn(true);
                sessionStorage.setItem("max", max);
            }
            
            function RemoveUser(id, isNew) {
                
                var line = "line" + id;                
                var element2 = document.getElementById(line);
                var root = document.getElementById("table");
                var counter = element2.children[0].innerHTML;                
                element2.parentNode.removeChild(element2);
                
                if(!isNew){
                    $.ajax({url: "${pageContext.request.contextPath}/config/AdminPanel?id=" + id,
                        type: 'DELETE',
                        contentType: 'application/json; charset=utf-8',
                        error: function () {
                            window.location.replace("/config/AdminPanel");
                        }
                    });
                }
                for (; counter < root.childElementCount; counter++) {
                    root.children[counter].children[0].innerHTML = counter;
                }
                sessionStorage.setItem("counter", counter);
            }
            
            function Update() {
                var template = "{\"id\":\"{0}\",\"login\":\"{1}\",\"password\":\"{2}\",\"role\":\"{3}\"},";
                var root = document.getElementById("table");
                var payload = "[";

                for (var i = 1; i < root.childElementCount; i++) {
                    var id = getUserId(root.children[i]);
                    var login = document.getElementById("login" + id).value;
                    var pass = document.getElementById("password" + id).value;
                    var role = document.getElementById("role" + id).value;
                    payload += template.replace("{0}", id).replace("{1}", login).replace("{2}", pass).replace("{3}", role);
                }
                payload += "]";
                //console.log(payload);
                //changeSaveBtn(false);
                $.ajax({url: "${pageContext.request.contextPath}/config/AdminPanel",
                    type: 'put',
                    contentType: 'application/json; charset=utf-8',
                    data: payload,
                    success: function(xhr){
                        changeSaveBtn(false);
                    },
                    error: function (xhr) {
                        if(xhr.status === 400){
                            var htmlText = xhr.responseText;
                            var text = htmlText.split("body")[1];
                            text = text.substr(1,text.length-3);
                            console.log(text);
                            changeSaveBtn(true);
                            alert(text);
                        }
                        else{
                            window.location.replace("/config/AdminPanel");
                        }
                        
                    }
                });                
            }

            function getUserId(element) {
                for (var i = 0; i < element.childElementCount; i++) {
                    if (element.children[i].id === "id") {
                        return element.children[i].value;
                    }
                }
                return null;
            }
            
            function changeSaveBtn(flag){
                if(flag){
                    document.getElementById("save").children[0].setAttribute("class","glyphicon glyphicon-floppy-disk");
                }
                else{
                    document.getElementById("save").children[0].setAttribute("class","glyphicon glyphicon-floppy-saved");
                }
            }                    
        </script>
    </body>
</html>
