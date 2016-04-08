<%@page import="com.github.jsonldjava.utils.JsonUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.util.Iterator"%>
<%@page import="ru.semiot.platform.apigateway.config.BundleConstants"%>
<%@page import="javax.json.Json"%>

<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setHeader("Expires", "0");

    JsonArray jsonBundles = (JsonArray) request.getAttribute("jsonArray");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | Installed Drivers</title>
    <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/css/common.css">
    <link rel="stylesheet" href="/static/css/drivers.css">
</head>
<body>

    <div class="navbar navbar-default">
        <div class="container-fluid container">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/">SemIoT Platform</a>
            </div>
            <ul class="nav navbar-nav">
                <li>
                    <a href="/explorer">Explorer</a>
                </li>
                <li class="dropdown active">
                    <a data-target="#" class="dropdown-toggle" data-toggle="dropdown">Configuration
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header">Drivers</li>
                        <li><a href="/config/DriversInstalled">Installed</a></li>
                        <li><a href="/config/AvailableDrivers">Available</a></li>
                        <li><a href="/config/UploadDriver">New</a></li>
                        <li class="divider"></li>
                        <li class="dropdown-header">Settings</li>
                        <li><a href="/config/SystemSettings">System</a></li>
                        <li><a href="/config/AdminPanel">Users</a></li>
                    </ul>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a data-target="#" class="dropdown-toggle" data-toggle="dropdown"><%=request.getRemoteUser()%>
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/config/AdminPanel?logout">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="container">
        <div class="main-wrapper">
            <h3>Installed drivers</h3>
            <%
            if (jsonBundles.size() <= BundleConstants.countDefaultBundles) {
            %>
                <p>No installed drivers found; check out one of <a href="/config/AvailableDrivers">available drivers</a></p>
            <%
                } else {
            %>
                <form action="${pageContext.request.contextPath}/config/DriversInstalled" method="post">
                    <div class="table-responsive system-list">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>
                                        <label>№</label>
                                    </th>
                                    <th>
                                        <label>Name</label>
                                    </th>
                                    <th>
                                        <label>Action</label>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    int j = 1;
                                    for (int i = 0; i < jsonBundles.size(); i++) {
                                        JsonObject obj = jsonBundles.getJsonObject(i);
                                        int id = obj.getInt("id");
                                        if (id >= BundleConstants.countDefaultBundles) {
                                            String symbName = obj.getString("symbolicName");
                                %>
                                            <tr>
                                                <td>
                                                    <span><%=j++%></span>
                                                <td>
                                                    <span><%=obj.get("name")%></span>
                                                <td>
                                                    <a
                                                        href="#myModal"
                                                        class="btn btn-primary btn-sm uninstall"
                                                        data-id="<%=symbName%>"
                                                    >
                                                        uninstall
                                                    </a>
                                                </td>
                                            </tr>
                                <%
                                        }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                    <input type="hidden" name="id_bundle" id="id_bundle" />
                    <input type="hidden" class="remove-with-data" />
                </form>
            <%
                }
            %>
        </div>
    </div>


    <div class="modal fade" id="myModal" tabindex="-1" role="dialog">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">Remove driver</h4>
          </div>
          <div class="modal-body">
            <p>Do you want to remove all data related to this driver?</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary modal-decline">No</button>
            <button type="button" class="btn btn-warning modal-accept">Yes</button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->


    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/js/material.min.js"></script>
    <script>$.material.init();</script>
    <script>
        function removeDriver(id, withData) {
            console.log('removing driver with id = ', id);
            $('#id_bundle').val(id);
            // ‎(╯°□°)╯︵ ┻━┻
            if (withData) {
                $('.remove-with-data').attr({
                    name: 'uninstallWithDeleteData',
                    value: 'yes'
                });
            } else {
                $('.remove-with-data').attr({
                    name: 'uninstall',
                    value: 'no'
                });
            }
            $('form').submit();
        }
        $('.uninstall').on('click', function() {
            var driverId = $(this).attr('data-id');
            $('.modal-accept').off().on('click', function() {
                removeDriver(driverId, true)
            });
            $('.modal-decline').off().on('click', function() {
                removeDriver(driverId, false)
            });
            $('#myModal').modal({});
        });
    </script>
</body>
</html>

