/*
 * BMNetTag.java
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class BMNetTag extends BaseLMTag {
	  private static TagDescription description = null;
	  private static PositionIcon[] icons = new PositionIcon[2];

	  //lat	lon	title	description	iconSize	iconOffset	icon
	  // 5193501.5396258	1601689.3235986	AnxaLug	<a href="http://www.anxalug.org/">http://www.anxalug.org/</a>	16,19	-8,-19	http://lugmap.it/images/icon.png
	  public BMNetTag (GeoTag next, String[] titles, String record, Resources res) throws Exception {
	    super(next, titles, record, res);
	  }

	  @Override
	  protected void init (Resources res) {
		  if (icons[0] == null) {
			  try {
				  icons[0] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.bmnet_logo));
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
		  }
		  if (icons[1] == null) {
			  try {
				  icons[1] = new PositionIcon(0.5, 1.0, BitmapFactory.decodeResource(res,R.drawable.bmnet_logo_wide));
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
		  }
		  if (description == null) {
			  try {
				  description = new TagDescription(res.getString(R.string.BMNetDescription), icons[0]);
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
		return description.isActive();
	  }

	  @Override
	  public void setActive(boolean active) {
		description.setActive(active);
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
}
