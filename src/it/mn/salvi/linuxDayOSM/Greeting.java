/*
 * Greeting.java
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
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Greeting {
	private Dialog dialog;

	public Greeting(Context context, boolean ShowClose) {
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.greeting);
		dialog.setTitle(context.getString(R.string.greeting_title));
		TextView text = (TextView) dialog.findViewById(R.id.ILS);
		text.setText(Html.fromHtml(context.getString(R.string.ILSref)));

		text = (TextView) dialog.findViewById(R.id.info);
		text.setText(Html.fromHtml(context.getString(R.string.GreetingText)));

		Button dialogButton = (Button) dialog.findViewById(R.id.close);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				dialog=null;
			}
		});
		ProgressBar progressBar = (ProgressBar)  dialog.findViewById(R.id.greetingProgressBar);
		if (ShowClose) {
			progressBar.setVisibility(View.GONE);
			dialogButton.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.VISIBLE);
			dialogButton.setVisibility(View.GONE);
		}

		dialog.show();
	}
	
	public void close () {
		dialog.dismiss();
		dialog=null;
	}

}
