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
    public static final FldStr name = new FldStr();
    public static final FldStr description = new FldStr();
    public static final FldStr passhash = new FldStr(32);
    public static final FldInt nlogins = new FldInt();
    public static final FldLng lng = new FldLng();
    public static final FldFlt flt = new FldFlt();
    public static final FldDbl dbl = new FldDbl();
    public static final FldBool bool = new FldBool(true);

    public static final FldTs birthTime = new FldTs();
    public static final FldDateTime date = new FldDateTime();
    public static final FldDateTime dateTime = new FldDateTime();

    public static final RelAgg profilePic = new RelAgg(File.class);
    public static final RelRef groupPic = new RelRef(File.class);
    public static final RelAggN files = new RelAggN(File.class);
    public static final RelRefN refFiles = new RelRefN(File.class);
    public static final RelAggN games = new RelAggN(Game.class);

    // public static final Index ixName = new Index(name);
    public static final IndexFt ixFt = new IndexFt(name);

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getName() {
        return name.getStr(this);
    }

    public void setName(final String v) {
        name.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getDescription() {
        return description.getStr(this);
    }

    public void setDescription(final String v) {
        description.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getPasshash() {
        return passhash.getStr(this);
    }

    public void setPasshash(final String v) {
        passhash.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public int getNlogins() {
        return nlogins.getInt(this);
    }

    public void setNlogins(final int v) {
        nlogins.setInt(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public long getLng() {
        return lng.getLng(this);
    }

    public void setLng(final long v) {
        lng.setLng(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public float getFlt() {
        return flt.getFlt(this);
    }

    public void setFlt(final float v) {
        flt.setFlt(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public double getDbl() {
        return dbl.getDbl(this);
    }

    public void setDbl(final double v) {
        dbl.setDbl(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public boolean isBool() {
        return bool.getBool(this);
    }

    public void setBool(final boolean v) {
        bool.setBool(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getBirthTime() {
        return birthTime.getTs(this);
    }

    public void setBirthTime(final Timestamp v) {
        birthTime.setTs(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getDate() {
        return date.getDateTime(this);
    }

    public void setDate(final Timestamp v) {
        date.setDateTime(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getDateTime() {
        return dateTime.getDateTime(this);
    }

    public void setDateTime(final Timestamp v) {
        dateTime.setDateTime(this, v);
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
        groupPic.set(this, o);
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
        files.delete(this, o);
    }

    public void deleteAllFiles() {
        files.deleteAll(this);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public void addRefFile(final int id) {
        refFiles.add(this, id);
    }

    public void addRefFile(final File o) {
        refFiles.add(this, o);
    }

    public DbObjects getRefFiles() {
        return refFiles.get(this);
    }

    public void removeRefFile(final int id) {
        refFiles.remove(this, id);
    }

    public void removeRefFile(final File o) {
        refFiles.remove(this, o);
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
        games.delete(this, o);
    }

    public void deleteAllGames() {
        games.deleteAll(this);
    }
}