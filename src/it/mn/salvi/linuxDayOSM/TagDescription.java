/*
 * TagDescription.java
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

import android.content.SharedPreferences;

public class TagDescription {
	private String description;
	private PositionIcon icon;
	private boolean active;
	private SharedPreferences preferences;
	private String tagClassName;
	
	public TagDescription (String description, PositionIcon icon, String tagClassName) {
		this.description = description;
		this.icon = icon;
		this.tagClassName = tagClassName;
	}
	
	public String getDescription () {
		return description;
	}
	
	public PositionIcon getIcon () {
		return icon;
	}
	
	public void initWithPreferences (SharedPreferences preferences) {
		this.preferences = preferences;
		active = preferences.getBoolean(tagClassName, true);
		System.out.println("Leggo la preferenza per " + tagClassName + " Valore " + active);
	}
	
	public void setActive (boolean active) {
		if (preferences != null) {
			System.out.println("Salvo la preferenza per " + tagClassName + " Valore " + active);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean(tagClassName, active); // value to store
			editor.commit();
		} else {
			System.out.println("Mancano le preferemze, quindi non posso salvare " + tagClassName);			
		}
		this.active = active;
	}
	
	public boolean isActive () {
		return active;
	}
}
