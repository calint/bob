package db.test;

import java.sql.Timestamp;
import java.util.List;

import db.DbObject;
import db.FldBool;
import db.FldDbl;
import db.FldFlt;
import db.FldInt;
import db.FldLng;
import db.FldStr;
import db.FldTs;
import db.IndexFt;
import db.Limit;
import db.Order;
import db.Query;
import db.RelAgg;
import db.RelAggN;
import db.RelRef;
import db.RelRefN;

public final class User extends DbObject {
	public final static FldStr name = new FldStr();
	public final static FldStr description = new FldStr();
	public final static FldStr passhash = new FldStr(32);
	public final static FldInt nlogins = new FldInt();
	public final static FldLng lng = new FldLng();
	public final static FldFlt flt = new FldFlt();
	public final static FldDbl dbl = new FldDbl();
	public final static FldBool bool = new FldBool(true);

	public final static FldTs birthTime = new FldTs();

	public final static RelAgg profilePic = new RelAgg(File.class);
	public final static RelRef groupPic = new RelRef(File.class);
	public final static RelAggN files = new RelAggN(File.class);
	public final static RelRefN refFiles = new RelRefN(File.class);
	public final static RelAggN games = new RelAggN(Game.class);

//	public final static Index ixName = new Index(name);
	public final static IndexFt ixFt = new IndexFt(name);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getPasshash() {
		return getStr(passhash);
	}

	public void setPasshash(final String v) {
		set(passhash, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public int getNlogins() {
		return getInt(nlogins);
	}

	public void setNlogins(final int v) {
		set(nlogins, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public long getLng() {
		return getLng(lng);
	}

	public void setLng(final long v) {
		set(lng, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public float getFlt() {
		return getFlt(flt);
	}

	public void setFlt(final float v) {
		set(flt, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public double getDbl() {
		return getDbl(dbl);
	}

	public void setDbl(final double v) {
		set(dbl, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public boolean isBool() {
		return getBool(bool);
	}

	public void setBool(final boolean v) {
		set(bool, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Timestamp getBirthTime() {
		return getTs(birthTime);
	}

	public void setBirthTime(final Timestamp v) {
		set(birthTime, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public File createFile() {
		return (File) files.create(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<File> getFiles(final Query qry, final Order ord, final Limit lmt) {
		return (List<File>) (List) files.get(this, qry, ord, lmt);
	}

	public int getFilesCount(final Query qry) {
		return files.getCount(this, qry);
	}

	public void deleteFile(final int id) {
		files.delete(this, id);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public int getProfilePicId() {
		return profilePic.getId(this);
	}

	public File getProfilePic(final boolean createIfNone) {
		return (File) profilePic.get(this, createIfNone);
	}

	public void deleteProfilePic() {
		profilePic.delete(this);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public int getGroupPicId() {
		return groupPic.getId(this);
	}

	public File getGroupPic() {
		return (File) groupPic.get(this);
	}

	public void setGroupPic(final int id) {
		groupPic.set(this, id);
	}

	public void setGroupPic(final File o) {
		groupPic.set(this, o.id());
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public void addRefFile(final int id) {
		refFiles.add(this, id);
	}

	public void addRefFile(final File o) {
		refFiles.add(this, o.id());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<File> getRefFiles(final Query qry, final Order ord, final Limit lmt) {
		return (List<File>) (List) refFiles.get(this, qry, ord, lmt);
	}

	public int getRefFilesCount(final Query qry) {
		return refFiles.getCount(this, qry);
	}

	public void removeRefFile(final int id) {
		refFiles.remove(this, id);
	}

	public void removeRefFile(final File o) {
		refFiles.remove(this, o.id());
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Game createGame() {
		return (Game) games.create(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Game> getGames(final Query qry, final Order ord, final Limit lmt) {
		return (List<Game>) (List) games.get(this, qry, ord, lmt);
	}

	public int getGamesCount(final Query qry) {
		return games.getCount(this, qry);
	}

	public void deleteGame(final int id) {
		games.delete(this, id);
	}

	public void deleteGame(final Game o) {
		games.delete(this, o.id());
	}
}