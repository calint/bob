package zen.zasm;

final class Token {

    private final String ws_lft;
    private final String id;
    private final String ws_rht;
    private final int pos_ws_lft;
    private final int pos_strt;
    private final int pos_end;
    private final int pos_ws_rht;
    private final int linenm;
    private final int charnm;

    public Token(String id, String ws_lft, String ws_rht, int pos_ws_lft, int pos_strt, int pos_end, int pos_ws_rht,
            int linenm, int charnm) {
        this.id = id;
        this.ws_lft = ws_lft;
        this.ws_rht = ws_rht;
        this.pos_ws_lft = pos_ws_lft;
        this.pos_strt = pos_strt;
        this.pos_end = pos_end;
        this.pos_ws_rht = pos_ws_rht;
        this.linenm = linenm;
        this.charnm = charnm;
    }

    public String toSource() {
        return ws_lft.toString() + id.toString() + ws_rht.toString();
    }

    public String toDebug() {
        return linenm + ":" + charnm + ": " + id;
    }

    public boolean isEmpty() {
        return id.length() == 0;
    }

    public boolean isId(String s) {
        return id.equals(s);
    }

    public String id() {
        return id;
    }

    public String sourcePos() {
        return linenm + ":" + charnm;
    }

    public int totalNChars() {
        return pos_ws_rht - pos_ws_lft;
    }

    public int getStartPos() {
        return pos_strt;
    }

    public int getEndPos() {
        return pos_end;
    }

}