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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;

public class HereTag extends GeoTag {
  public HereTag (GeoTag next, GeoPoint coords) {
    super(next, coords, new Dimension(20,20), new Point (10,10));
  }

  @Override
  protected void scaledPaint(Canvas canvas, Paint paint, int x, int y) {
    paint.setColor(Color.BLACK);
    paint.setStyle(Style.STROKE);
    canvas.drawOval(new RectF(x,y,x+20,y+20), paint);
    canvas.drawLine(x+10, y+20, x+10, y, paint);
    canvas.drawLine(x, y+10, x+20, y+10, paint);
  }

  @Override
  public void action(Context context, Point p) {
    // TODO Auto-generated method stub
  }

}
