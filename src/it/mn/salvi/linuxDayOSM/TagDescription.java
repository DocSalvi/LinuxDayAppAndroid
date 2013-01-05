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

public class TagDescription {
	private String description;
	private PositionIcon icon;
	private boolean active;
	
	public TagDescription (String description, PositionIcon icon) {
		this.description = description;
		this.icon = icon;		
	}
	
	public String getDescription () {
		return description;
	}
	
	public PositionIcon getIcon () {
		return icon;
	}
	
	public void setActive (boolean active) {
		this.active = active;
	}
	
	public boolean isActive () {
		return active;
	}
}
