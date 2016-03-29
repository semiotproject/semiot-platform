<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="java.util.Iterator"%>

<%
	String keyType = "@type";
	String commonSchemaType = "semiot:CommonSchema";
	String repeatableSchemaType = "semiot:RepeatableSchema";

	JsonObject jsonConfig = (JsonObject) session.getAttribute("jsonConfig");
	String pid = jsonConfig.getString("semiot:driverPid");
	JsonArray jsonArray = jsonConfig.getJsonArray("semiot:view");
	Integer repeatbleMax = jsonConfig.getInt("semiot:maxRepeatableSchemas");

	session.setAttribute("pid", pid);

	int j = 0;

	JsonObject repeatbleJson = null;
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Configuration Driver</title>
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
		<h3>Configuration Driver</h3>
		<form
			action="${pageContext.request.contextPath}/config/ConfigurationDriver"
			method="post" id="mainForm">
			<div class="panel panel-default">
				<div class="panel-body">
					<div class="small-table">
						<table class="table table-hover" id="table">
							<tr>
							<tr>
								<th>Name</th>
								<th>Value</th>
							</tr>
							<%
								if (jsonArray != null) {
									for (int i = 0; i < jsonArray.size(); i++) {
										JsonObject jo = jsonArray.getJsonObject(i);
										Set<String> it = jo.keySet();
										if (jo.getString(keyType).equals(commonSchemaType)) {
											// int j = 0;
											for (String key : it) {
												if (!key.equals(keyType)) {
													JsonObject jsonProperty = jo.getJsonObject(key);
													String type = jsonProperty.getString(keyType);
													if (type.equals("semiot:GeoRectangle")) {
														Set<String> it1 = jsonProperty.keySet();
														// int j = 0;
														for (String key1 : it1) {
															if (!key1.equals(keyType) && !key1.equals("rdfs:label")) {
																JsonObject jsonProperty1 = jsonProperty.getJsonObject(key1);
																String name = null;
																String label = null;
																if (key1.equals(key + ".latitude_ne")) {
																	name = key + ".1.latitude";
																	label = "Latitude of fist point";
																}
																if (key1.equals(key + ".longitude_ne")) {
																	name = key +  ".1.longitude";
																	label = "Longitude of fist point";
																}
																if (key1.equals(key + ".latitude_sw")) {
																	name = key +  ".2.latitude";
																	label = "Latitude of second point";
																}
																if (key1.equals(key + ".longitude_sw")) {
																	name = key +  ".2.longitude";
																	label = "Longitude of second point";
																}
																++j;
																%>
																<td><%=label%>
																<td><input type="text" id="txtfld<%=j%>"
																	onClick="SelectAll('txtfld<%=j%>');" name=<%=name%>
																	style="width: 200px"
																	value=<%=jsonProperty1.get("dtype:defaultValue")%> />
																</tr>
																<%
															}
														}
													} else {
														++j;
														%>
														<td><%=jsonProperty.getString("rdfs:label")%>
														<td><input type="text" id="txtfld<%=j%>"
															onClick="SelectAll('txtfld<%=j%>');" name=<%=key%>
															style="width: 200px"
															value=<%=jsonProperty.get("dtype:defaultValue")%> />
														</tr>
														<%
													}
												}
											}
										} else if (jo.getString(keyType).equals(repeatableSchemaType)) {
											repeatbleJson = jo;
										}
									}
								}
								if (j < 1) {
							%>
							<tr>
								<td>Configuration is not found.</td>
							</tr>
							<%
								}
							%>
						</table>
					</div>
				</div>
			</div>
			<div class="text-center" id="divAdd">
				<a class="btn btn-primary btn-sm" id="add"><i
					class="glyphicon glyphicon-plus"></i> Add repeatable configuration</a>
				<br>
				<br />
			</div>


			<div class="text-right">
				<%
					if (j > 0) {
				%>
				<input class="btn btn-primary btn-sm" type="submit" name="save"
					value="Save and start" />
				<%
					}
				%>
				<input class="btn btn-primary btn-sm" type="submit" name="back"
					value="Back" /> <br>
				<br />
			</div>
		</form>

		<script>
			var indexPanel = 0;
			var countPanel = 0;

			var json = jQuery.parseJSON('<%=repeatbleJson%>');
			var maxRepeatble =<%=repeatbleMax%>;

			addPanel(false);

			$("#add").click(function() {
				addPanel(true);
			});

			function addPanel(withRemove) {
				++indexPanel;
				++countPanel;
				var remButton = '';
				if (withRemove) {
					remButton = '<div class="pull-right"><button id="remove" class="btn btn-primary btn-xs glyphicon glyphicon-minus" onClick="removePanel('
							+ indexPanel + ');"></button></div>';
				}
				var panel = '<div class="panel panel-default" id="panel'+ indexPanel + '"> <div class="panel-body">'
						+ remButton
						+ '<table class="table table-hover" id="table'+indexPanel +'"><tbody></tbody></table</div> </div>';
				$("#divAdd").after(panel);

				AddCommonRawToTable('Name', 'Value');
				for (key in json) {
					if (key != '@type') {
						var val = json[key];
						if (val['@type'] == 'semiot:GeoRectangle') {
							var lat1 = val[key + '.latitude_ne']['dtype:defaultValue'];
							var long1 = val[key + '.longitude_ne']['dtype:defaultValue'];
							var lat2 = val[key + '.latitude_sw']['dtype:defaultValue'];
							var long2 = val[key + '.longitude_sw']['dtype:defaultValue'];
							AddRectangleToTable(key, lat1, long1, lat2, long2);
						} else {
							AddInputRawToTable(key, val['rdfs:label'],
									val['dtype:defaultValue']);
						}
					}
				}

				if (maxRepeatble <= countPanel) {
					$('#add').addClass('disabled');
				}
				$("#mainForm").trigger('pagecreate');
			}

			function removePanel(id) {
				$('#panel' + id).remove();
				--countPanel;
				if (maxRepeatble - 1 == countPanel) {
					$('#add').removeClass('disabled');
				}
			}

			// all for repeatble
			function AddCommonRawToTable(name, defaultValue) {
				var row = '<tr><th>' + name + '</th><th>' + defaultValue
						+ '</th></tr>';
				$('#table' + indexPanel + ' > tbody:last-child').append(row);
			}

			function AddInputRawToTable(keyWithoutIndex, name, defaultValue) {
				key = indexPanel + '.' + keyWithoutIndex;
				var row = '<tr><td>' + name
						+ '</td><td><input type="text" id="' + key
						+ '" onClick="SelectAll(' + key + ');" name=' + key
						+ ' style="width: 200px" value=' + defaultValue
						+ ' /></td></tr>';
				$('#table' + indexPanel + ' > tbody:last-child').append(row);
			}

			function AddInputRawToTable(keyWithoutIndex, name, defaultValue) {
				key = indexPanel + '.' + keyWithoutIndex;
				var row = '<tr><td>' + name
						+ '</td><td><input type="text" id="' + key
						+ '" onClick="SelectAll(' + key + ');" name=' + key
						+ ' style="width: 200px" value=' + defaultValue
						+ ' /></td></tr>';
				$('#table' + indexPanel + ' > tbody:last-child').append(row);
			}

			function AddRectangleToTable(key, lat1, long1, lat2, long2) {
				AddInputRawToTable(key + '.1.latitude',
						'Latitude of fist point', lat1);
				AddInputRawToTable(key + '.1.longitude',
						'Longitude of fist point', long1);
				AddInputRawToTable(key + '.2.latitude',
						'Latitude of second point', lat2);
				AddInputRawToTable(key + '.2.longitude',
						'Longitude of second point', long2);
			}

			function SelectAll(id) {
				document.getElementById(id).focus();
				document.getElementById(id).select();
			}
		</script>
</body>
</html>