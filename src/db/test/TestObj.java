package db.test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.List;

import db.DbObject;
import db.FldChars;
import db.FldDateTime;
import db.FldSerializable;
import db.FldStr;

public final class TestObj extends DbObject {

    public static final FldSerializable list = new FldSerializable();
    public static final FldChars md5 = new FldChars(32, "abc");
    public static final FldStr subject = new FldStr(200, "no 'subject'");
    public static final FldDateTime dateTime = new FldDateTime(Timestamp.valueOf("2022-11-28 03:28:00"));

    @SuppressWarnings("unchecked")
    public List<String> getList() {
        final Object v = list.getObj(this);
        if (v == null) {
            return null;
        }

        if (v instanceof List<?>) {
            return (List<String>) v;
        }

        // convert from sql representation
        final byte[] ba = (byte[]) list.getObj(this);
        try {
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
            final List<String> ls = (List<String>) ois.readObject();
            ois.close();
            list.putObj(this, ls); // put without marking field dirty
            return ls;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void setList(final List<String> v) {
        list.setObj(this, v);
    }

    public String getMd5() {
        return md5.getChars(this);
    }

    public void setMd5(final String v) {
        md5.setChars(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getSubject() {
        return subject.getStr(this);
    }

    public void setSubject(final String v) {
        subject.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getDateTime() {
        return dateTime.getDateTime(this);
    }

    public void setDateTime(final Timestamp v) {
        dateTime.setDateTime(this, v);
    }

}
