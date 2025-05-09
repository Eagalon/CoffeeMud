package com.planet_ink.coffee_mud.Abilities.Traps;
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

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Trap_Noise extends StdTrap
{
	@Override
	public String ID()
	{
		return "Trap_Noise";
	}

	private final static String	localizedName	= CMLib.lang().L("noisy trap");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_EXITS | Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public Trap_Noise()
	{
		super();
		trapLevel = 1;
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	@Override
	public void spring(final MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((doesSaveVsTraps(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) setting off a noise trap!")));
			}
			else
			{
				if(target.location().show(target,target,this,
						CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,getTrigMsg(L("<S-NAME> set(s) off a noise trap!"))))
				{
					super.spring(target);
					final Area A=target.location().getArea();
					for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
					{
						final Room R=e.nextElement();
						if(R!=target.location())
						{
							R.showHappens(CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
									getDamMsg(L("You hear a loud noise coming from somewhere.")));
						}
					}
					if((canBeUninvoked())&&(affected instanceof Item))
						disable();
				}
			}
		}
	}
}
