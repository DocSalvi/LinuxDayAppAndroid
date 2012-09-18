/*
 * LDTag.java
 * LinuxDayOSM
 * Copyright (C) Stefano Salvi 2010 <stefano@salvi.mn.it>
 *
 * LinuxDayOSM is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinuxDayOSM is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.mn.salvi.linuxDayOSM;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LMTag extends GeoTag implements View.OnTouchListener {
  private static Bitmap ldMark = null;

  private String organizzazione;
  private String sito;

//  private Dialog info;
//  private Label url;

  public LMTag (GeoTag next, GeoPoint coords, Resources res) {
    super(next, coords, new Dimension(20,24), new Point (10,24));
    init (res);
  }

  //lat	lon	title	description	iconSize	iconOffset	icon
  // 5193501.5396258	1601689.3235986	AnxaLug	<a href="http://www.anxalug.org/">http://www.anxalug.org/</a>	16,19	-8,-19	http://lugmap.it/images/icon.png
  public LMTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
    super(next, null, new Dimension(16,19), new Point (8,19));
    String[] fields = OsmBrowser.tabParser(record);
	// Log.i("stdout","Record '" + record +"'");
	// Log.i("stdout","Campi " + fields.length);
    // for (int i = 0; i < fields.length; i++) {
    //	  Log.i("stdout",fields[i]);
    // }
    organizzazione = fields[2];
    // <a href="http://www.alflug.it/">http://www.alflug.it/</a>
    Pattern p = Pattern.compile(".*>([^<]*)<.*");
    Matcher m = p.matcher(fields[3]);
    // Log.i("stdout","Sito : " + fields[3] + " Pattern trovato " + m.matches() + " Gruppi " + m.groupCount());
    if (m.matches()) {
    	sito = m.group(1);
    } else {
    	throw new Exception("Il Lug non ha coordinate");
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
    init (res);
  }

  private void init (Resources res) {
    if (ldMark == null) {
      try {
        ldMark = BitmapFactory.decodeResource(res,R.drawable.lm_icon);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void scaledPaint(Canvas canvas, Paint mPaint, int x, int y) {
    canvas.drawBitmap(ldMark, x, y, mPaint);
  }

  @Override
  public void action(Context context, Point p) {
    final Dialog dialog = new Dialog(context);
    dialog.setContentView(R.layout.ld_dialog_layout);
    dialog.setTitle("Lug...");

    TextView text = (TextView) dialog.findViewById(R.id.content);
	text.setText(Html.fromHtml(context.getString(R.string.LugDialog, organizzazione, sito)));

    Button dialogButton = (Button) dialog.findViewById(R.id.close);
    // if button is clicked, close the custom dialog
    dialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    dialog.show();
  }

  @Override
  public boolean onTouch(View currentView, MotionEvent event) {
	  return true;
  }
}
