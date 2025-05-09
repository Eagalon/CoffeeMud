package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
   Copyright 2006-2025 Bo Zimmerman

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
/**
 * This interface denotes an ability that also incidentally is capable
 * of generating objects, usually items.  This is probably a common
 * skill, but one could imagine an itemcraftor also being a spell that
 * generates its own items.  Also the item generation is an incidental
 * and internal aspect of the ability, these methods allow that
 * functionality to be exposed for archon use.
 */
public interface ItemCraftor extends CraftorAbility
{
	public enum CraftorType
	{
		Weapons,
		Armor,
		General,
		Consumables,
		LargeConstructions,
		Resources,
		ClanItems,
		Magic
	}

	/**
	 * Returns the general craftor type of this skill, to let
	 * outside subsystems know what they might expect.
	 * @see ItemCraftor.CraftorType
	 * @return the craftor type
	 */
	public CraftorType getCraftorType();

	/**
	 * Crafts a random item of a type supported by this class of
	 * the given resource code.
	 * Returns a pair containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @return an item pair
	 */
	public CraftedItem craftAnyItem(int material);

	/**
	 * Crafts a random item of a type supported by this class of
	 * a random resource code but within the given level range.
	 * Returns a item pair containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param minlevel the min level to try to match
	 * @param maxlevel the max level to try to match
	 * @return an item pair
	 */
	public CraftedItem craftAnyItemNearLevel(int minlevel, int maxlevel);

	/**
	 * Returns the level range of the items craftable by this skill.
	 * @return the level range of the items craftable by this skill.
	 */
	public int[] getCraftableLevelRange();

	/**
	 * Crafts every item of a type supported by this class of
	 * the given resource code.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @return a vector of vectors of item(s)
	 */
	public List<CraftedItem> craftAllItemSets(int material, boolean forceLevels);

	/**
	 * Crafts every item of a type supported by this class of
	 * every supported material.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @return a vector of vectors of item vector(s)
	 */
	public List<CraftedItem> craftAllItemSets(boolean forceLevels);

	/**
	 * Crafts the item specified by the recipe name, of a supported
	 * material type which this class can produce.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipeName the name of the item to make
	 * @return a vector of Item(s)
	 */
	public CraftedItem craftItem(String recipeName);

	/**
	 * Crafts the item specified by the recipe name, of the specified
	 * material type which this class can produce or -1 for any material.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipeName the name of the item to make
	 * @param material the rawmaterial code to make the item out of, or -1
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @param noSafety whether normal safeguards against creating broken items are overridden
	 * @return a vector of Item(s)
	 */
	public CraftedItem craftItem(String recipeName, int material, boolean forceLevels, boolean noSafety);

	/**
	 * For auto-crafting, this object represents an item,
	 * and (optionally) a key to go with it.
	 * @author Bo Zimmerman
	 */
	public class CraftedItem
	{
		public Item item;
		public DoorKey key;
		public int duration;

		public CraftedItem(final Item item, final DoorKey key, final int duration)
		{
			this.item = item;
			this.key = key;
			this.duration=duration;
		}

		public List<Item> asList()
		{
			final List<Item> list = new LinkedList<Item>();
			if(item!=null)
				list.add(item);
			if(key != null)
				list.add(key);
			return list;
		}

	}

	/**
	 * The base unlocalized brand added to crafted items
	 */
	public final static String CRAFTING_BRAND_STR_PREFIX="This is the work of ";

	/**
	 * The base unlocalized brand added to anonymously crafted items
	 */
	public final static String CRAFTING_BRAND_STR_ANON=CRAFTING_BRAND_STR_PREFIX+"an anonymous craftsman.";

	/**
	 * The base unlocalized brand added to named crafted items
	 */
	public final static String CRAFTING_BRAND_STR_NAME=CRAFTING_BRAND_STR_PREFIX+"@x1.";

	/**
	 * Returns whether the given item could have been crafted by this skill.
	 * @param I the item to examine
	 * @return true if the item is consistent with this crafting, or false otherwise
	 */
	public boolean mayICraft(final Item I);

	/**
	 * Returns true if mundane items can be demonstructed into recipes with this skill.
	 * @return true if mundane items can be demonstructed into recipes with this skill.
	 */
	public boolean supportsDeconstruction();

	/**
	 * Returns the ratio of the weight of material used to make an item with this
	 * skill versus the item weight when finished
	 * @param bundling true if the item being created is just a raw resource bundle
	 * @return the ratio of the weight of material used to make an item with this
	 */
	public double getItemWeightMultiplier(boolean bundling);
}
