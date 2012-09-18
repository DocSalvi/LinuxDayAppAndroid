/*
 * GeoTag.java
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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public abstract class GeoTag {
  protected GeoTag next;
  protected GeoPoint coords;
  protected Point absolutePos;
  protected Dimension size;
  protected Point refPoint;

  public GeoTag (GeoTag next, GeoPoint coords, Dimension size, Point refPoint) {
    this.next = next;
    this.size = size;
    this.refPoint = refPoint;
    if (coords != null) {
      setCoords (coords);
    }
  }

  public GeoTag getNext () {
    return next;
  }

  public boolean isHit (Point absolute, int scale) {
    return absolute.x >= (absolutePos.x - refPoint.x * scale) && absolute.x < (absolutePos.x + (size.width - refPoint.x) * scale) &&
        absolute.y >= (absolutePos.y - refPoint.y * scale) && absolute.y < (absolutePos.y + (size.height - refPoint.y) * scale);
  }

  public boolean isInto (Point absTopLeft, Point absBottomRight, int scale) {
	  return absolutePos.x >= absTopLeft.x && absolutePos.x < absBottomRight.x && absolutePos.y >= absTopLeft.y && absolutePos.y < absBottomRight.y;
  }

  protected void setCoords (GeoPoint coords) {
    this.coords = coords;
    absolutePos = new Point(OsmBrowser.long2absolutex(coords.lon), OsmBrowser.lat2absolutey(coords.lat));
    // System.out.println("GeoTag Coords: lat" + coords.lat + " lon " + coords.lon + " AbsolutePos " + absolutePos.x + "x" + absolutePos.y);
  }

  public void paint (Canvas canvas, Paint paint, Point absTopLeft, Point absBottomRight, int scale) {
    if (absolutePos.x >= absTopLeft.x && absolutePos.x < absBottomRight.x && absolutePos.y >= absTopLeft.y && absolutePos.y < absBottomRight.y) {
      scaledPaint (canvas, paint, (absolutePos.x - absTopLeft.x) / scale - refPoint.x, (absolutePos.y - absTopLeft.y) / scale - refPoint.y);
    }
  }

  abstract protected void scaledPaint (Canvas canvas, Paint paint, int x, int y);
  abstract public void action (Context context, Point p);
}
