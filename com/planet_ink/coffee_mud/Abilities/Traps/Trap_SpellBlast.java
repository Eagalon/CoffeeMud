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
public class Trap_SpellBlast extends StdTrap
{
	@Override
	public String ID()
	{
		return "Trap_SpellBlast";
	}

	private final static String	localizedName	= CMLib.lang().L("spell blast");

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

	public Trap_SpellBlast()
	{
		super();
		trapLevel = 23;
	}

	@Override
	public String requiresToSet()
	{
		return "a spell scroll";
	}

	protected Item getSpellItem(final MOB mob)
	{
		if(mob==null)
			return null;
		if(mob.location()==null)
			return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)
			&&(I instanceof Scroll)
			&&(((SpellHolder)I).getSpells()!=null)
			&&(((SpellHolder)I).getSpells().size()>0)
			&&(I.usesRemaining()>0))
				return I;
		}
		return null;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		final Scroll I=(Scroll)CMClass.getMiscMagic("StdScroll");
		Ability A=CMClass.getAbility(text());
		if(A==null)
			A=CMClass.getAbility("Spell_Fireball");
		I.setSpellList(A.ID());
		V.add(I);
		return V;
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		if(P==null)
			return null;
		final Item I=getSpellItem(mob);
		if((I!=null)&&(I instanceof SpellHolder))
		{
			final List<Ability> V=((SpellHolder)I).getSpells();
			if(V.size()>0)
				setMiscText(V.get(0).ID());
			I.setUsesRemaining(I.usesRemaining()-1);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		final Item I=getSpellItem(mob);
		if((I==null)
		&&(mob!=null))
		{
			mob.tell(L("You'll need to set down a scroll with a spell first."));
			return false;
		}
		return true;
	}

	@Override
	protected boolean doesInnerExplosionDestroy(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_GAS:
			return false;
		}
		return true;
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_ROCK:
			return false;
		}
		return true;
	}

	@Override
	public void spring(final MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!canInvokeTrapOn(invoker(),target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) setting off a trap!")));
			}
			else
			{
				if(target.location().show(target,target,this,
						CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,getTrigMsg(L("<S-NAME> set(s) off a trap!"))))
				{
					super.spring(target);
					Ability A=CMClass.getAbility(miscText);
					if(A==null)
						A=CMClass.getAbility("Spell_Fireball");
					if(A!=null)
						A.invoke(invoker(),target,true,trapLevel()+abilityCode());
					if((canBeUninvoked())&&(affected instanceof Item))
						disable();
				}
			}
		}
	}
}
