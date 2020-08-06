package tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class getJSON extends HttpServlet {

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		try {
			// parse params
			String req_uri = "";
			if (request.getParameter("uri") != null) {
				req_uri = request.getParameter("uri");
			}
			String req_format = "";
			if (request.getParameter("format") != null) {
				req_format = request.getParameter("format");
			}
			// parse header
			String acceptHeader = "application/json";
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				key = key.toLowerCase();
				String value = request.getHeader(key);
				if (key.equals("accept")) {
					System.out.println(key + " " + value);
				}
			}
			req_uri = URLDecoder.decode(req_uri, "UTF-8");
			// call GeoNames API
			String geonamesid = req_uri.replace("http://sws.geonames.org/", "");
			req_uri = "http://api.geonames.org/get?geonameId=" + geonamesid + "&username=mainzed.labeling";
			URL obj = new URL(req_uri);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			String urlParameters = "";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			Double lat = -1000.0;
			Double lng = -1000.0;
			String name = null;
			Double west = -1000.0;
			Double north = -1000.0;
			Double east = -1000.0;
			Double south = -1000.0;
			JSONObject jsonObject = new JSONObject();
			if (con.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
				String inputLine;
				StringBuilder responseGeoNames = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					responseGeoNames.append(inputLine);
				}
				in.close();
				//
				String output = XML.toJSONObject(responseGeoNames.toString()).toString();
				jsonObject = (JSONObject) new JSONParser().parse(output);
				int startTagLat = responseGeoNames.indexOf("<lat>");
				int endTagLat = responseGeoNames.indexOf("</lat>");
				if (startTagLat != -1 && endTagLat != -1) {
					lat = Double.parseDouble(responseGeoNames.substring(startTagLat, endTagLat).replace("<lat>", ""));
				}
				int startTagLng = responseGeoNames.indexOf("<lng>");
				int endTagLng = responseGeoNames.indexOf("</lng>");
				if (startTagLng != -1 && endTagLng != -1) {
					lng = Double.parseDouble(responseGeoNames.substring(startTagLng, endTagLng).replace("<lng>", ""));
				}
				int startTagName = responseGeoNames.indexOf("<name>");
				int endTagName = responseGeoNames.indexOf("</name>");
				if (startTagName != -1 && endTagName != -1) {
					name = responseGeoNames.substring(startTagName, endTagName).replace("<name>", "");
				}

				int startTagWest = responseGeoNames.indexOf("<west>");
				int endTagWest = responseGeoNames.indexOf("</west>");
				if (startTagWest != -1 && endTagWest != -1) {
					west = Double.parseDouble(responseGeoNames.substring(startTagWest, endTagWest).replace("<west>", ""));
				}
				int startTagNorth = responseGeoNames.indexOf("<north>");
				int endTagNorth = responseGeoNames.indexOf("</north>");
				if (startTagNorth != -1 && endTagNorth != -1) {
					north = Double.parseDouble(responseGeoNames.substring(startTagNorth, endTagNorth).replace("<north>", ""));
				}

				int startTagEast = responseGeoNames.indexOf("<east>");
				int endTagEast = responseGeoNames.indexOf("</east>");
				if (startTagEast != -1 && endTagEast != -1) {
					east = Double.parseDouble(responseGeoNames.substring(startTagEast, endTagEast).replace("<east>", ""));
				}
				int startTagSouth = responseGeoNames.indexOf("<south>");
				int endTagSouth = responseGeoNames.indexOf("</south>");
				if (startTagSouth != -1 && endTagSouth != -1) {
					south = Double.parseDouble(responseGeoNames.substring(startTagSouth, endTagSouth).replace("<south>", ""));
				}
			}
			if (acceptHeader.contains("application/vnd.geo+json") || req_format.equals("geojson")) {
				// geometry
				// lat / lon
				JSONObject geometry = new JSONObject();
				JSONArray coordinatesArray = new JSONArray();
				JSONObject geometryObject = new JSONObject();
				coordinatesArray.add((double) lng);
				coordinatesArray.add((double) lat);
				geometryObject.put("type", "Point");
				geometryObject.put("coordinates", coordinatesArray);
				JSONArray geometriesArray = new JSONArray();
				geometriesArray.add(geometryObject);
				// bbox
				geometry = new JSONObject();
				coordinatesArray = new JSONArray();
				JSONArray coordinatesArray2 = new JSONArray();
				geometryObject = new JSONObject();
				JSONArray bboxpoint = new JSONArray();
				bboxpoint.add((double) west);
				bboxpoint.add((double) north);
				coordinatesArray2.add(bboxpoint);
				bboxpoint = new JSONArray();
				bboxpoint.add((double) west);
				bboxpoint.add((double) south);
				coordinatesArray2.add(bboxpoint);
				bboxpoint = new JSONArray();
				bboxpoint.add((double) east);
				bboxpoint.add((double) south);
				coordinatesArray2.add(bboxpoint);
				bboxpoint = new JSONArray();
				bboxpoint.add((double) east);
				bboxpoint.add((double) north);
				coordinatesArray2.add(bboxpoint);
				bboxpoint = new JSONArray();
				bboxpoint.add((double) west);
				bboxpoint.add((double) north);
				coordinatesArray2.add(bboxpoint);
				geometryObject.put("type", "Polygon");
				coordinatesArray.add(coordinatesArray2);
				geometryObject.put("coordinates", coordinatesArray);
				geometriesArray.add(geometryObject);
				// GeometryCollection
				geometry.put("type", "GeometryCollection");
				geometry.put("geometries", geometriesArray);
				// geojson
				JSONObject geojson = new JSONObject();
				geojson.put("type", "FeatureCollection");
				JSONArray features = new JSONArray();
				JSONObject feature = new JSONObject();
				feature.put("type", "Feature");
				feature.put("properties", jsonObject);
				feature.put("geometry", geometry);
				features.add(feature);
				geojson.put("features", features);
				out.print(geojson);
			} else {
				out.print(jsonObject);
			}
		} catch (Exception e) {
			out.print(Logging.getMessageJSON(e, "tools.getJSON"));
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Get JSON of a GeoNames ID.";
	}

}
