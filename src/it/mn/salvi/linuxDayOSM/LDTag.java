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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.Html;

public class LDTag extends GeoTag {
  private static TagDescription description = null;
  private static PositionIcon[] icons = new PositionIcon[2];


  private String organizzazione;
  private String luogo;
  private String indirizzo;
  private String citta;
  private String provincia;
  private String sito;

//  private Dialog info;
//  private Label url;

  public LDTag (GeoTag next, GeoPoint coords, Resources res) {
    super(next, coords);
    init (res);
  }

  public LDTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
    super(next, null);
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
	  if (icons[0] == null) {
		  try {
			  icons[0] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.ld_icon));
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  if (icons[1] == null) {
		  try {
			  icons[1] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.ld_icon_wide));
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  if (description == null) {
		  try {
			  description = new TagDescription(res.getString(R.string.LinuxDayDescription), icons[0], "LDTag");
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
  }

  @Override
  public void action(Context context, Point p) {
	infoDialog (context, "Linux Day...", Html.fromHtml(context.getString(R.string.LinuxDayDialog, organizzazione, luogo, indirizzo, citta + " (" + provincia + ")", sito)));
  }

  @Override
  public boolean isActive() {
	return description.isActive();
  }

  @Override
  public void setActive(boolean active) {
	description.setActive(active);
  }

  @Override
  public boolean canDisable() {
	return true;
  }

  @Override
  public TagDescription getDescription() {
	  return description;
  }

  @Override
  public PositionIcon getIcon(int level) {
	if (level >= icons.length) {
		level = icons.length - 1;
	}
	return icons[level];
  }

  @Override
  public void initWithPreferences(SharedPreferences preferences) {
	  description.initWithPreferences(preferences);
  }
}
