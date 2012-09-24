/*
 * SelectLists.java
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

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class SelectLists {
	class listCheckBox {
		protected GeoTag list;
		protected CheckBox box;
		protected listCheckBox(GeoTag list, CheckBox box) {
			this.list = list;
			this.box = box;
		}
	}
	private Dialog dialog;
	private OsmBrowser browser;
	ArrayList<TagDescription> descs = new ArrayList<TagDescription>();
	ArrayList<listCheckBox> boxes = new ArrayList<SelectLists.listCheckBox>();

	public SelectLists(OsmBrowser osmBrowser) {
		browser = osmBrowser;
		Context context = osmBrowser.getContext();
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.list_select);
		dialog.setTitle("Seleziona Liste da Visualizzare...");
		LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.checkbox_container);
		for (GeoTag t = osmBrowser.tagList; t != null; t = t.getNext()) {
			if (t.canDisable() && t.getDescription() != null && !descs.contains(t.getDescription())) {
				descs.add(t.getDescription());
		        CheckBox cb = new CheckBox(dialog.getContext());
		        cb.setText(t.getDescription().getDescription());
		        cb.setChecked(t.isActive());
		        ll.addView(cb);
		        boxes.add(new listCheckBox(t, cb));
			}
		}
		// TextView text = (TextView) dialog.findViewById(R.id.ILS);
		// text.setText(Html.fromHtml(context.getString(R.string.ILSref)));

		// text = (TextView) dialog.findViewById(R.id.info);
		// text.setText(Html.fromHtml(context.getString(R.string.GreetingText)));

		Button dialogButton = (Button) dialog.findViewById(R.id.close);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity myAct = (Activity) browser.getContext();
				SharedPreferences preferences = myAct.getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				for (int i = 0; i < boxes.size(); i++) {
					listCheckBox lcb = boxes.get(i);
					lcb.list.setEnable(lcb.box.isChecked());
					editor.putBoolean(lcb.box.toString(), lcb.box.isChecked()); // value to store
				}
				editor.commit();
				browser.postInvalidate();
				
				dialog.dismiss();
				dialog=null;
			}
		});

		dialog.show();
	}
	
	public void close () {
		dialog.dismiss();
		dialog=null;
	}
}
