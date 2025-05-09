package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class JournalLoader
{
	protected DBConnector DB=null;
	public JournalLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	public int DBCount(String journal, String from, String to)
	{
		if(journal==null)
			return 0;

		journal	= DB.injectionClean(journal);
		from	= DB.injectionClean(from);
		to		= DB.injectionClean(to);

		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			int ct=0;
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				final ResultSet R=D.query("SELECT CMFROM,CMTONM FROM CMJRNL WHERE CMJRNL='"+journal+"'");
				if((from==null)&&(to==null))
					ct=D.getRecordCount(R);
				else
				{
					while(R.next())
					{
						if(((from==null)||(from.equalsIgnoreCase(DBConnections.getRes(R,"CMFROM"))))
						&&((to==null)||(to.equalsIgnoreCase(DBConnections.getRes(R,"CMTONM")))))
							ct++;
					}
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
			return ct;
		}
	}

	public String DBGetRealName(String possibleName)
	{
		possibleName = DB.injectionClean(possibleName);

		DBConnection D=null;
		String realName=null;
		try
		{
			D=DB.DBFetch(); // add unique
			final ResultSet R=D.query("SELECT CMJRNL FROM CMJRNL WHERE CMJRNL='"+possibleName+"'");
			if(R.next())
			{
				realName=DBConnections.getRes(R,"CMJRNL");
				if(realName.length()==0)
				{
					return realName=null;
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return realName;
	}

	public long[] DBJournalLatestDateNewerThan(String journal, String to, final long olderTime)
	{
		journal = DB.injectionClean(journal);
		to		= DB.injectionClean(to);

		DBConnection D=null;
		final long[] newest=new long[]{0,0};
		try
		{
			D=DB.DBFetch(); // add max and count to fakedb
			String sql="SELECT CMUPTM FROM CMJRNL WHERE CMJRNL='"+journal+"' AND CMUPTM > " + olderTime;
			if(to != null)
				sql +=" AND CMTONM='"+to+"'";
			final ResultSet R=D.query(sql);
			while(R.next())
			{
				newest[1]++;
				final long date=R.getLong("CMUPTM");
				if(date>newest[0])
					newest[0]=date;
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return newest;
	}

	public synchronized List<String> DBReadJournals()
	{
		DBConnection D=null;
		final HashSet<String> journalsH = new HashSet<String>();
		final Vector<String> journals=new Vector<String>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMJRNL FROM CMJRNL"); // add unique to fakedb
			while(R.next())
			{
				final String which=DBConnections.getRes(R,"CMJRNL");
				if(!journalsH.contains(which))
				{
					journalsH.add(which);
					journals.addElement(which);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		return journals;
	}

	protected JournalEntry DBReadJournalEntry(final ResultSet R, final boolean fixUpdateTimestamp)
	{
		final JournalEntry entry=(JournalEntry)CMClass.getCommon("DefaultJournalEntry");
		entry.key		(DBConnections.getRes(R,"CMJKEY"));
		entry.from		(DBConnections.getRes(R,"CMFROM"));

		entry.to		(DBConnections.getRes(R,"CMTONM"));
		entry.subj		(DBConnections.getRes(R,"CMSUBJ"));
		entry.parent	(DBConnections.getRes(R,"CMPART"));
		entry.attributes(CMath.s_long(DBConnections.getRes(R,"CMATTR")));
		entry.data		(DBConnections.getRes(R,"CMDATA"));
		entry.update	(CMath.s_long(DBConnections.getRes(R,"CMUPTM")));
		entry.msgIcon	(DBConnections.getRes(R, "CMIMGP"));
		entry.views		(CMath.s_int(DBConnections.getRes(R, "CMVIEW")));
		entry.replies	(CMath.s_int(DBConnections.getRes(R, "CMREPL")));
		entry.msg		(DBConnections.getRes(R,"CMMSGT"));
		entry.expiration(CMath.s_long(DBConnections.getRes(R, "CMEXPI")));
		entry.dateStr   (DBConnections.getRes(R, "CMDATE"));
		if(fixUpdateTimestamp)
		{
			if(entry.update()<entry.date())
				entry.update(entry.date());

			final String subject=entry.subj().toUpperCase();
			if((subject.startsWith("MOTD"))
			||(subject.startsWith("MOTM"))
			||(subject.startsWith("MOTY")))
			{
				final char c=subject.charAt(3);
				entry.subj(entry.subj().substring(4));
				long last=entry.date();
				if(c=='D')
					last=last+TimeManager.MILI_DAY;
				else
				if(c=='M')
					last=last+TimeManager.MILI_MONTH;
				else
				if(c=='Y')
					last=last+TimeManager.MILI_YEAR;
				entry.update(last);
			}
		}
		return entry;
	}

	public List<JournalEntry> DBReadJournalPageMsgs(String journal, String parent, String searchStr, final long newerDate, final int limit)
	{
		journal		= DB.injectionClean(journal);
		parent		= DB.injectionClean(parent);
		searchStr	= DB.injectionClean(searchStr);

		final boolean searching=((searchStr!=null)&&(searchStr.length()>0));

		final Vector<JournalEntry> journalV=new Vector<JournalEntry>(); // return value
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT * FROM CMJRNL WHERE";
			if(newerDate==0)
				sql+=" CMUPTM >= 0"; // <0 are the meta msgs
			else
			if((parent==null)||(parent.length()>0))
				sql+=" CMUPTM > " + newerDate;
			else
				sql+=" CMUPTM < " + newerDate;

			if((journal!=null)&&(journal.length()>0))
				sql += " AND CMJRNL='"+journal+"'";
			if(parent != null)
				sql += " AND CMPART='"+parent+"'";
			if(searching)
				sql += " AND (CMSUBJ LIKE '%"+searchStr+"%' OR CMMSGT LIKE '%"+searchStr+"%')";
			sql += " ORDER BY CMUPTM";
			if((parent==null)||(parent.length()>0))
				sql += " ASC";
			else
				sql += " DESC";

			final ResultSet R=D.query(sql);
			int cardinal=0;
			JournalEntry entry;
			final Map<String,JournalEntry> parentKeysDone=new HashMap<String,JournalEntry>();
			while(((cardinal < limit)||(limit==0)) && R.next())
			{
				entry = DBReadJournalEntry(R,true);
				if((parent!=null)&&(CMath.bset(entry.attributes(),JournalEntry.JournalAttrib.STUCKY.bit)))
					continue;
				if(searching)
				{
					final long updated=entry.update();
					final String parentKey=((entry.parent()!=null)&&(entry.parent().length()>0)) ? entry.parent() : entry.key();
					if(parentKeysDone.containsKey(parentKey))
					{
						parentKeysDone.get(parentKey).update(updated);
						continue;
					}
					if((entry.parent()!=null)&&(entry.parent().length()>0))
					{
						final JournalEntry oldEntry=entry;
						entry=DBReadJournalEntry(journal, entry.parent());
						if(entry==null)
							entry=oldEntry;
					}
					parentKeysDone.put(entry.key(),entry);
					entry.update(updated);
				}
				entry.cardinal(++cardinal);
				journalV.addElement(entry);
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader","Query ("+journalV.size()+"): "+sql);
			if((journalV.size()>0)&&(parent!=null)) // set last entry -- make sure its not stucky
			{
				journalV.lastElement().lastEntry(true);
				while(R.next())
				{
					final long attributes=R.getLong("CMATTR");
					if(CMath.bset(attributes,JournalEntry.JournalAttrib.STUCKY.bit))
						continue;
					journalV.lastElement().lastEntry(false);
					break;
				}
			}
			else
			if((journalV.size()>0)&&(!R.next()))
				journalV.lastElement().lastEntry(true);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		return journalV;
	}

	public List<Long> DBReadJournalPages(String journal, String parent, String searchStr, final int limit)
	{
		journal		= DB.injectionClean(journal);
		parent		= DB.injectionClean(parent);
		searchStr	= DB.injectionClean(searchStr);

		final boolean searching=((searchStr!=null)&&(searchStr.length()>0));

		final Vector<Long> journalV=new Vector<Long>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT * FROM CMJRNL WHERE CMUPTM > 0"; // <0 are the meta msgs

			if((journal!=null)&&(journal.length()>0))
				sql += " AND CMJRNL='"+journal+"'";
			if(parent != null)
				sql += " AND CMPART='"+parent+"'";
			if(searching)
				sql += " AND (CMSUBJ LIKE '%"+searchStr+"%' OR CMMSGT LIKE '%"+searchStr+"%')";
			sql += " ORDER BY CMUPTM";
			if((parent==null)||(parent.length()>0))
				sql += " ASC";
			else
				sql += " DESC";

			final ResultSet R=D.query(sql);
			JournalEntry entry;
			final Map<String,JournalEntry> parentKeysDone=new HashMap<String,JournalEntry>();
			boolean done=false;
			while( (!done) && R.next())
			{
				entry = DBReadJournalEntry(R,true);
				if(searching)
				{
					final long updated=entry.update();
					final String parentKey=((entry.parent()!=null)&&(entry.parent().length()>0)) ? entry.parent() : entry.key();
					if(parentKeysDone.containsKey(parentKey))
					{
						parentKeysDone.get(parentKey).update(updated);
						continue;
					}
					if((entry.parent()!=null)&&(entry.parent().length()>0))
					{
						final JournalEntry oldEntry=entry;
						entry=DBReadJournalEntry(journal, entry.parent());
						if(entry==null)
							entry=oldEntry;
					}
					parentKeysDone.put(entry.key(),entry);
					entry.update(updated);
				}
				journalV.addElement(Long.valueOf(entry.update()));
				if(limit > 0)
				{
					done=true;
					for(int i=0;i<limit-1;i++)
					{
						done = !R.next();
						if(done)
							break;
					}
				}
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader","Query ("+journalV.size()+"): "+sql);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		return journalV;
	}

	public List<JournalEntry> DBSearchAllJournalEntries(String journal, String searchStr)
	{
		journal		= DB.injectionClean(journal);
		searchStr	= DB.injectionClean(searchStr);

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT * FROM CMJRNL WHERE CMJRNL='"+journal+"'";
				sql += " AND (CMSUBJ LIKE '%"+searchStr+"%' OR CMMSGT LIKE '%"+searchStr+"%')";
				sql += " ORDER BY CMUPTM";
				sql += " ASC";

			return this.makeJournalEntryList(journal,D.query(sql), 100);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public int DBCountJournalMsgsNewerThan(String journal, String to, final long olderDate)
	{
		journal	= DB.injectionClean(journal);
		to		= DB.injectionClean(to);

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT CMJKEY FROM CMJRNL WHERE CMUPTM > " + olderDate+" AND CMJRNL='"+journal+"'";
			if(to != null)
				sql += " AND CMTONM='"+to+"'";
			final ResultSet R=D.query(sql);
			return D.getRecordCount(R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return 0;
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public List<JournalEntry> DBReadJournalMsgsNewerThan(String journal, String to, final long olderDate)
	{
		journal	= DB.injectionClean(journal);
		to		= DB.injectionClean(to);

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT CMJKEY FROM CMJRNL WHERE CMUPTM > " + olderDate+" AND CMJRNL='"+journal+"'";
			if(to != null)
				sql += " AND CMTONM='"+to+"'";
			sql += " ORDER BY CMUPTM ASC";
			final ResultSet R=D.query(sql);
			return this.makeJournalEntryList(journal, R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public List<JournalEntry> makeJournalEntryList(final String journalID, final ResultSet R) throws SQLException
	{
		return makeJournalEntryList(journalID, R, Long.MAX_VALUE);
	}

	public List<JournalEntry> makeJournalEntryList(final String journalID, final ResultSet R, long limit) throws SQLException
	{
		final List<String> ids = new ArrayList<String>();
		while(R.next() && ((limit==0)||(--limit>=0)))
			ids.add(R.getString("CMJKEY"));
		R.close();
		final String lastEntry = (ids.size()==0) ? "" : ids.get(ids.size()-1);
		return new FullConvertingList<String,JournalEntry>(ids, new FullConverter<String,JournalEntry>()
		{
			@Override
			public JournalEntry convert(final int cardinal, final String obj)
			{
				final JournalEntry j=DBReadJournalEntry(journalID, obj);
				if(j != null)
				{
					j.cardinal(cardinal);
					if(j.key().equals(lastEntry))
						j.lastEntry(true);
					return j;
				}
				return null;
			}

			@Override
			public String reverseConvert(final JournalEntry obj)
			{
				return obj==null ? "" : obj.key();
			}
		});
	}

	public List<JournalEntry> DBReadJournalMsgsExpiredBefore(String journal, String to, final long newestDate)
	{
		journal	= DB.injectionClean(journal);
		to		= DB.injectionClean(to);

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT CMJKEY FROM CMJRNL WHERE CMEXPI < " + newestDate + " AND CMJRNL='"+journal+"'";
			if(to != null)
				sql += " AND CMTONM='"+to+"'";
			sql += "ORDER BY CMEXPI ASC";
			final ResultSet R=D.query(sql);
			return makeJournalEntryList(journal,R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public List<JournalEntry> DBReadJournalMsgsOlderThan(String journal, String to, final long newestDate)
	{
		journal	= DB.injectionClean(journal);
		to		= DB.injectionClean(to);

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String sql="SELECT CMJKEY FROM CMJRNL WHERE CMUPTM < " + newestDate + " AND CMJRNL='"+journal+"'";
			if(to != null)
				sql += " AND CMTONM='"+to+"'";
			sql += "ORDER BY CMUPTM ASC";
			final ResultSet R=D.query(sql);
			return makeJournalEntryList(journal,R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public List<JournalEntry> DBReadJournalMsgsByTimeStamps(String journalID, String from, final long startRange, final long endRange)
	{
		journalID = DB.injectionClean(journalID);

		final List<JournalEntry> entries = new Vector<JournalEntry>();
		if(journalID==null)
			return entries;
		synchronized(CMClass.getSync("JOURNAL_"+journalID.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			String fromSQL = "";
			if((from != null)&&(from.length()>0))
			{
				from = DB.injectionClean(from);
				fromSQL += " AND CMFROM='"+from+"' ";
			}
			try
			{
				D=DB.DBFetch();
				String sql="SELECT * FROM CMJRNL WHERE CMJRNL='"+journalID+"' "
						+ "AND CMEXPI >= "+startRange+" AND CMUPTM <= "+endRange+" ";
				sql += fromSQL + " ORDER BY CMUPTM ASC";
				ResultSet R = D.query(sql);
				R = D.query(sql);
				while(R.next())
					entries.add(DBReadJournalEntry(R,true));
				R.close();
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
		return entries;
	}

	public List<JournalEntry> DBReadJournalMsgsByUpdateRange(String journalID, String from, final long startRange, final long endRange)
	{
		journalID = DB.injectionClean(journalID);

		final List<JournalEntry> entries = new Vector<JournalEntry>();
		if(journalID==null)
			return entries;
		synchronized(CMClass.getSync("JOURNAL_"+journalID.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String sql="SELECT * FROM CMJRNL WHERE CMJRNL='"+journalID+"' "
						+ "AND CMUPTM >= "+startRange+" "
						+ "AND CMUPTM <= "+endRange+" ";
				if((from != null)&&(from.length()>0))
				{
					from = DB.injectionClean(from);
					sql += " AND CMFROM='"+from+"' ";
				}
				sql += " ORDER BY CMUPTM ASC";
				final ResultSet R = D.query(sql);
				while(R.next())
					entries.add(DBReadJournalEntry(R,true));
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
		return entries;
	}

	public List<JournalEntry> DBReadAllJournalMsgsByExpiDateStr(String journalID, final long startRange, String searchStr)
	{
		journalID = DB.injectionClean(journalID);

		final List<JournalEntry> entries = new Vector<JournalEntry>();
		if(journalID==null)
			return entries;
		synchronized(CMClass.getSync("JOURNAL_"+journalID.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String sql="SELECT * FROM CMJRNL WHERE CMJRNL='"+journalID+"' "
						+ "AND CMEXPI >= "+startRange+" ";
				if((searchStr != null)&&(searchStr.length()>0))
				{
					searchStr = DB.injectionClean(searchStr);
					sql += " AND CMDATE LIKE '%"+searchStr+"%' ";
				}
				sql += " ORDER BY CMUPTM ASC";
				final ResultSet R = D.query(sql);
				while(R.next())
					entries.add(DBReadJournalEntry(R,false));
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
		return entries;
	}

	public List<JournalEntry> DBReadJournalMsgsByExpiRange(String journalID, String from, final long startRange, final long endRange, String search)
	{
		journalID = DB.injectionClean(journalID);

		final List<JournalEntry> entries = new Vector<JournalEntry>();
		if(journalID==null)
			return entries;
		synchronized(CMClass.getSync("JOURNAL_"+journalID.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String sql="SELECT * FROM CMJRNL WHERE CMJRNL='"+journalID+"' "
						+ "AND CMEXPI >= "+startRange+" "
						+ "AND CMEXPI <= "+endRange+" ";
				if((search != null)&&(search.length()>0))
				{
					search = DB.injectionClean(search);
					sql += " AND CMDATA LIKE '%"+search+"%' ";
				}
				if((from != null)&&(from.length()>0))
				{
					from = DB.injectionClean(from);
					sql += " AND CMFROM='"+from+"' ";
				}
				sql += " ORDER BY CMUPTM ASC";
				final ResultSet R = D.query(sql);
				while(R.next())
					entries.add(DBReadJournalEntry(R,true));
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
		return entries;
	}

	public List<JournalEntry> DBReadJournalMsgsSorted(String journal, final boolean ascending, final long limit, final boolean useUpdateSort)
	{
		journal = DB.injectionClean(journal);

		if(journal==null)
			return new Vector<JournalEntry>();
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String sql="SELECT CMJKEY FROM CMJRNL WHERE CMJRNL='"+journal+"' ORDER BY "+(useUpdateSort?"CMUPTM ":"CMDATE ");
				sql += ascending ? "ASC" : "DESC";
				return this.makeJournalEntryList(journal,D.query(sql), limit);
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return null;
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}

	public List<JournalEntry> DBReadJournalMsgsSorted(String journal, final boolean ascending, final int limit, final String[] tos, final boolean useUpdateSort)
	{
		journal = DB.injectionClean(journal);

		if(journal==null)
			return new Vector<JournalEntry>();
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			//Resources.submitResource("JOURNAL_"+journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String sql="SELECT CMJKEY FROM CMJRNL WHERE CMJRNL='"+journal+"'";
				if((tos != null)&&(tos.length>0))
				{
					final StringBuilder orBox = new StringBuilder("");
					for(String to : tos)
					{
						if(orBox.length()>0)
							orBox.append(" OR");
						to = DB.injectionClean(to);
						if(to.indexOf('%')>=0)
							orBox.append(" CMTONM LIKE '"+to+"'");
						else
							orBox.append(" CMTONM = '"+to+"'");
					}
					sql +=" AND ("+orBox.toString()+")";
				}
				sql +=" ORDER BY "+(useUpdateSort?"CMUPTM ":"CMDATE ");
				sql += ascending ? "ASC" : "DESC";
				return this.makeJournalEntryList(journal,D.query(sql), limit);
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return null;
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}

	public JournalEntry DBReadJournalEntry(String journal, String messageKey)
	{
		journal	= DB.injectionClean(journal);
		messageKey	= DB.injectionClean(messageKey);

		if(journal==null)
			return null;
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				final String sql="SELECT * FROM CMJRNL WHERE CMJKEY='"+messageKey+"' AND CMJRNL='"+journal+"'";
				final ResultSet R=D.query(sql);
				if(R.next())
					return DBReadJournalEntry(R,true);
			}
			catch(final Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return null;
			}
			finally
			{
				DB.DBDone(D);
			}
			return null;
		}
	}

	public int getFirstMsgIndex(final List<JournalEntry> journal, String from, String to, String subj)
	{
		from	= DB.injectionClean(from);
		to		= DB.injectionClean(to);
		subj	= DB.injectionClean(subj);

		if(journal==null)
			return -1;
		for(int i=0;i<journal.size();i++)
		{
			final JournalEntry E=journal.get(i);
			if((from!=null)&&(!(E.from()).equalsIgnoreCase(from)))
				continue;
			if((to!=null)&&(!(E.to()).equalsIgnoreCase(to)))
				continue;
			if((subj!=null)&&(!(E.subj()).equalsIgnoreCase(subj)))
				continue;
			return i;
		}
		return -1;
	}

	public void DBUpdateJournal(String key, String subject, String msg, final long newAttributes)
	{
		key = DB.injectionClean(key);
		subject = DB.injectionClean(subject);
		msg = DB.injectionClean(msg);

		final String sql="UPDATE CMJRNL SET CMSUBJ=?, CMMSGT=?, CMATTR="+newAttributes+" WHERE CMJKEY='"+key+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.updateWithClobs(sql,subject,msg);
	}

	public void DBUpdateJournal(String journal, final JournalEntry entry)
	{
		journal			=DB.injectionClean(journal);
		entry.data		(DB.injectionClean(entry.data()));
		entry.from		(DB.injectionClean(entry.from()));
		entry.key		(DB.injectionClean(entry.key()));
		entry.msg		(DB.injectionClean(entry.msg()));
		entry.msgIcon	(DB.injectionClean(entry.msgIcon()));
		entry.parent	(DB.injectionClean(entry.parent()));
		entry.subj		(DB.injectionClean(entry.subj()));
		entry.to		(DB.injectionClean(entry.to()));

		String sql="UPDATE CMJRNL SET "
				  +"CMFROM='"+entry.from()+"' , "
				  +"CMDATE='"+entry.dateStr()+"' , "
				  +"CMTONM='"+entry.to()+"' , "
				  +"CMSUBJ=? ,"
				  +"CMPART='"+entry.parent()+"' ,"
				  +"CMATTR="+entry.attributes()+" ,"
				  +"CMDATA=? "
				  +"WHERE CMJRNL='"+journal+"' AND CMJKEY='"+entry.key()+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.updateWithClobs(sql,entry.subj(),entry.data());

		sql="UPDATE CMJRNL SET "
		  + "CMUPTM="+entry.update()+", "
		  + "CMIMGP='"+entry.msgIcon()+"', "
		  + "CMVIEW="+entry.views()+", "
		  + "CMREPL="+entry.replies()+", "
		  + "CMEXPI="+entry.expiration()+", "
		  + "CMMSGT=? "
		  + "WHERE CMJRNL='"+journal+"' AND CMJKEY='"+entry.key()+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.updateWithClobs(sql, entry.msg());
	}

	public void DBTouchJournalMessage(final String key)
	{
		DBTouchJournalMessage(key,System.currentTimeMillis());
	}

	public void DBTouchJournalMessage(String key, final long newDate)
	{
		key = DB.injectionClean(key);
		final String sql="UPDATE CMJRNL SET CMUPTM="+newDate+" WHERE CMJKEY='"+key+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.update(sql);
	}

	public void DBUpdateMessageReplies(String messageKey, final int numReplies)
	{
		messageKey = DB.injectionClean(messageKey);
		final String sql="UPDATE CMJRNL SET CMUPTM="+System.currentTimeMillis()+", CMREPL="+numReplies+" WHERE CMJKEY='"+messageKey+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.update(sql);
	}

	public void DBUpdateJournalMessageViews(String key, final int views)
	{
		key = DB.injectionClean(key);
		final String sql="UPDATE CMJRNL SET CMVIEW="+views+" WHERE CMJKEY='"+key+"'";
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
			Log.debugOut("JournalLoader",sql);
		DB.update(sql);
	}

	public void DBDeletePlayerPrivateJournalEntries(String name)
	{
		name = DB.injectionClean(name);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final Vector<String> deletableEntriesV=new Vector<String>();
			final Vector<String> deletableAttachmentsV=new Vector<String>();
			final Vector<String[]> notifiableParentsV=new Vector<String[]>();
			//Resources.submitResource("JOURNAL_"+journal);
			final String sql="SELECT CMJRNL,CMJKEY,CMPART,CMATTR FROM CMJRNL WHERE CMTONM='"+name+"'";
			final ResultSet R=D.query(sql);
			while(R.next())
			{
				final String journalName=R.getString("CMJRNL");
				if((CMLib.journals().getForumJournal(journalName)!=null)
				||(CMLib.journals().getCommandJournal(journalName)!=null))
					continue;
				final String key=R.getString("CMJKEY");
				final String parent=R.getString("CMPART");
				final int attrib=R.getInt("CMATTR");
				if((parent!=null)&&(parent.length()>0))
					notifiableParentsV.add(new String[]{journalName,parent});
				deletableEntriesV.add(key);
				if(CMath.bset(attrib,(int)JournalEntry.JournalAttrib.ATTACHMENT.bit))
					deletableAttachmentsV.add(key);
			}
			for(final String[] parentKey : notifiableParentsV)
			{
				if(!deletableEntriesV.contains(parentKey[0]))
				{
					final JournalEntry parentEntry=DBReadJournalEntry(parentKey[0], parentKey[1]);
					if(parentEntry!=null)
						DBUpdateMessageReplies(parentEntry.key(),parentEntry.replies()-1);
				}
			}
			for(final String s : deletableEntriesV)
				D.update("DELETE FROM CMJRNL WHERE CMJKEY='"+s+"' OR CMPART='"+s+"'",0);
			D.update("DELETE FROM CMJRNL WHERE CMJRNL='SYSTEM_CALENDAR' AND CMFROM='"+name+"'",0);
			for(final String s : deletableAttachmentsV)
			{
				CMLib.database().DBDeleteVFSFileLike(s+"/%", CMFile.VFS_MASK_ATTACHMENT);
				CMLib.database().DBDeleteVFSFileLike("%/"+s+"/%", CMFile.VFS_MASK_ATTACHMENT);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdateJournalMetaData(String journal, final JournalsLibrary.JournalMetaData metaData)
	{
		journal = DB.injectionClean(journal);
		metaData.imagePath	(DB.injectionClean(metaData.imagePath()));
		metaData.introKey	(DB.injectionClean(metaData.introKey()));
		metaData.longIntro	(DB.injectionClean(metaData.longIntro()));
		metaData.name		(DB.injectionClean(metaData.name()));
		metaData.shortIntro(DB.injectionClean(metaData.shortIntro()));

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+metaData.name()+"' AND CMTONM='JOURNALINTRO'");
			JournalEntry entry = null;
			if(R.next())
				entry = this.DBReadJournalEntry(R,false);
			R.close();
			if(entry!=null)
			{
				entry.subj		(metaData.shortIntro());
				entry.msg		(metaData.longIntro());
				entry.data		(metaData.imagePath());
				entry.attributes(JournalEntry.JournalAttrib.PROTECTED.bit);
				entry.dateStr	("-1");
				entry.update	(-1);
				DBUpdateJournal(journal,entry);
			}
			else
			{
				entry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
				entry.subj		(metaData.shortIntro());
				entry.msg		(metaData.longIntro());
				entry.data		(metaData.imagePath());
				entry.to		("JOURNALINTRO");
				entry.from		("");
				entry.attributes(JournalEntry.JournalAttrib.PROTECTED.bit);
				entry.dateStr	("-1");
				entry.update	(-1);
				this.DBWrite(journal, entry);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBReadJournalSummaryStats(final String journalID, final JournalsLibrary.JournalMetaData metaData)
	{
		metaData.imagePath	(DB.injectionClean(metaData.imagePath()));
		metaData.introKey	(DB.injectionClean(metaData.introKey()));
		metaData.longIntro	(DB.injectionClean(metaData.longIntro()));
		metaData.name		(DB.injectionClean(journalID));
		metaData.shortIntro(DB.injectionClean(metaData.shortIntro()));

		DBConnection D=null;
		String topKey = null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+metaData.name()+"' AND CMTONM='ALL' AND CMPART=''");
			long topTime = 0;
			while(R.next())
			{
				final String key=R.getString("CMJKEY");
				final long updateTime=R.getLong("CMUPTM");
				final long attributes=R.getLong("CMATTR");
				final int replies=R.getInt("CMREPL");
				metaData.posts(metaData.posts()+1);
				metaData.threads(metaData.threads()+1);
				metaData.posts(metaData.posts()+replies);
				if(updateTime>topTime)
				{
					topTime=updateTime;
					topKey=key;
				}
				if(CMath.bset(attributes,JournalEntry.JournalAttrib.STUCKY.bit))
				{
					if(metaData.stuckyKeys()==null)
						metaData.stuckyKeys(new Vector<String>());
					metaData.stuckyKeys().add(key);
				}
			}
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(topKey != null)
			metaData.latestKey(topKey);
		else
			metaData.latestKey(null);
		metaData.imagePath("");
		metaData.shortIntro("[This is the short journal description.]");
		metaData.longIntro("[This is the long journal description.    To change it, use forum Admin, or create a journal entry addressed to JOURNALINTRO with updatetime 0.]");
		try
		{
			if(D==null)
				D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+metaData.name()+"' AND CMTONM='JOURNALINTRO'");
			if(R.next())
			{
				final JournalEntry entry = this.DBReadJournalEntry(R,false);
				metaData.introKey	(entry.key());
				metaData.longIntro	(entry.msg());
				metaData.shortIntro(entry.subj());
				metaData.imagePath	(entry.data());
			}
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBDelete(String journal, String messageKey)
	{
		journal = DB.injectionClean(journal);
		messageKey = DB.injectionClean(messageKey);

		if(journal==null)
			return;
		if(messageKey != null)
		{
			CMLib.database().DBDeleteVFSFileLike(messageKey+"/%", CMFile.VFS_MASK_ATTACHMENT);
			CMLib.database().DBDeleteVFSFileLike("%/"+messageKey+"/%", CMFile.VFS_MASK_ATTACHMENT);
		}
		else
			CMLib.database().DBDeleteVFSFileLike(journal+"/%", CMFile.VFS_MASK_ATTACHMENT);
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			String sql;
			if(messageKey!=null)
			{
				sql="DELETE FROM CMJRNL WHERE CMJKEY='"+messageKey+"' OR CMPART='"+messageKey+"'";
			}
			else
			{
				sql="DELETE FROM CMJRNL WHERE CMJRNL='"+journal+"'";
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader",sql);
			DB.update(sql);
		}
	}

	public void DBDeleteByFrom(String journal, String from)
	{
		journal = DB.injectionClean(journal);
		from = DB.injectionClean(from);

		if(journal==null)
			return;
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			final String sql="DELETE FROM CMJRNL WHERE CMJRNL='"+journal+"' AND CMFROM='"+from+"'";
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader",sql);
			DB.update(sql);
		}
	}

	public JournalEntry DBWriteJournalReply(String journal, String messageKey, String from, String to, String subject, String message)
	{
		journal 	= DB.injectionClean(journal);
		messageKey	= DB.injectionClean(messageKey);
		from		= DB.injectionClean(from);
		to			= DB.injectionClean(to);
		subject		= DB.injectionClean(subject);
		message		= DB.injectionClean(message);

		if(journal==null)
			return null;
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			final JournalEntry entry=DBReadJournalEntry(journal, messageKey);
			if(entry==null)
				return null;
			final long now=System.currentTimeMillis();
			final String oldkey=entry.key();
			final String oldmsg=entry.msg();
			final int replies = entry.replies()+1;
			message=oldmsg+JournalsLibrary.JOURNAL_BOUNDARY
			 +"^yReply from^w: "+from+"    ^yDate/Time ^w: "+CMLib.time().date2String(now)+"%0D"
			 +message;
			entry.msg(message);
			final String sql="UPDATE CMJRNL SET CMUPTM="+now+", CMMSGT=?, CMREPL="+replies+" WHERE CMJKEY='"+oldkey+"'";
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader",sql);
			DB.updateWithClobs(sql,message);
			return entry;
		}
	}

	public String DBWrite(final String journal, final String from, final String to, final String subject, final String message)
	{
		return DBWrite(journal, "", from, to, subject, message);
	}

	public String DBWrite(final String journal, final String journalSource, final String from, final String to, final String subject, final String message)
	{
		return DBWrite(journal,journalSource,from,to,"",subject,message);
	}

	public String DBWrite(String journal, String journalSource, String from, String to, String parentKey, String subject, String message)
	{
		journal		  = DB.injectionClean(journal);
		from		  = DB.injectionClean(from);
		to			  = DB.injectionClean(to);
		subject 	  = DB.injectionClean(subject);
		parentKey 	  = DB.injectionClean(parentKey);
		message 	  = DB.injectionClean(message);
		journalSource = DB.injectionClean(journalSource);

		final JournalEntry entry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
		entry.key (null);
		entry.data (journalSource);
		entry.from (from);
		entry.dateStr (""+System.currentTimeMillis());
		entry.to (to);
		entry.subj (subject);
		entry.msg (message);
		entry.update (System.currentTimeMillis());
		entry.parent (parentKey);
		return DBWrite(journal, entry);
	}

	public String DBWrite(String journal, final JournalEntry entry)
	{
		journal			=DB.injectionClean(journal);
		entry.data		(DB.injectionClean(entry.data()));
		entry.from		(DB.injectionClean(entry.from()));
		entry.key		(DB.injectionClean(entry.key()));
		entry.msg		(DB.injectionClean(entry.msg()));
		entry.msgIcon	(DB.injectionClean(entry.msgIcon()));
		entry.parent	(DB.injectionClean(entry.parent()));
		entry.subj		(DB.injectionClean(entry.subj()));
		entry.to		(DB.injectionClean(entry.to()));

		if(journal==null)
			return null;
		synchronized(CMClass.getSync("JOURNAL_"+journal.toUpperCase()))
		{
			final long now=System.currentTimeMillis();
			if(entry.subj().length()>255)
				entry.subj(entry.subj().substring(0,255));
			if(entry.key()==null)
				entry.key(journal+now+Math.random());
			if(entry.date()==0)
				entry.dateStr(""+now);
			if(entry.update()==0)
				entry.update(now);
			final String sql = "INSERT INTO CMJRNL ("
				+"CMJKEY, "
				+"CMJRNL, "
				+"CMFROM, "
				+"CMDATE, "
				+"CMTONM, "
				+"CMSUBJ, "
				+"CMPART, "
				+"CMATTR, "
				+"CMDATA, "
				+"CMUPTM, "
				+"CMIMGP, "
				+"CMVIEW, "
				+"CMREPL, "
				+"CMMSGT, "
				+"CMEXPI"
				+") VALUES ('"
				+entry.key()
				+"','"+journal
				+"','"+entry.from()
				+"','"+entry.dateStr()
				+"','"+entry.to()
				+"',?"
				+",'"+entry.parent()
				+"',"+entry.attributes()
				+",?"
				+","+entry.update()
				+",'"+entry.msgIcon()
				+"',"+entry.views()
				+","+entry.replies()
				+",?"
				+","+entry.expiration()+")";
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMJRNL))
				Log.debugOut("JournalLoader",sql);
			DB.updateWithClobs(sql , entry.subj(), entry.data(), entry.msg());
			if((entry.parent()!=null)&&(entry.parent().length()>0))
			{
				// this constitutes a threaded reply -- update the counter
				final JournalEntry parentEntry=DBReadJournalEntry(journal, entry.parent());
				if(parentEntry!=null)
					DBUpdateMessageReplies(parentEntry.key(),parentEntry.replies()+1);
			}
			if(System.currentTimeMillis()==now) // ensures unique keys.
				CMLib.s_sleep(1);
		}
		return entry.key();
	}
}
