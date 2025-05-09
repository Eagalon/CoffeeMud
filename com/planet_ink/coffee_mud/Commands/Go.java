package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Go extends StdCommand
{
	public Go()
	{
	}

	private final String[]	access	= I(new String[] { "GO", "WALK" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected Command		stander		= null;
	protected List<String>	ifneccvec	= null;

	public boolean standIfNecessary(final MOB mob, final List<String> commands, final int metaFlags, final boolean giveMsg)
		throws java.io.IOException
	{
		final boolean wasStanding = CMLib.flags().isStanding(mob);
		if(CMLib.flags().isFlying(mob) && wasStanding)
			return true;
		if((ifneccvec==null)||(ifneccvec.size()!=2))
		{
			ifneccvec=new Vector<String>();
			ifneccvec.add("STAND");
			ifneccvec.add("IFNECESSARY");
		}
		if(stander==null)
			stander=CMClass.getCommand("Stand");
		if((stander!=null)&&(ifneccvec!=null))
			stander.execute(mob,ifneccvec,metaFlags);
		final boolean isStanding = CMLib.flags().isStanding(mob) && (!CMLib.flags().isSleeping(mob));
		if(giveMsg && (!isStanding))
		{
			if(CMLib.flags().isSleeping(mob))
				CMLib.commands().postCommandFail(mob, commands, L("You need to wake up first."));
			else
			if(!wasStanding)
				CMLib.commands().postCommandFail(mob, commands, L("You failed to stand up. You might try crawling."));
			else
				CMLib.commands().postCommandFail(mob, commands, L("You need to stand up."));
		}
		return isStanding;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(!standIfNecessary(mob,commands, metaFlags, true))
			return false;

		final String whereStr=CMParms.combine(commands,1);
		final Room R=mob.location();
		if(R==null)
			return false;

		final Directions.DirType dirType = CMLib.flags().getDirType(R);
		final String validDirs = Directions.NAMES_LIST(dirType);
		final boolean running = mob.isAttributeSet(MOB.Attrib.AUTORUN);

		int direction=-1;
		if(whereStr.equalsIgnoreCase("OUT"))
		{
			if(!CMath.bset(R.domainType(),Room.INDOORS))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You aren't indoors."));
				return false;
			}

			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((R.getExitInDir(d)!=null)
				&&(R.getRoomInDir(d)!=null)
				&&(!CMath.bset(R.getRoomInDir(d).domainType(),Room.INDOORS)))
				{
					if(direction>=0)
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("Which way out?  Try @x1.",validDirs));
						return false;
					}
					direction=d;
				}
			}
			if(direction<0)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("There is no direct way out of this place.  Try a direction."));
				return false;
			}
		}
		if(direction<0)
		{
			if(mob.isMonster())
				direction=CMLib.directions().getGoodDirectionCode(CMStrings.removePunctuation(whereStr));
			else
				direction=CMLib.directions().getGoodDirectionCode(CMStrings.removePunctuation(whereStr), dirType);
		}
		if(direction<0)
		{
			final Environmental E=R.fetchFromRoomFavorItems(null,whereStr);
			if(E instanceof Rideable)
			{
				final Command C=CMClass.getCommand("Enter");
				return C.execute(mob,commands,metaFlags);
			}
			if(E instanceof Exit)
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(R.getExitInDir(d)==E)
					{
						direction=d;
						break;
					}
				}
			}
		}
		final String doing=commands.get(0);
		if(direction>=0)
		{
			final boolean success;
			if(running)
				success=CMLib.tracking().run(mob,direction,false,false,false,false);
			else
				success=CMLib.tracking().walk(mob,direction,false,false,false,false);
			if(!success)
				mob.clearCommandQueue();
		}
		else
		{
			Exit E=R.fetchExit(whereStr);
			if(E != null)
				return CMLib.commands().forceStandardCommand(mob, "Enter", commands);

			boolean doneAnything=false;
			final List<List<String>> prequeCommands=new ArrayList<List<String>>();


			for(int v=1;v<commands.size();v++)
			{
				final String[] cs = commands.get(v).split(",");
				if(cs.length>1)
				{
					commands.remove(v);
					for(int i=cs.length-1;i>=0;i--)
						if(cs[i].trim().length()>0)
							commands.add(v, cs[i]);
				}
			}
			for(int v=1;v<commands.size();v++)
			{
				int num=1;
				String s=commands.get(v);
				if(CMath.s_int(s)>0)
				{
					num=CMath.s_int(s);
					v++;
					if(v<commands.size())
						s=commands.get(v);
				}
				else
				if((s.length()>0) && (Character.isDigit(s.charAt(0))))
				{
					int x=1;
					while((x<s.length()-1)&&(Character.isDigit(s.charAt(x))))
						x++;
					num=CMath.s_int(s.substring(0,x));
					s=s.substring(x);
				}

				if(mob.isMonster())
					direction=CMLib.directions().getGoodDirectionCode(CMStrings.removePunctuation(s));
				else
					direction=CMLib.directions().getGoodDirectionCode(CMStrings.removePunctuation(s), dirType);
				if(direction>=0)
				{
					doneAnything=true;
					for(int i=0;i<num;i++)
					{
						if(mob.isMonster())
						{
							if(running)
							{
								if(!CMLib.tracking().run(mob,direction,false,false,false,false))
									return false;
							}
							else
							if(!CMLib.tracking().walk(mob,direction,false,false,false,false))
								return false;
						}
						else
						{
							final Vector<String> V=new Vector<String>();
							V.add(doing);
							V.add(CMLib.directions().getDirectionName(direction, dirType));
							prequeCommands.add(V);
						}
					}
				}
				else
				{
					E=R.fetchExit(s);
					if(E!=null)
					{
						if(mob.isMonster())
							CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",s));
						else
							prequeCommands.add(new XVector<String>("ENTER",s));
					}
					else
						break;
				}
			}
			if(!doneAnything)
				mob.tell(L("@x1 which direction?\n\rTry @x2.",CMStrings.capitalizeAndLower(doing),validDirs.toLowerCase()));
			else
			if(prequeCommands.size()>0)
				mob.prequeCommands(prequeCommands, metaFlags);
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		double cost=CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),100.0);
		if((mob!=null)&&(mob.isAttributeSet(MOB.Attrib.AUTORUN)))
			cost /= 4.0;
		return CMProps.getCommandActionCost(ID(), cost);
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		double cost=CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),100.0);
		if((mob!=null)&&(mob.isAttributeSet(MOB.Attrib.AUTORUN)))
			cost /= 4.0;
		return CMProps.getCommandCombatActionCost(ID(), cost);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
