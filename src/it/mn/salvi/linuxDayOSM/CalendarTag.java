/*
 * CalendarTag.java
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

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class CalendarTag extends BaseLMTag {
	private static TagDescription description = null;
	private static TagDescription descriptionOld = null;
	private static PositionIcon[] icons = new PositionIcon[4];
	private boolean old;
	private String data;

	//lat	lon	title	description	iconSize	iconOffset	icon
	// 5193501.5396258	1601689.3235986	AnxaLug	<a href="http://www.anxalug.org/">http://www.anxalug.org/</a>	16,19	-8,-19	http://lugmap.it/images/icon.png
	public CalendarTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
		super(next, titles, record, res);
	}

	@Override
	protected void init (Resources res, String[] fields) {
		if (icons[0] == null) {
			try {
				icons[0] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.calendar_icon));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[1] == null) {
			try {
				icons[1] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.calendar_icon_wide));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[2] == null) {
			try {
				icons[2] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.calendar_old_icon));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[3] == null) {
			try {
				icons[3] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.calendar_old_icon_wide));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (description == null) {
			try {
				description = new TagDescription(res.getString(R.string.CalendarDescription), icons[0], "CalendarTag");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (descriptionOld == null) {
			try {
				descriptionOld = new TagDescription(res.getString(R.string.CalendarDescriptionOld), icons[2], "CalendarTagOld");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		titleId = R.string.CalendarTitle;
		msgStringId = R.string.CalendarDialog;
		Pattern p = Pattern.compile(".*href=\"([^\"]*)\".*>([^<]*)<.*");
		Matcher m = p.matcher(fields[3]);		
		if (m.matches()) {
			sito = m.group(1);
			organizzazione = m.group(2);
		} else {
			organizzazione = "Non riesco a capire l'indirizzo del sito";
		}
		data = fields[2];
		Calendar c = Calendar.getInstance(); 
		int monthNow = c.get(Calendar.MONTH) + 1;
		int dayNow = c.get(Calendar.DAY_OF_MONTH);
		// DD/MM
		old = false;
		p = Pattern.compile("([0-9.]*)/([0-9.]*)");
		m = p.matcher(data);
		// System.out.println("Coordinate : " + fields[6] + " Pattern trovato " + m.matches());
		if (m.matches()) {
			int month = Integer.parseInt(m.group(2));
			// mancando l'anno, Bisogna gestire il mese in maniera "circolare"...
			// Se il mese è precedente di almeno 6 mesi, aggiungo un'anno
			// Se invece è successivo di almeno 6 mesi, tolgo un anno
			if (month <= monthNow - 6) {
				month += 12;
			} else if (month >= monthNow + 6) {
				month -= 12;
			}
			int day = Integer.parseInt(m.group(1));
			if (month < monthNow || (month == monthNow && day < dayNow)) {
				old = true;
			}
			data += " " + dayNow + "/" + monthNow + " " + day + "/" + month + " Old?" + old;
		} else {
			data += " - Non riesc a convertirla per confrontarla";
		}
	}

	@Override
	protected String getDialogString (Context context) {
		return context.getString(msgStringId, data, organizzazione, sito);
	}

	@Override
	public void action(Context context, Point p) {
		baseAction (context);
	}

	@Override
	public boolean isActive() {
		return (old) ? descriptionOld.isActive() : description.isActive();
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
		return (old) ? descriptionOld : description;
	}

	@Override
	public PositionIcon getIcon(int level) {
		if (old) {
			level += 2;
		}
		if (level >= icons.length) {
			level = icons.length - 1;
		}
		return icons[level];
	}

	@Override
	public void initWithPreferences(SharedPreferences preferences) {
		description.initWithPreferences(preferences);
		descriptionOld.initWithPreferences(preferences);
	}
}
