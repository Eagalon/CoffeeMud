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
public class Bomb_Poison extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Poison";
	}

	private final static String	localizedName	= CMLib.lang().L("poison gas bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Poison()
	{
		super();
		trapLevel = 5;
	}

	@Override
	public String requiresToSet()
	{
		return "some poison";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		final Item I=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_POISON);
		Ability A=CMClass.getAbility(text());
		if(A==null)
			A=CMClass.getAbility("Poison");
		I.addNonUninvokableEffect(A);
		V.add(I);
		return V;
	}

	public List<Ability> returnOffensiveAffects(final Physical fromMe)
	{
		final List<Ability> offenders=new Vector<Ability>();

		for(final Enumeration<Ability> a=fromMe.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
				offenders.add(A);
		}
		return offenders;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		final List<Ability> V=returnOffensiveAffects(P);
		if((!(P instanceof Drink))||(V.size()==0))
		{
			if(mob!=null)
				mob.tell(L("You need some poison to make this out of."));
			return false;
		}
		return true;
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		if(P==null)
			return null;
		final List<Ability> V=returnOffensiveAffects(P);
		if(V.size()>0)
			setMiscText(V.get(0).ID());
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_GAS:
			return false;
		}
		return true;
	}

	@Override
	public void spring(final MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the poison gas!")));
			}
			else
			{
				final String triggerMsg = getTrigMsg(L("@x1 spews poison gas all over <T-NAME>!",affected.name()));
				if(target.location().show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,triggerMsg))
				{
					super.spring(target);
					Ability A=(miscText.length()>0)?CMClass.getAbility(miscText):null;
					if(A==null)
						A=CMClass.getAbility("Poison");
					if(A!=null)
						A.invoke(invoker(),target,true,trapLevel()+abilityCode());
				}
			}
		}
	}

}
