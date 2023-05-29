package zen.lang;

class Tokenizer {
	private String src;
	private int pos;
	private int linenm = 1; // current line number
	private int charnm = 1; // character number on current line

	public Tokenizer(String src) {
		this.src = src;
	}

	public Token nextToken() {
		int pos_ws_lft;
		int pos_strt;
		int pos_end;
		int pos_ws_rht;
		StringBuilder sb_ws_lft = new StringBuilder();
		StringBuilder sb_id = new StringBuilder();
		StringBuilder sb_ws_rht = new StringBuilder();

		int p = pos;
		pos_ws_lft = p;
		while (true) {
			if (p == src.length())
				break;
			char ch = src.charAt(p);
			if (Character.isWhitespace(ch)) {
				sb_ws_lft.append(ch);
				p++;
				if (ch == '\n') {
					linenm++;
					charnm = 1;
				} else {
					charnm++;
				}
				continue;
			}
			break;
		}
		pos_strt = p;
		int token_linenm = linenm;
		int token_charnm = charnm;
		while (true) {
			if (p == src.length())
				break;
			char ch = src.charAt(p);
			if (Character.isWhitespace(ch))
				break;
			sb_id.append(ch);
			p++;
			charnm++;
			continue;
		}
		pos_end = p;
		while (true) {
			if (p == src.length())
				break;
			char ch = src.charAt(p);
			if (Character.isWhitespace(ch)) {
				sb_ws_rht.append(ch);
				p++;
				if (ch == '\n') {
					linenm++;
					charnm = 1;
					break;
				} else {
					charnm++;
				}
				continue;
			}
			break;
		}
		pos_ws_rht = p;
		pos = p;
		Token tk = new Token(sb_id.toString(), sb_ws_lft.toString(), sb_ws_rht.toString(), pos_ws_lft, pos_strt,
				pos_end, pos_ws_rht, token_linenm, token_charnm);
		return tk;
	}

	void pushBackToken(Token tk) {
		int nchars = tk.totalNChars();
		while (nchars-- != 0) {
			pos--;
			if (src.charAt(pos) == '\n') {
				linenm--;
				charnm = findCharNmInCurrentLine();
			} else {
				charnm--;
			}
		}
	}

	private int findCharNmInCurrentLine() {
		// ldi r3 0x0001\nadd_
		int p = pos;
		while (true) {
			if (src.charAt(p) == '\n') {
				return pos - p;
			}
			p--;
			if (p == 0) {
				return pos;
			}
		}
	}

	public String readComment() {
		StringBuilder sb = new StringBuilder();
		while (true) {
			if (pos != 0 && src.charAt(pos - 1) == '\n') {
				// check if it is an empty comment. example: "ld 0x1234 r1 #\n"
				// without this check the next line would be read as a comment
				return "";
			}
			char ch = src.charAt(pos);
			sb.append(ch);
			pos++;
			charnm++;
			if (pos == src.length()) {
				return sb.toString();
			}
			if (ch == '\n') {
				linenm++;
				charnm = 1;
				return sb.toString();
			}
		}
	}
}