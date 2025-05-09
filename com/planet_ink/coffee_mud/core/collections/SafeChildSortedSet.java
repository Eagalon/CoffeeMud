package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2015-2025 Bo Zimmerman

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
public class SafeChildSortedSet<K> implements SortedSet<K>
{
	private final SortedSet<K>	set;
	private final SafeCollectionHost host;

	public SafeChildSortedSet(final SortedSet<K> s, final SafeCollectionHost host)
	{
		this.set = s;
		this.host=host;
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return set.comparator();
	}

	@Override
	public K first()
	{
		return set.first();
	}

	@Override
	public SortedSet<K> headSet(final K arg0)
	{
		return new SafeChildSortedSet<K>(set.headSet(arg0), host);
	}

	@Override
	public K last()
	{
		return set.last();
	}

	@Override
	public SortedSet<K> subSet(final K arg0, final K arg1)
	{
		return new SafeChildSortedSet<K>(set.subSet(arg0, arg1), host);
	}

	@Override
	public SortedSet<K> tailSet(final K arg0)
	{
		return new SafeChildSortedSet<K>(set.tailSet(arg0), host);
	}

	@Override
	public boolean add(final K e)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		return set.containsAll(arg0);
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new SafeFeedbackIterator<K>(set.iterator(), host);
	}

	@Override
	public boolean remove(final Object o)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return set.toArray(arg0);
	}
}
