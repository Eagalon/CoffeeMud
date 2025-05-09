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
public class Chant_VineMass extends Chant_SummonVine
{
	@Override
	public String ID()
	{
		return "Chant_VineMass";
	}

	private final static String localizedName = CMLib.lang().L("Vine Mass");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Vine Mass)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public MOB determineMonster(final MOB caster, final int material)
	{
		final MOB victim=caster.getVictim();
		MOB newMOB=null;
		final int limit=((caster.phyStats().level()+(2*getXLEVELLevel(caster)))/4);
		for(int i=0;i<limit;i++)
		{
			newMOB=CMClass.getMOB("GenMOB");
			int level=adjustedLevel(caster,0);
			final int altLevel = (caster.phyStats().level()-5)+(super.getXLEVELLevel(caster)/2);
			if(altLevel > level)
				level = altLevel;
			if(level<1)
				level=1;
			newMOB.basePhyStats().setLevel(level);
			newMOB.basePhyStats().setAbility(CMProps.getMobHPBase()*2);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
			final String name="a vine";
			newMOB.setName(name);
			newMOB.setDisplayText(L("@x1 looks enraged!",name));
			newMOB.setDescription("");
			CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
			Ability A=CMClass.getAbility("Fighter_Rescue");
			A.setProficiency(100);
			newMOB.addAbility(A);
			newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
			newMOB.setLocation(caster.location());
			newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
			newMOB.basePhyStats().setDamage(6+(5*(level/5)));
			newMOB.basePhyStats().setAttackAdjustment(10);
			newMOB.basePhyStats().setArmor(100-(30+(level/2)));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
			newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0"));
			newMOB.addTattoo("SYSTEM_SUMMONED");
			newMOB.addTattoo("SUMMONED_BY:"+caster.name());
			newMOB.setMiscText(newMOB.text());
			newMOB.recoverCharStats();
			newMOB.recoverPhyStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.bringToLife(caster.location(),true);
			CMLib.beanCounter().clearZeroMoney(newMOB,null);
			newMOB.setMoneyVariation(0);
			if(victim.getVictim()!=newMOB)
				victim.setVictim(newMOB);
			newMOB.setVictim(victim);
			newMOB.setStartRoom(null); // keep before postFollow for Conquest
			if((i+1)<limit)
			{
				beneficialAffect(caster,newMOB,0,0);
				CMLib.commands().postFollow(newMOB,caster,true);
				if(newMOB.amFollowing()!=caster)
				{
					A=newMOB.fetchEffect(ID());
					if(A!=null)
						A.unInvoke();
					return null;
				}
				CMLib.combat().postAttack(newMOB,victim,newMOB.fetchWieldedItem());
			}
		}
		return(newMOB);
	}
}
