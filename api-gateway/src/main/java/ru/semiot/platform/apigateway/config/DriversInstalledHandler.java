package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.apigateway.ExternalQueryService;
import ru.semiot.platform.apigateway.OSGiApiService;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import rx.Observable;

@WebServlet(urlPatterns = "/config/DriversInstalled", asyncSupported = true)
public class DriversInstalledHandler extends HttpServlet {

	public static final String queryIdSystemsForDriver = "SELECT DISTINCT ?id "
			+ "WHERE { " + "GRAPH <urn:semiot:graphs:private> {"
			+ "?system  semiot:hasDriver <${DRIVER}> } "
			+ "?system  dcterms:identifier ?id. }";

	private static final Logger logger = LoggerFactory
			.getLogger(DriversInstalledHandler.class);

	@Inject
	OSGiApiService service;

	@Inject
	SPARQLQueryService query;

	@Inject
	ExternalQueryService externalService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		final AsyncContext ctx = req.startAsync();

		Observable<JsonArray> jsonArray = service.getBundlesJsonArray();

		jsonArray.subscribe((jsArray) -> {
			ctx.getRequest().setAttribute("jsonArray", jsArray);
		}, (Throwable e) -> {
			logger.warn(e.getMessage(), e);
		}, () -> {
			ctx.dispatch("/configuration/DriversInstalled");
		});

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, String> parameters = getRequestParameters(request);
		String pid = parameters.get("id_bundle");
		if (StringUtils.isNotBlank(pid)) {
			if (request.getParameter("uninstall") != null) {
				final AsyncContext ctx = request.startAsync();
				Observable<String> unistall = service.sendPostUninstall(pid);
				unistall.map((__) -> {
					Observable<JsonArray> jsonArray = service
							.getBundlesJsonArray();
					JsonArray jsArray = jsonArray.toBlocking().single();
					ctx.getRequest().setAttribute("jsonArray", jsArray);
					return jsArray;
				}).subscribe(ConfigHelper.dispatch(ctx,
						"/configuration/DriversInstalled"));
			} else if (request
					.getParameter("uninstallWithDeleteData") != null) {
				final AsyncContext ctx = request.startAsync();
				Observable<String> unistall = service.sendPostUninstall(pid);
				unistall.map((__) -> {
					Observable<ResultSet> obsSystemsRS = query.select(
							queryIdSystemsForDriver.replace("${DRIVER}", pid));
					ResultSet systemsRS = obsSystemsRS.toBlocking().single();
					while (systemsRS.hasNext()) {
						QuerySolution qs = systemsRS.next();
						Literal id = qs.getLiteral("id");
						externalService.sendRsRemoveFromTsdb(id.toString())
								.subscribe();
					}
					externalService.sendRsRemoveFromFuseki(pid).subscribe();
					Observable<JsonArray> jsonArray = service
							.getBundlesJsonArray();
					JsonArray jsArray = jsonArray.toBlocking().single();
					ctx.getRequest().setAttribute("jsonArray", jsArray);
					return jsArray;
				}).subscribe(ConfigHelper.dispatch(ctx,
						"/configuration/DriversInstalled"));
			}
		}
	}

	private static HashMap<String, String> getRequestParameters(
			HttpServletRequest request) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		Enumeration _enum = request.getParameterNames();
		while (_enum.hasMoreElements()) {
			String key = (String) _enum.nextElement();
			String value = request.getParameter(key);
			// request.get
			parameters.put(key, value);
			System.out.println(key + " " + value);
		}
		return parameters;
	}

}
