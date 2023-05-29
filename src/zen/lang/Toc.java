package zen.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

class Toc {
	public enum LinkType {
		CALL, JMP, LDI
	}

	private ArrayList<Short> instrs = new ArrayList<Short>();
	private ArrayList<Statement> stmts = new ArrayList<Statement>();
	private ArrayList<ArrayList<String>> comments = new ArrayList<ArrayList<String>>();
	private ArrayList<Link> links = new ArrayList<Link>();
	private HashMap<String, Label> allLabels = new HashMap<String, Label>();
	private Stack<Scope> scopes = new Stack<Scope>();
	private int pc;

	private static class Scope {
		HashMap<String, Label> labels = new HashMap<String, Label>(); // labels declared in this scope
		String name; // the function name

		public Scope(String name) {
			this.name = name;
		}
	}

	private static class Link {
		Token token; // source location
		int pc; // location
		String name; // name as used in source
		String scopedName; // name including scope
		LinkType type; // call, jmp, ldi

		public Link(Token token, int pc, String name, String scopedName, LinkType type) {
			this.token = token;
			this.pc = pc;
			this.name = name;
			this.scopedName = scopedName;
			this.type = type;
		}
	}

	private static class Label {
		Token token; // token which declared the label
		int pc; // program counter where it was declared
		String name; // name
		boolean isFunc; // function

		public Label(Token token, int pc, String name, boolean isFunc) {
			this.token = token;
			this.pc = pc;
			this.name = name;
			this.isFunc = isFunc;
		}
	}

	public void enterFunc(Token tk, String name) throws Throwable {
		if (!scopes.isEmpty())
			throw new Exception(tk.sourcePos() + ": cannot declare function within function");
		scopes.push(new Scope(name));
	}

	public void exitFunc(Token tk) throws Throwable {
		if (scopes.isEmpty())
			throw new Exception(tk.sourcePos() + ": not in a function");

		scopes.pop();
	}

	public int getProgramCounter() {
		return pc;
	}

	private Label getLabelInCurrentScope(String label) {
		if (scopes.isEmpty())
			return allLabels.get(label);

		return scopes.peek().labels.get(label);
	}

	private String getScopePrefix() {
		final int n = scopes.size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(scopes.get(i).name).append('.');
		}
		return sb.toString();
	}

	private void ensureCommentsArraySize() {
		final int n = comments.size();
		final int m = pc + 1; // +1 because comments before the statement are added to next pc
		for (int i = n; i < m; i++) {
			comments.add(null);
		}
	}

	public void addComment(String txt) {
		ensureCommentsArraySize();
		ArrayList<String> ls = comments.get(pc);
		if (ls == null) {
			ls = new ArrayList<String>();
			comments.set(pc, ls);
		}
		ls.add(txt);
	}

	public void write(Statement stmt, short instr) {
		stmts.add(stmt);
		instrs.add(Short.valueOf(instr));
		pc++;
	}

	public void write(Statement stmt, short instr, LinkType linkType, String label, Token tk) {
		Link lnk = new Link(tk, pc, label, getScopePrefix() + label, linkType);
		links.add(lnk);
		write(stmt, instr);
	}

	public void addLabel(Token tk, String name, boolean isFunc) throws Throwable {
		Label lbl = getLabelInCurrentScope(name);
		if (lbl != null) {
			throw new Exception(tk.sourcePos() + ": label '" + name + "' already declared at " + lbl.token.sourcePos());
		}
		if (isFunc) {
			// align at 16 bytes
			if ((pc & 0xf) != 0) {
				int pc_nxt = (pc & 0xfff0) + 0x10;
				if (pc_nxt > 0xffff)
					throw new Exception(tk.sourcePos() + ": function '" + name + "' cannot be located at '" + pc_nxt
							+ "' because it would be out-of-range of '" + 0xfff0 + "'");
				fwdPcTo(tk, pc_nxt, true);
			}
		}
		Label newLbl = new Label(tk, pc, name, isFunc);
		allLabels.put(getScopePrefix() + name, newLbl); // i.e. 'print.done' where 'done' is the label
		if (isFunc) {
			enterFunc(tk, name);
		} else {
			if (!scopes.isEmpty()) { // if within the scope of a function add the label to current scope
				scopes.peek().labels.put(name, newLbl);
			}
		}
	}

	/** Moves the program counter forward and pads the space with 0. */
	public void fwdPcTo(Token tk, int addr, boolean moveComments) throws Throwable {
		if (addr < pc)
			throw new Exception(tk.sourcePos() + ": cannot move program counter to " + addr + " because it is " + pc);
		// add empty statements in the padding
		for (int i = pc; i < addr; i++) {
			instrs.add((short) 0);
			stmts.add(null);
			comments.add(null);
		}
		if (moveComments) {
			// move the current comments to the beginning of the function
			// one more element in comments for the next instruction
			comments.add(null);
			comments.set(addr, comments.remove(pc));
		}
		pc = addr;
	}

	public void link() throws Throwable {
		for (Link lnk : links) {
			short instr = instrs.get(lnk.pc);
			switch (lnk.type) {
			case CALL: {
				Label lbl = allLabels.get(lnk.name); // get function from global scope using 'name'
				if (lbl == null)
					throw new Exception(lnk.token.sourcePos() + ": function '" + lnk.name + "' not found");

				assert ((lbl.pc & 0xf) == 0);
				instr |= lbl.pc;
				break;
			}
			case JMP: {
				Label lbl = allLabels.get(lnk.scopedName); // get label from current scope
				if (lbl == null)
					throw new Exception(lnk.token.sourcePos() + ": label '" + lnk.scopedName + "' not found");

				int dpc = lbl.pc - lnk.pc;
				if (dpc > 2047 || dpc < -2048) {
					throw new Exception(
							lnk.token.sourcePos() + ": jmp to '" + lbl.name + "' out-of-range (offset: " + dpc + ")");
				}
				instr |= (dpc & 0xfff) << 4;
				break;
			}
			case LDI: {
				Label lbl = allLabels.get(lnk.name); // get from global scope using 'name'
				if (lbl == null)
					throw new Exception(lnk.token.sourcePos() + ": function '" + lnk.name + "' not found");

				instr = (short) lbl.pc;
				break;
			}
			default:
				assert (false);
			}
			instrs.set(lnk.pc, instr);
		}
	}

	public String toHexString() {
		StringBuilder sb = new StringBuilder();
		int j = 0;
		int k = 0;
		final int n = instrs.size();
		for (int i = 0; i < n; i++) {
			short instr = instrs.get(i);
			sb.append(String.format("%04X", instr)).append(' ');
			j++;
			if (j > 3) {
				sb.append('\n');
				j = 0;
				k++;
				if (k > 3) {
					sb.append('\n');
					k = 0;
				}
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	// messy handling of comments
	private void appendComments(StringBuilder sb, int i) {
		ArrayList<String> cmnts = comments.get(i);
		if (cmnts == null)
			return;
		boolean lastCommentHadNewline = true;
		for (String s : cmnts) {
			if (s == null || s.length() == 0) // ? find out where an empty comment is inserted
				continue;
			if (lastCommentHadNewline) {
				sb.append("// ");
			}
			String s1 = s.replaceAll("\n", "\n// ");
			if (s.endsWith("\n")) {
				s1 = s1.substring(0, s1.length() - 3);
				lastCommentHadNewline = true;
			} else {
				lastCommentHadNewline = false;
			}
			sb.append(s1);
		}
		if (lastCommentHadNewline)
			return;
		sb.append('\n');
	}

	public String toAnnotatedHexString(boolean padTo64K) {
		StringBuilder sb = new StringBuilder();
		final int n = instrs.size();
		// ? messy handling of comments
		ensureCommentsArraySize();
		for (int i = 0; i < n; i++) {
			appendComments(sb, i);
			short instr = instrs.get(i);
			sb.append(String.format("%04X", instr));
			Statement stmt = stmts.get(i);
			if (stmt != null) {
				sb.append(" // ").append("[").append(i).append("] ").append(stmt.sourcePos()).append('\n');
			} else {
				sb.append("\n");
			}
		}
		// handle comments at end of file
		final int m = comments.size();
		for (int i = instrs.size(); i < m; i++) {
			appendComments(sb, i);
		}
		if (padTo64K) {
			sb.append("\n\n// padding: " + (0x10000 - n) + " words\n");
			for (int i = n; i < 0x10000; i++) {
				sb.append(Integer.toHexString(i)).append(' '); // ("0 ");
			}
		}
		sb.append('\n');
		return sb.toString();
	}
}