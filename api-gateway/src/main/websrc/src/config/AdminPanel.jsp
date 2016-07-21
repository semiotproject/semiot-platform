<%@page import="java.util.List"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="ru.semiot.platform.apigateway.utils.Credentials"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | Admin Panel</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/settings.css">
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
                    <a href="/systems">Explorer</a>
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
                        <li><a href="/user/logout">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="container">
        <div class="main-wrapper">
            <form action="${pageContext.request.contextPath}/config/AdminPanel" method="post">
                <h3>
                    <span>Users</span>
                    <button class="btn btn-raised btn-primary show-modal">
                        Add <i class="fa fa-plus"></i>
                    </button>
                    <button type="submit "class="btn btn-raised btn-primary" name="save">
                        Save <i class="fa fa-save"></i>
                    </button>
                </h3>
                <div class="table-responsive system-list">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>
                                    <label>Login</label>
                                </th>
                                <th>
                                    <label>Password</label>
                                </th>
                                <th>
                                    <label>Role</label>
                                </th>
                                <th>
                                    <label>Action</label>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                List<Credentials> list = (List<Credentials>) request.getAttribute("credentials");
                                if (list != null && !list.isEmpty())
                                    for (Credentials user : list) {
										if(user.getRole().equals("internal"))
											continue;
                            %>
                                        <tr>
                                            <td>
                                                <div class="form-group form-group-sm is-empty">
                                                    <input class="form-control"
                                                        placeholder="login"
                                                        name="login"
                                                        type="text"
                                                        id="inputSmall"
                                                        placeholder="ss"
                                                        value=<%=user.getLogin()%>
                                                    >
                                                </div>
                                            </td>
                                            <td>
                                                <div class="form-group form-group-sm is-empty">
                                                    <input class="form-control"
                                                        placeholder="password"
                                                        name="password"
                                                        type="password"
                                                        id="inputSmall"
                                                        placeholder="ss"
                                                        value=<%=user.getPassword()%>
                                                    >
                                                </div>
                                            </td>
                                            <td>
                                                <div class="form-group form-group-sm is-empty">
                                                    <select name="role" class="form-control" value=<%=user.getRole()%>>
                                                        <option value="user" <%= (user.getRole().equals("user") ? "selected='selected'" : "") %>>user</option>
                                                        <option value="admin" <%= (user.getRole().equals("admin") ? "selected='selected'": "") %>>admin</option>
                                                    </select>
                                                </div>
                                            </td>
                                            <input type="hidden" id="id" value=<%=user.getId()%> name="id">
                                            <%
                                                if (user.getId() != 1) {
                                            %>
                                                <td>
                                                    <button class="btn btn-fab btn-fab-mini remove-user" data-id="<%=user.getId()%>">
                                                        <i class="fa fa-remove"></i>
                                                    </button>
                                                </td>
                                            <%
                                                }
                                            %>
                                        </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            </form>
        </div>
    </div>

    <div class="user-template" style="display: none;">
        <table>
            <tbody>
                <tr>
                    <td>
                        <div class="form-group form-group-sm is-empty">
                            <input class="form-control"
                                placeholder="login"
                                name="login"
                                type="text"
                                id="inputSmall"
                                placeholder="ss"
                            >
                        </div>
                    </td>
                    <td>
                        <div class="form-group form-group-sm is-empty">
                            <input class="form-control"
                                placeholder="password"
                                name="password"
                                type="password"
                                id="inputSmall"
                                placeholder="ss"
                            >
                        </div>
                    </td>
                    <td>
                        <div class="form-group form-group-sm is-empty">
                            <select name="role" class="form-control">
                                <option value="admin">admin</option>
                                <option value="user">user</option>
                            </select>
                        </div>
                    </td>
                    <input type="hidden" id="id" name="id">
                    <td>
                        <button class="btn btn-fab btn-fab-mini remove-user" data-id="0">
                            <i class="fa fa-remove"></i>
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div class="modal fade" id="myModal" tabindex="-1" role="dialog">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">Add user</h4>
          </div>
          <div class="modal-body">
             <div class="form-group label-floating">
                <label for="add-user-login" class="control-label">Username</label>
                <input type="text" class="form-control" id="add-user-login">
            </div>
             <div class="form-group label-floating">
                <label for="add-user-password" class="control-label">Password</label>
                <input type="text" class="form-control" id="add-user-password">
            </div>
             <div class="form-group label-floating">
                <label for="add-user-role" class="control-label">Role</label>
                <select id="add-user-role" class="form-control" value="admin">
                    <option value="admin">admin</option>
                    <option value="user">user</option>
                </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default modal-decline" data-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary modal-accept add-user-button">Add</button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/js/material.min.js"></script>
    <script>$.material.init();</script>
    <script>

        // soooooooo wasted....
        // 2016 year
        // web 7.0
        var MAX_ID = 0;
        function checkMaxId() {
            MAX_ID = 0;
            $('form').find('input[type=hidden]').each(function(index, input) {
                if (MAX_ID < input.value) {
                    MAX_ID = input.value
                }
            });
            console.info('max id is ', MAX_ID);
        }
        checkMaxId();

        function getUserRow(user) {
            var template = $('.user-template').find('tr').clone();

            template.find('[name=login]').val(user.login);
            template.find('[name=password]').val(user.password);
            template.find('[name=role]').val(user.role);
            template.find('[name=id]').val(user.id);
            template.find('[data-id]').attr({ 'data-id': user.id});

            return template;
        }

        $('.remove-user').on('click', function() {
            var id = $(this).attr('data-id');
            console.info('removing user with id = ', id);
            $(this).parents('tr').remove();
            checkMaxId();
        })
        $('.show-modal').on('click', function(e) {
            e.preventDefault();
            console.info('showing modal');
            $('#myModal').modal({});
        })
        $('.add-user-button').on('click', function() {
            var user = {
                id: parseInt(MAX_ID) + 1,
                login: $('#add-user-login').val(),
                password: $('#add-user-password').val(),
                role: $('#add-user-role').val()
            };
            console.info('adding new user: ', user);

            $('.table').append(getUserRow(user));

            checkMaxId();
            $('#myModal').modal('hide');
        })
    </script>
</body>
</html>

