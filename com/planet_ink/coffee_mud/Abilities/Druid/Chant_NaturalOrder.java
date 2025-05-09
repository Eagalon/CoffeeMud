package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2025 Bo Zimmerman

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
public class Chant_NaturalOrder extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_NaturalOrder";
	}

	private final static String	localizedName	= CMLib.lang().L("Natural Order");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Communing with the Natural Order)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ENDURING;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected long[]	lastTime	= new long[] { 0 };

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> end(s) <S-HIS-HER> natural communion."));
				else
					mob.tell(L("Your communion with natural order ends."));
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.tool()!=this)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)) // prevents endless loops
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH)&&CMath.bset(msg.sourceMajor(),CMMsg.MASK_SOUND))))
			unInvoke();
		return;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;

		if((msg.amISource(mob)
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&(msg.sourceMajor()>0)))
		{
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		final MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!mob.isInCombat())
		{
			if((System.currentTimeMillis()-lastTime[0])<60000)
				return true;
			if(!proficiencyCheck(null,0,false))
				return true;
			lastTime[0]=System.currentTimeMillis();
			final Room room=mob.location();
			final int myInclination=mob.fetchFaction(CMLib.factions().getInclinationID());
			final int inclinationTotal=CMLib.factions().getTotal(CMLib.factions().getInclinationID());
			final int oneHalfPct=(int)Math.round(CMath.mul(inclinationTotal,.01))/2;
			if(CMLib.factions().getInclinationPurity(myInclination,Faction.Align.MODERATE)<99)
			{
				if(CMLib.factions().getInclinationPurity(myInclination,Faction.Align.CHAOTIC)<CMLib.factions().getInclinationPurity(myInclination,Faction.Align.LAWFUL))
					CMLib.factions().postSkillFactionChange(mob,this, CMLib.factions().getInclinationID(), oneHalfPct);
				else
					CMLib.factions().postSkillFactionChange(mob,this, CMLib.factions().getInclinationID(), -oneHalfPct);
				switch(CMLib.dice().roll(1,10,0))
				{
				case 0:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> observe(s) with the plants."));
					break;
				case 1:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> stud(ys) the birds."));
					break;
				case 2:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> count(s) the insects."));
					break;
				case 3:
					room.show(mob, null, this, CMMsg.MSG_HANDS | CMMsg.MASK_ALWAYS, L("<S-NAME> feel(s) the ground."));
					break;
				case 4:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> consider(s) the fresh air."));
					break;
				case 5:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> watch(es) the plants grow."));
					break;
				case 6:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> understand(s) the cycle of life."));
					break;
				case 7:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> seek(s) the inner beauty of the natural order."));
					break;
				case 8:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> expunge(s) <S-HIS-HER> unnatural thoughts."));
					break;
				case 9:
					room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> find(s) clarity in the natural world."));
					break;
				}
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("You can't commune while in combat!"));
			return false;
		}
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if(CMLib.flags().isACityRoom(mob.location()))
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,somaticCastCode(mob,null,auto),L("^S<S-NAME> begin(s) to commune with the natural order...^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Chant_NaturalOrder B = (Chant_NaturalOrder)beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				if(B!=null)
					B.lastTime = this.lastTime;
				helpProficiency(mob, 0);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) to commune with nature, but lose(s) concentration."));

		// return whether it worked
		return success;
	}
}
