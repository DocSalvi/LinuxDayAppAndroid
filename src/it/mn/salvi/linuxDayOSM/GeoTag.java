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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public abstract class GeoTag {
  protected GeoTag next;
  protected GeoPoint coords;
  protected Point absolutePos;
  
  private static final int SMALLICONLIMIT = 2048;

  public GeoTag (GeoTag next, GeoPoint coords) {
    this.next = next;
    if (coords != null) {
      setCoords (coords);
    }
  }

  public GeoTag getNext () {
    return next;
  }
  
  private PositionIcon getIconScale(int scale) {
	  int i=0;
	  if (scale < SMALLICONLIMIT) {
		  i = 1;
	  }
	  return getIcon(i);
  }

  public boolean isHit (Point absolute, int scale) {
	PositionIcon i = getIconScale(scale);
	if (i != null) {
		if (absolute.x >= (absolutePos.x - i.getRefPoint().x * scale) && absolute.x < (absolutePos.x + (i.getSize().width - i.getRefPoint().x) * scale) &&
			absolute.y >= (absolutePos.y - i.getRefPoint().y * scale) && absolute.y < (absolutePos.y + (i.getSize().height - i.getRefPoint().y) * scale)) {
			int x = (absolute.x - absolutePos.x) / scale + i.getRefPoint().x;
			int y = (absolute.y - absolutePos.y) / scale + i.getRefPoint().y;
			int alpha = Color.alpha(i.getIcon().getPixel(x, y));
			return alpha > 128;
		}
        return false;
	}
	return false;
  }

  public boolean isInto (Point absTopLeft, Point absBottomRight, int scale) {
	  if (isActive()) {
		  return absolutePos.x >= absTopLeft.x && absolutePos.x < absBottomRight.x && absolutePos.y >= absTopLeft.y && absolutePos.y < absBottomRight.y;
	  }
	  return false;
  }

  protected void setCoords (GeoPoint coords) {
    this.coords = coords;
    absolutePos = new Point(OsmBrowser.long2absolutex(coords.lon), OsmBrowser.lat2absolutey(coords.lat));
    // System.out.println("GeoTag Coords: lat" + coords.lat + " lon " + coords.lon + " AbsolutePos " + absolutePos.x + "x" + absolutePos.y);
  }

  public void paint (Canvas canvas, Paint paint, Point absTopLeft, Point absBottomRight, int scale) {
	  if (absolutePos.x >= absTopLeft.x && absolutePos.x < absBottomRight.x && absolutePos.y >= absTopLeft.y && absolutePos.y < absBottomRight.y) {
		  PositionIcon i = getIconScale(scale);
		  if (i != null) {
			  int x = (absolutePos.x - absTopLeft.x) / scale - i.getRefPoint().x;
			  int y = (absolutePos.y - absTopLeft.y) / scale - i.getRefPoint().y;
			  canvas.drawBitmap(i.getIcon(), x, y, paint);
		  }
	  }
  }
  
  protected void infoDialog (Context context, String title, Spanned content) {
	  final Dialog dialog = new Dialog(context);
	  dialog.setContentView(R.layout.ld_dialog_layout);
	  dialog.setTitle(title);

	  TextView text = (TextView) dialog.findViewById(R.id.content);
	  text.setText(content);

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

  abstract public void action (Context context, Point p);
  abstract public boolean isActive();
  abstract public void setActive(boolean state);
  abstract public boolean canDisable();
  abstract public TagDescription getDescription();
  abstract public PositionIcon getIcon(int level);
}
