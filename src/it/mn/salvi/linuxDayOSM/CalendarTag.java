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
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class CalendarTag extends BaseLMTag {
	private static boolean enabled = true;
	private static TagDescription description = null;
	private static TagDescription descriptionOld = null;
	private static PositionIcon[] icons = new PositionIcon[4];
	private boolean old;

	//lat	lon	title	description	iconSize	iconOffset	icon
	// 5193501.5396258	1601689.3235986	AnxaLug	<a href="http://www.anxalug.org/">http://www.anxalug.org/</a>	16,19	-8,-19	http://lugmap.it/images/icon.png
	public CalendarTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
		super(next, titles, record, res);
		Calendar c = Calendar.getInstance(); 
		int monthNow = c.get(Calendar.MONTH) + 1;
		int dayNow = c.get(Calendar.DAY_OF_MONTH);
		// DD/MM
		old = false;
		Pattern p = Pattern.compile("([0-9.]*)/([0-9.]*)");
		Matcher m = p.matcher(organizzazione);
		// System.out.println("Coordinate : " + fields[6] + " Pattern trovato " + m.matches());
		if (m.matches()) {
			int month = Integer.parseInt(m.group(2));
			int day = Integer.parseInt(m.group(1));
			if (month < monthNow || (month == monthNow && day < dayNow)) {
				old = true;
			}
		}
	}

	@Override
	protected void init (Resources res) {
		if (icons[0] == null) {
			try {
				icons[0] = new PositionIcon(new Dimension(16,19), new Point (8,19), BitmapFactory.decodeResource(res,R.drawable.calendar_icon));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[1] == null) {
			try {
				icons[1] = new PositionIcon(new Dimension(30,36), new Point (14,36), BitmapFactory.decodeResource(res,R.drawable.calendar_icon_wide));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[0] == null) {
			try {
				icons[2] = new PositionIcon(new Dimension(16,19), new Point (8,19), BitmapFactory.decodeResource(res,R.drawable.calendar_old_icon));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (icons[1] == null) {
			try {
				icons[3] = new PositionIcon(new Dimension(30,36), new Point (14,36), BitmapFactory.decodeResource(res,R.drawable.calendar_old_icon_wide));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (description == null) {
			try {
				description = new TagDescription(res.getString(R.string.CalendarDescription), icons[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (descriptionOld == null) {
			try {
				descriptionOld = new TagDescription(res.getString(R.string.CalendarDescriptionOld), icons[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void action(Context context, Point p) {
		baseAction (context, "Lug...");
	}

	@Override
	public boolean isActive() {
		return enabled;
	}

	@Override
	public void setEnable(boolean state) {
		enabled = state;
	}

	@Override
	public boolean canDisable() {
		return !old;
	}

	@Override
	public TagDescription getDescription() {
		return (old) ? descriptionOld : description;
	}

	@Override
	public PositionIcon getIcon(int level) {
		if (level >= icons.length) {
			level = icons.length - 1;
		}
		return icons[level];
	}
}
