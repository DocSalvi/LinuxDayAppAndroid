/*
 * HereTag.java
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

public class HereTag extends GeoTag {
  private static TagDescription description = null;
  private static PositionIcon[] icons = new PositionIcon[2];

  public HereTag (GeoTag next, GeoPoint coords, Resources res) {
    super(next, coords);
    
	  if (icons[0] == null) {
		  try {
			  icons[0] = new PositionIcon(0.5, 0.5, BitmapFactory.decodeResource(res,R.drawable.target_icon));
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  if (icons[1] == null) {
		  try {
			  icons[1] = new PositionIcon(0.5, 0.5, BitmapFactory.decodeResource(res,R.drawable.target_icon_wide));
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  if (description == null) {
		  try {
			  description = new TagDescription(res.getString(R.string.HereDescription), icons[0]);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
  }

  @Override
  public void action(Context context, Point p) {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isActive() {
	return true;
  }

  @Override
  public void setEnable(boolean state) {
  }

  @Override
  public boolean canDisable() {
	return false;
  }

  @Override
  public TagDescription getDescription() {
	  if (isActive()) {
		  return description;
	  }
	  return null;
  }

  @Override
  public PositionIcon getIcon(int level) {
	if (level >= icons.length) {
		level = icons.length - 1;
	}
	return icons[level];
  }
}
