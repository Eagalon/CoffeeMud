package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2001-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class WalkingBoots extends StdArmor
{
	@Override
	public String ID()
	{
		return "WalkingBoots";
	}

	public WalkingBoots()
	{
		super();

		setName("a pair of nice hide walking boots");
		setDisplayText("a pair of hide walking boots sits here.");
		setDescription("They look like a rather nice pair of footwear.");
		properWornBitmap=Wearable.WORN_FEET;
		wornLogicalAnd=false;
		basePhyStats().setArmor(1);
		basePhyStats().setAbility(0);
		basePhyStats().setWeight(5);
		baseGoldValue=5;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_LEATHER;
	}
}
