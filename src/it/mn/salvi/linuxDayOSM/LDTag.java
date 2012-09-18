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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LDTag extends GeoTag implements View.OnTouchListener {
  private static Bitmap ldMark = null;

  private String organizzazione;
  private String luogo;
  private String indirizzo;
  private String citta;
  private String provincia;
  private String sito;

//  private Dialog info;
//  private Label url;

  public LDTag (GeoTag next, GeoPoint coords, Resources res) {
    super(next, coords, new Dimension(20,24), new Point (10,24));
    init (res);
  }

  public LDTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
    super(next, null, new Dimension(20,24), new Point (10,24));
    String[] fields = OsmBrowser.csvParser(record);
    //  for (int i = 0; i < fields.length; i++) {
    //    System.out.println(fields[i]);
    //  }
    organizzazione = fields[0];
    luogo = fields[1];
    indirizzo = fields[2];
    citta = fields[3];
    provincia = fields[4];
    sito = fields[5];
    // GEOMETRYCOLLECTION(POINT(10.76773 45.13915))
    Pattern p = Pattern.compile(".*POINT[ \t]*\\(([0-9.]*)&?[^0-9.]*([0-9.]*)&?\\).*");
    Matcher m = p.matcher(fields[6]);
    // System.out.println("Coordinate : " + fields[6] + " Pattern trovato " + m.matches());
    if (m.matches()) {
    	setCoords(new GeoPoint(Double.parseDouble(m.group(2)), Double.parseDouble(m.group(1))));
    } else {
    	throw new Exception("Mancano le coordinate");
    }
    init (res);
  }

  private void init (Resources res) {
    if (ldMark == null) {
      try {
        ldMark = BitmapFactory.decodeResource(res,R.drawable.ld_icon);
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
    dialog.setTitle("Evento...");
    
    TextView text = (TextView) dialog.findViewById(R.id.content);
	text.setText(Html.fromHtml(context.getString(R.string.LinuxDayDialog, organizzazione, luogo, indirizzo, citta + " (" + provincia + ")", sito)));
    

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
