// reviewed: 2024-08-05
package b;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/** A filesystem path. */
public final class path implements Serializable {
	static final long serialVersionUID = 1;

	private final File file;

	path(final File f) {
		file = f;
		final String name = f.getName();
		if (name.contains("..")) {
			throw new Error("illegal name: " + name);
		}
		if (b.firewall_paths_on) {
			b.firewall_ensure_path_access(uri());
		}
	}

	/** called by b.b */
	path(final File f, final boolean ommit_checks) {
		file = f;
	}

	public InputStream inputstream() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public FileInputStream fileinputstream() throws IOException {
		return new FileInputStream(file);
	}

	public FileOutputStream outputstream(final boolean append) throws IOException {
		if (!exists()) {
			mkbasedir();
		}
		return new FileOutputStream(file, append);
	}

	public FileOutputStream outputstream() throws IOException {
		return outputstream(false);
	}

	public Reader reader() throws IOException {
		return new InputStreamReader(inputstream(), b.strenc);
	}

	public Writer writer(final boolean append) throws IOException {
		return new OutputStreamWriter(outputstream(append), b.strenc);
	}

	public boolean exists() {
		return file.exists();
	}

	public long lastmod() {
		return file.lastModified();
	}

	public long size() {
		return file.length();
	}

	public boolean isfile() {
		return file.isFile();
	}

	public boolean isdir() {
		if (!exists()) {
			return false;
		}
		return file.isDirectory();
	}

	public String fullpath() {
		try {
			return file.getCanonicalPath();
		} catch (final IOException e) {
			throw new Error(e);
		}
	}

	public path get(final String name) {
		return new path(new File(file, name));
	}

	public String name() {
		return file.getName();
	}

	public String[] list() {
		final String[] f = file.list();
		if (f == null) {
			return new String[0];
		}
		return f;
	}

	public String[] list(final FilenameFilter fnmf) {
		final String[] f = file.list(fnmf);
		if (f == null) {
			return new String[0];
		}
		return f;
	}

	public boolean rename(final path pth) {
		return file.renameTo(pth.file);
	}

	public boolean rename(final String new_name) {
		return file.renameTo(new File(file.getParent(), new_name));
	}

	public void lastmod(final long lastmod) {
		if (!file.setLastModified(lastmod)) {
			throw new Error();
		}
	}

	public void setreadonly() {
		if (!file.setReadOnly()) {
			throw new Error();
		}
	}

	public boolean ishidden() {
		return file.getName().charAt(0) == '.';
	}

	public path parent() {
		final File f = file.getParentFile();
		return f == null ? null : new path(f);
	}

	public void to(final xwriter x) throws Throwable {
		to(x.outputstream());
	}

	public FileChannel filechannel() throws IOException {
		return outputstream(false).getChannel();
	}

	@Override
	public int hashCode() {
		return file.toString().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof path)) {
			return false;
		}
		return ((path) obj).file.equals(file);
	}

	@Override
	public String toString() {
		final String fn = file.toString();
		if (fn.startsWith("./")) {
			return fn.substring("./".length());
		}
		return fn;
	}

	public path mkfile() throws IOException {
		if (!file.createNewFile()) {
			throw new IOException("cannot make file " + file);
		}
		return this;
	}

	public boolean rm() {
		return rm(null);
	}

	public boolean rm(final sts st) {
		if (!file.exists()) {
			return true;
		}
		if (st != null) {
			try {
				st.sts_set("deleteting " + file.toString());
			} catch (final Throwable t) {
				throw new Error(t);
			}
		}
		if (file.isFile()) {
			return file.delete();
		}
		for (final File f : file.listFiles()) {
			if (!new path(f).rm(st)) {
				return false;
			}
		}
		return file.delete();
	}

	public void append(final String line, final String eol) throws IOException {
		if (!file.exists() && !file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
			throw new Error();
		}
		final byte[] ba = b.tobytes(line);
		final OutputStream os = outputstream(true);
		try {
			os.write(ba);
			if (eol != null) {
				os.write(b.tobytes(eol));
			}
		} finally {
			os.close();
		}
	}

	public void append(final String line) throws IOException {
		append(line, null);
	}

	public void append(final String[] lines, final String eol) throws IOException {
		if (!file.exists() && !file.getParentFile().mkdirs()) {
			throw new Error();
		}
		final OutputStream os = outputstream(true);
		try {
			final byte[] eosba = eol != null ? b.tobytes(eol) : null;
			for (final String line : lines) {
				os.write(b.tobytes(line));
				if (eol != null) {
					// ? move outside and make 2 loops
					os.write(eosba);
				}
			}
		} finally {
			os.close();
		}
	}

	public path to(final OutputStream os) throws Throwable {
		final InputStream is = inputstream();
		try {
			b.cp(is, os, null);
		} finally {
			is.close();
		}
		return this;
	}

	public void mkdirs() throws IOException {
		if (file.exists() && file.isDirectory()) {
			return;
		}
		if (!file.mkdirs()) {
			throw new IOException("cannot make dir " + file);
		}
	}

	public void mkbasedir() throws IOException {
		final File pf = file.getParentFile();
		if (pf != null && pf.isDirectory()) {
			return;
		}
		if (pf == null) {
			throw new Error();
		}
		if (!pf.mkdirs()) {
			throw new IOException("cannot make basedir for " + file);
		}
	}

	public path to(final ByteBuffer bb) throws IOException {
		final FileInputStream fis = fileinputstream();
		final FileChannel channelFrom = fis.getChannel();
		try {
			channelFrom.read(bb);
		} finally {
			channelFrom.close();
			fis.close();
		}
		return this;
	}

	public String type() {
		final String fn = file.getName();
		final int ix = fn.lastIndexOf('.');
		if (ix == -1) {
			return "";
		}
		return fn.substring(ix + 1).toLowerCase();
	}

	public String uri() {
		return b.file_to_uri(file);
	}

	public Object readobj() throws IOException, ClassNotFoundException {
		final ObjectInputStream ois = new ObjectInputStream(inputstream());
		try {
			return ois.readObject();
		} finally {
			ois.close();
		}
	}

	public void writeobj(final Object o) throws IOException {
		final ObjectOutputStream oos = new ObjectOutputStream(outputstream(false));
		try {
			oos.writeObject(o);
		} finally {
			oos.close();
		}
	}

	public path writeba(final byte[] data) throws IOException {
		return writeba(data, 0, data.length);
	}

	public path writeba(final byte[] data, final int offset, final int count) throws IOException {
		final OutputStream os = outputstream(false);
		try {
			os.write(data, offset, count);
		} finally {
			os.close();
		}
		return this;
	}

	public path writebb(final ByteBuffer byteBuffer) throws IOException {
		final FileOutputStream os = outputstream(false);
		try {
			final FileChannel fc = os.getChannel();
			fc.write(byteBuffer);
			if (byteBuffer.hasRemaining()) {
				throw new Error("incompletewrite");
			}
		} finally {
			os.close();
		}
		return this;
	}

	public String readstr() throws IOException {
		if (!isfile()) {
			return "";
		}
		final ByteBuffer bb = ByteBuffer.allocate((int) size());
		to(bb);
		if (bb.hasRemaining()) {
			throw new Error("buffernotfullyread");
		}
		bb.flip();
		return new String(bb.array(), bb.position(), bb.limit(), b.strenc);
	}

	public boolean isin(final path p) {
		try {
			return fullpath().startsWith(p.fullpath());
		} catch (final Throwable t) {
			throw new Error(t);
		}
	}

	public boolean moveto(final path p) {
		return rename(p);
	}

	public void copyto(final path dir) throws Throwable {
		final path p = dir.get(name());
		if (p.exists()) {
			throw new Error("exists. overwrite?");
		}
		final OutputStream os = p.outputstream();
		try {
			to(os);
		} finally {
			os.close();
		}
	}

	public path writestr(final String s) throws IOException {
		writeba(b.tobytes(s));
		return this;
	}

	public interface visitor {
		/** @return true to break */
		boolean visit(final path p) throws Throwable;
	}

	public void apply(final visitor v) throws Throwable {
		if (!exists()) {
			return;
		}
		if (isfile()) {
			v.visit(this);
		}
		if (isdir()) {
			for (final String fn : file.list()) {
				get(fn).apply(v);
			}
		}
	}

	public ByteBuffer readbb() throws IOException {
		final long size = size();
		if (size > Integer.MAX_VALUE) {
			throw new Error("filesizetolarge " + size);
		}
		final ByteBuffer bb = ByteBuffer.allocate((int) size);
		to(bb);
		bb.flip();
		return bb;
	}

	public void foreach(final visitor v) throws Throwable {
		if (!isdir()) {
			return;
		}
		// android compatible
		for (final String s : list()) {
			final path pth = get(s);
			if (v.visit(pth)) {
				break;
			}
		}
	}
}
