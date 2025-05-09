package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2025 Bo Zimmerman

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
public class Fighter_CommandMount extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_CommandMount";
	}

	private final static String localizedName = CMLib.lang().L("Command Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"COMMANDMOUNT"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> V=new Vector<String>();
		if(commands.size()>0)
		{
			V.add(commands.get(0));
			commands.remove(0);
		}

		final MOB target=getTarget(mob,V,givenTarget);
		if(target==null)
			return false;

		if(commands.size()==0)
		{
			if(mob.isMonster())
				commands.add("FLEE");
			else
			{
				if(V.size()>0)
					mob.tell(L("Command @x1 to do what?",V.get(0)));
				return false;
			}
		}

		final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(mob);
		if(((!choices.containsSecond(target.baseCharStats().getMyRace()))
			&&(!choices.containsFirst(target.baseCharStats().getMyRace().racialCategory())))
		||(!CMLib.flags().isAnAnimal(target)))
		{
			mob.tell(L("@x1 is not the sort that would heed you.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalSpeakCode(mob,target,auto),
					auto?"":L("^S<S-NAME> command(s) <T-NAMESELF> to '@x1'.^?",CMParms.combine(commands,0)));
			final int malicious = mob.getGroupMembersAndRideables(new HashSet<Rider>()).contains(target) ? 0 : CMMsg.MASK_MALICIOUS;
			final CMMsg msg2=CMClass.getMsg(mob,target,this,malicious|CMMsg.MASK_SOUND|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			final Language langL = CMLib.utensils().getLanguageSpoken(target);
			final CMMsg omsg=CMClass.getMsg(mob,target,langL,CMMsg.MSG_ORDER,null);
			if((mob.location().okMessage(mob,msg))
			&&((mob.location().okMessage(mob,msg2)))
			&&(mob.location().okMessage(mob, omsg)))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().send(mob,msg2);
					mob.location().send(mob,omsg);
					if((msg2.value()<=0)
					&&(omsg.sourceMinor()==CMMsg.TYP_ORDER))
					{
						invoker=mob;
						target.makePeace(true);
						target.enqueCommand(commands,MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER,0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to command <T-NAMESELF>, but it definitely didn't work."));

		// return whether it worked
		return success;
	}
}
