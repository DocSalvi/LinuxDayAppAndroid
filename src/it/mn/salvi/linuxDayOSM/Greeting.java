package it.mn.salvi.linuxDayOSM;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Greeting {
	private Dialog dialog;

	public Greeting(Context context, boolean ShowClose) {
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.greeting);
		dialog.setTitle("Informazioni...");
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
		if (!ShowClose) {
			dialogButton.setVisibility(View.GONE);
		}

		dialog.show();
	}
	
	public void close () {
		dialog.dismiss();
		dialog=null;
	}

}
