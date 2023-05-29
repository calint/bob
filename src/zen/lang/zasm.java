package zen.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class zasm {
	private static ArrayList<Statement> stmts = new ArrayList<Statement>();

	public static void main(String[] args) throws Throwable {
		String srcPathStr = args.length == 0 ? "rom.zasm" : args[0];
		File srcPath = new File(srcPathStr);
		if (!srcPath.exists()) {
			System.out.println("source file '" + srcPath + "' not found");
			return;
		}
		boolean padTo64K = false;

		System.out.println("compiling: " + srcPath);
		String srcIn = readFileToString(srcPath.toString());

		CompilationResult cr = compile(srcIn, padTo64K);

		String compiledPathStr = srcPathStr.substring(0, srcPathStr.lastIndexOf('.')) + ".mem";
		writeStringToFile(compiledPathStr, cr.output);
		System.out.println("    wrote: " + compiledPathStr);
		System.out.println("     size: " + cr.size);
	}

	private static class CompilationResult {
		String output;
		int size;
	}

	private static CompilationResult compile(String srcIn, boolean padTo64K) throws Throwable {
		Tokenizer tz = new Tokenizer(srcIn);
		StringBuilder srcOut = new StringBuilder();
		while (true) {
			Token zn = tz.nextToken();
			if (zn.id().endsWith(":")) {
				stmts.add(new StmtLabel(zn, tz));
				continue;
			}
			if (zn.isId("#")) {
				stmts.add(new StmtComment(zn, tz));
				continue;
			}
			if (zn.isId("@")) {
				stmts.add(new StmtFwdTo(zn, tz));
				continue;
			}
			if (zn.isId("endfunc")) {
				stmts.add(new StmtEndFunc(zn));
				continue;
			}
			Token id = null;
			if (zn.isId("ifz") || zn.isId("ifn") || zn.isId("ifp")) {
				id = tz.nextToken();
				if (id.isEmpty())
					throw new Exception(id.sourcePos() + ": unexpected end of file");
			}
			if (zn.isEmpty()) {
				stmts.add(new StmtEof(zn));
				break;
			}
			if (id == null) {
				id = zn;
				zn = null;
			}
			if (id.isId("ldi")) {
				stmts.add(new OpLdi(zn, id, tz));
			} else if (id.isId("st")) {
				stmts.add(new OpSt(zn, id, tz));
			} else if (id.isId("ld")) {
				stmts.add(new OpLd(zn, id, tz));
			} else if (id.isId("addi")) {
				stmts.add(new OpAddi(zn, id, tz));
			} else if (id.isId("add")) {
				stmts.add(new OpAdd(zn, id, tz));
			} else if (id.isId("sub")) {
				stmts.add(new OpSub(zn, id, tz));
			} else if (id.isId("or")) {
				stmts.add(new OpOr(zn, id, tz));
			} else if (id.isId("xor")) {
				stmts.add(new OpXor(zn, id, tz));
			} else if (id.isId("and")) {
				stmts.add(new OpAnd(zn, id, tz));
			} else if (id.isId("not")) {
				stmts.add(new OpNot(zn, id, tz));
			} else if (id.isId("cp")) {
				stmts.add(new OpCp(zn, id, tz));
			} else if (id.isId("shf")) {
				stmts.add(new OpShf(zn, id, tz));
			} else if (id.isId("jmp")) {
				stmts.add(new OpJmp(zn, id, tz));
			} else if (id.isId("call")) {
				stmts.add(new OpCall(zn, id, tz));
			} else if (id.isId("wl")) {
				stmts.add(new OpWl(zn, id, tz));
			} else if (id.isId("wh")) {
				stmts.add(new OpWh(zn, id, tz));
			} else if (id.isId("rl")) {
				stmts.add(new OpRl(zn, id, tz));
			} else if (id.isId("rh")) {
				stmts.add(new OpRh(zn, id, tz));
			} else if (id.isId("ledi")) {
				stmts.add(new OpLedi(zn, id, tz));
			} else if (id.isId("led")) {
				stmts.add(new OpLed(zn, id, tz));
			} else {
				stmts.add(new StmtData(id));
			}
		}

		Toc tc = new Toc();
		// ? messy handling of comments that are on the same line as the statement
		// example:
		// cp r1 r1 # comment statement is after the statement that needs compile
		int i = 0;
		final int n = stmts.size();
		while (true) {
			if (i == n)
				break;
			Statement st = stmts.get(i);
			String src = st.toSource();
			tc.addComment(src);
			srcOut.append(src);
			i++;
			if (i == n)
				break;
			if (!src.endsWith("\n")) {
				// handle comment on the same line as the statement
				Statement cmnt = stmts.get(i);
				if (cmnt instanceof StmtComment) {
					String s = cmnt.toSource();
					tc.addComment(s);
					srcOut.append(s);
					i++;
				}
			}
			st.compile(tc);
		}

		tc.link();

		String src = srcOut.toString();
		if (!src.toString().equals(srcIn)) {
			writeStringToFile("diff.zasm", src);
			System.out.println("!!! source and parsed source differ. See file 'diff'");
		}
		// String compiled = toc.toHexString();
		CompilationResult cr = new CompilationResult();
		cr.output = tc.toAnnotatedHexString(padTo64K); // false: don't pad to 64K
		cr.size = tc.getProgramCounter();
		return cr;
	}

	public static String readFileToString(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}
		reader.close();
		return sb.toString();
	}

	public static void writeStringToFile(String filePath, String fileContent) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write(fileContent);
		writer.close();
	}
}
