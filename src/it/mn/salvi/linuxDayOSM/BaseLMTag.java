package it.mn.salvi.linuxDayOSM;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;

public abstract class BaseLMTag extends GeoTag {
	protected String organizzazione;
	protected String sito;
	protected int msgStringId = R.string.LugDialog;
	protected int titleId = R.string.LugTitle;

	public BaseLMTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
		super(next, null);
		String[] fields = OsmBrowser.tabParser(record);
		// Log.i("stdout","Record '" + record +"'");
		// Log.i("stdout","Campi " + fields.length);
		// for (int i = 0; i < fields.length; i++) {
		//	  Log.i("stdout",fields[i]);
		// }
		organizzazione = fields[2];
		// <a href="http://www.alflug.it/">http://www.alflug.it/</a>
		Pattern p = Pattern.compile(".*href=\"([^\"]*)\".*");
		// Pattern p = Pattern.compile(".*>([^<]*)<.*");
		Matcher m = p.matcher(fields[3]);
		// Log.i("stdout","Sito : " + fields[3] + " Pattern trovato " + m.matches() + " Gruppi " + m.groupCount());
		if (m.matches()) {
			sito = m.group(1);
		} else {
			throw new Exception("Non riesco a capire l'indirizzo del sito");
		}
		// Proiezione EPSG:900913 - A me serve WGS84
		/* http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/
		        self.originShift = 2 * math.pi * 6378137 / 2.0
		        # 20037508.342789244

		    def MetersToLatLon(self, mx, my ):
		        "Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum"

		        lon = (mx / self.originShift) * 180.0
		        lat = (my / self.originShift) * 180.0

		        lat = 180 / math.pi * (2 * math.atan( math.exp( lat * math.pi / 180.0)) - math.pi / 2.0)
		        return lat, lon
		 */
		double originShift = 2 * Math.PI * 6378137 / 2.0;
		double lon = (Double.parseDouble(fields[1]) / originShift) * 180.0;
		double lat = (Double.parseDouble(fields[0]) / originShift) * 180.0;

		lat = 180 / Math.PI * (2 * Math.atan( Math.exp( lat * Math.PI / 180.0)) - Math.PI / 2.0);
		// setCoords(new GeoPoint(Double.parseDouble(fields[0])/152146.001925433+8.05967155838889, Double.parseDouble(fields[1])/111327.159945651-0.00306519225921));
		setCoords(new GeoPoint(lat, lon));
		init (res, fields);		  
	}

	protected abstract void init (Resources res, String[] fields);
	
	protected String getDialogString (Context context) {
		return context.getString(msgStringId, organizzazione, sito);
	}

	public void baseAction(Context context) {
		infoDialog (context, context.getString(titleId), Html.fromHtml(getDialogString (context)));
	}

	@Override
	public boolean canDisable() {
		return true;
	}
}
