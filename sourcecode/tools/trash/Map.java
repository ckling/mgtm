package geo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Map {

	private String output = "";
	private int size = 3;
	private BufferedWriter bw = null;

	public Map (double latCenter, double lonCenter, int level) {

		addText("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"+
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"+
				"  <head>\n"+
				"    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n"+
				"    <title>Google Maps JavaScript API Example: Map Markers</title>\n"+
				"    <script src=\"http://maps.googleapis.com/maps/api/js?sensor=true\"\n"+
				"            type=\"text/javascript\"></script>\n"+
				"    <script type=\"text/javascript\">\n"+
				"    \n"+

				"function get_GET_params() {"+
				"   var GET = new Array();"+
				"   if(location.search.length > 0) {"+
				"      var get_param_str = location.search.substring(1, location.search.length);"+
				"      var get_params = get_param_str.split(\"&\");"+
				"      for(i = 0; i < get_params.length; i++) {"+
				"         var key_value = get_params[i].split(\"=\");"+
				"         if(key_value.length == 2) {"+
				"            var key = key_value[0];"+
				"            var value = key_value[1];"+
				"            GET[key] = value;"+
				"         }"+
				"      }"+
				"   }"+
				"   return(GET);"+
				"}"+
				" \n"+
				"function get_GET_param(key) {"+
				"   var get_params = get_GET_params();"+
				"   if(get_params[key]){"+
				"      return(get_params[key]);"+
				"   } else {"+
				"      return false;}"+
				"}"+
				""+
				"function contains(array, value) {"+
				"for (var i = 0; i < array.length; i++) {"+
				"	if (array[i] == value) {"+
				"	return(true);"+
				"	}"+
				"}"+
				"return(false);"+
				"}"+

				"    function initialize() {\n"+
				"var topics = new Array();"+
				"var t = get_GET_param(\"t\");"+
				"if (t!=false) {"+
				"topics = t.split(\",\");"+
				"}\n" +				
				"            var myLatLng = new google.maps.LatLng("+latCenter+", "+lonCenter+");"+
				"  var myOptions = { "+
				"  zoom: "+level+","+
				"center:  myLatLng,"+
				"mapTypeId: google.maps.MapTypeId.SATELLITE"+
				"    };"+

" var map = new google.maps.Map(document.getElementById(\"map_canvas\"),"+
				"      myOptions);\n");

	}



	public String getMap() {

		try {
			addText("    }\n"+
					"\n"+
					"    </script>\n"+
					"  </head>\n"+
					"\n"+
					"  <body onload=\"initialize()\">\n"+
					"    <div id=\"map_canvas\" style=\"width: 1024px; height: 768px\"></div>\n"+
					"  </body>"+
					"</html>");

			if (bw != null) {
				bw.close();
			}
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;

	}

	public void size (int size) {
		this.size = size;
	}

	public void image (String image) {
		addText("		 var image = new google.maps.MarkerImage('"+image+"',"+
				// This marker is 20 pixels wide by 32 pixels tall.
				"		      new google.maps.Size("+size+", "+size+"),"+
				// The origin for this image is 0,0.
				"new google.maps.Point(0,0),"+
				// The anchor for this image is the base of the flagpole at 0,32.
				"new google.maps.Point(0, 0));\n");
	}

	public void add (double lati, double longi) {

		lati = Math.round(lati * 10000.0) / 10000.0;
		longi = Math.round(longi * 10000.0) / 10000.0;

		addText("myLatLng = new google.maps.LatLng("+lati+","+longi+");\n" +
				" var marker = new google.maps.Marker({"+
				"position: myLatLng,"+
				"map: map,"+
				"icon: image"+
				"});\n");


	}

	public void addPolygon(double[] lat, double[] lon, int r, int g, int b, double opacity, int id) {

		String rgb = math.BasicMath.convertNumber(r, 16, 2) + 
				math.BasicMath.convertNumber(g, 16, 2) +
				math.BasicMath.convertNumber(b, 16, 2);


		String coords = "";
		for (int i=0; i < lat.length; i++) {
			if (i>0) {
				coords+=",";
			}

			coords+="new google.maps.LatLng("+lat[i]+","+lon[i]+")";
		}

		addText("if (topics.length == 0 || contains(topics,"+id+")) {var triangleCoords = ["+coords+"]; triangle = new google.maps.Polygon({ paths: triangleCoords, strokeWeight: 0, fillColor: \"#"+rgb+"\", fillOpacity: "+opacity+" }); triangle.setMap(map);\n }\n");

	}

	public void setFile (String path)  {
		try {
			File outputFile = new File(path);
			FileWriter fw;
			fw = new FileWriter(outputFile);
			bw = new BufferedWriter(fw); 
			addText(output);
			output = "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void addText(String text) {
		
		if (bw != null) {
		
		try {

			bw.write(text);
			bw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		else {
			
			output += text;
			
		}
	}

}
