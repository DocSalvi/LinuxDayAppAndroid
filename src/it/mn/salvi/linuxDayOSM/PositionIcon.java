/*
 * PositionIcon.java
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

import android.graphics.Bitmap;
import android.graphics.Point;

public class PositionIcon {
	  private Dimension size;
	  private Point refPoint;
	  private Bitmap icon;

	  public PositionIcon (Dimension size, Point refPoint, Bitmap icon) {
		  this.size = size;
		  this.refPoint = refPoint;
		  this.icon = icon;		  
	  }
	  
	  public Dimension getSize () {
		  return size;
	  }
	  
	  public Point getRefPoint () {
		  return refPoint;
	  }
	  
	  public Bitmap getIcon () {
		  return icon;
	  }
}
