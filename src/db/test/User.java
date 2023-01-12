package db.test;

import java.sql.Timestamp;

import db.DbObject;
import db.DbObjects;
import db.FldBool;
import db.FldDateTime;
import db.FldDbl;
import db.FldFlt;
import db.FldInt;
import db.FldLng;
import db.FldStr;
import db.FldTs;
import db.IndexFt;
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
	public final static FldDateTime date = new FldDateTime();
	public final static FldDateTime dateTime = new FldDateTime();

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
	public String getDescription() {
		return getStr(description);
	}

	public void setDescription(final String v) {
		set(description, v);
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
		groupPic.set(this, o == null ? 0 : o.id());
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public File createFile() {
		return (File) files.create(this);
	}

	public DbObjects getFiles() {
		return files.get(this);
	}

	public void deleteFile(final int id) {
		files.delete(this, id);
	}

	public void deleteFile(final File o) {
		files.delete(this, o.id());
	}

	public void deleteAllFiles() {
		files.deleteAll(this);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public void addRefFile(final int id) {
		refFiles.add(this, id);
	}

	public void addRefFile(final File o) {
		refFiles.add(this, o.id());
	}

	public DbObjects getRefFiles() {
		return refFiles.get(this);
	}

	public void removeRefFile(final int id) {
		refFiles.remove(this, id);
	}

	public void removeRefFile(final File o) {
		refFiles.remove(this, o.id());
	}

	public void removeAllRefFiles() {
		refFiles.removeAll(this);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Game createGame() {
		return (Game) games.create(this);
	}

	public DbObjects getGames() {
		return games.get(this);
	}

	public void deleteGame(final int id) {
		games.delete(this, id);
	}

	public void deleteGame(final Game o) {
		games.delete(this, o.id());
	}

	public void deleteAllGames() {
		games.deleteAll(this);
	}
}