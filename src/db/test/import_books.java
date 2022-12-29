package db.test;

import java.io.FileReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Query;
import imp.CsvReader;
import imp.Util;

// import-200k-book
//   download csv at https://www.kaggle.com/datasets/mohamedbakhet/amazon-books-reviews
public class import_books extends TestCase {
	final private String filePath;

	public import_books() {
		this("csv/books_data_sample.csv");
	}

	public import_books(final String path) {
		filePath = path;
	}

	@Override
	public String getTestName() {
		return getClass().getName() + " " + filePath;
	}

	private final SimpleDateFormat year = new SimpleDateFormat("yyyy");
	private final SimpleDateFormat yearMonth = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();

		// sanity check
		FileReader in = new FileReader(filePath);
		CsvReader csv = new CsvReader(in);
		List<String> ls = csv.nextRecord();// read headers
		int i = 2; // skip headers
		out.println("\nbounds check file '" + filePath + "'");
		while (true) {
			ls = csv.nextRecord();
			if (ls == null) {
				break;
			}
			final String name = ls.get(0);
			if (name.length() > Book.name.getSize())
				throw new RuntimeException("record " + i + " has size of name " + name.length()
						+ " but field length is " + Book.name.getSize());

			final String authorsStr = ls.get(2);
			if (authorsStr.length() > Book.authorsStr.getSize())
				throw new RuntimeException("record " + i + " has size of authors " + authorsStr.length()
						+ " but field length is " + Book.authorsStr.getSize());

			final String publisherStr = ls.get(5);
			if (publisherStr.length() > Book.publisherStr.getSize())
				throw new RuntimeException("record " + i + " has size of publisher " + publisherStr.length()
						+ " but field length is " + Book.publisherStr.getSize());

			final String categoriesStr = ls.get(8);
			if (categoriesStr.length() > Book.categoriesStr.getSize())
				throw new RuntimeException("record " + i + " has size of category " + categoriesStr.length()
						+ " but field length is " + Book.categoriesStr.getSize());

			if (++i % 100 == 0) {
				out.println("  " + i);
			}
		}
		in.close();
		out.println("bounds check done. importing " + (i - 2) + " books from " + filePath + "'");
		in = new FileReader(filePath);
		csv = new CsvReader(in);
		ls = csv.nextRecord();// read headers
		i = 2; // skip headers
		final StringBuilder sb = new StringBuilder(1000);
		while (true) {
			ls = csv.nextRecord();
			if (ls == null) {
				break;
			}
			final Book o = (Book) tn.create(Book.class);
			o.setName(ls.get(0));
			final String authors = ls.get(2);
			if (authors.length() != 0) {
				final List<String> authorsList = Util.readList(new StringReader(authors), ',', '\'');
				final StringBuilder authorsSb = new StringBuilder(128);
				for (final String s : authorsList) {
					authorsSb.append(s).append(';');
					final List<DbObject> lsa = tn.get(Author.class, new Query(Author.name, Query.EQ, s), null, null);
					final Author a;
					if (lsa.isEmpty()) {
						a = (Author) tn.create(Author.class);
						a.setName(s);
					} else {
						a = (Author) lsa.get(0);
					}
					o.addAuthor(a);
				}
				if (authorsSb.length() > 1) { // remove last delimiter
					authorsSb.setLength(authorsSb.length() - 1);
				}
				o.setAuthorsStr(authorsSb.toString());
			} else {
				o.setAuthorsStr("");
			}
			final String publisherStr = ls.get(5);
			o.setPublisherStr(publisherStr);
			if (publisherStr.length() > 0) {
				final List<DbObject> res = tn.get(Publisher.class, new Query(Publisher.name, Query.EQ, publisherStr),
						null, null);
				final Publisher p;
				if (res.isEmpty()) {
					p = (Publisher) tn.create(Publisher.class);
					p.setName(publisherStr);
				} else {
					p = (Publisher) res.get(0);
				}
				o.setPublisher(p);
			}
			final String pd = ls.get(6);
			if (pd.length() != 0) {
				final Timestamp ts = parseDate(pd);
				o.setPublishedDate(ts);
			}
			final String categoriesStr = ls.get(8);
			if (categoriesStr.length() != 0) {
				final List<String> categoriesList = Util.readList(new StringReader(categoriesStr), ',', '\'');
				final StringBuilder categoriesSb = new StringBuilder(128);
				for (final String s : categoriesList) {
					categoriesSb.append(s).append(';');
					final List<DbObject> cls = tn.get(Category.class, new Query(Category.name, Query.EQ, s), null,
							null);
					final Category bc;
					if (cls.isEmpty()) {
						bc = (Category) tn.create(Category.class);
						bc.setName(s);
					} else {
						bc = (Category) cls.get(0);
					}
					o.addCategory(bc);
				}
				if (categoriesSb.length() > 1) { // remove last delimiter
					categoriesSb.setLength(categoriesSb.length() - 1);
				}
				o.setCategoriesStr(categoriesSb.toString());
			} else {
				o.setCategoriesStr("");
			}

			final DataText d = o.getData(true);
			d.setData(ls.get(1));

			sb.setLength(0);
			sb.append(o.getName()).append(" ").append(o.getAuthors()).append(" ").append(o.getPublisherStr());
			d.setMeta(sb.toString());

			if (++i % 100 == 0) {
				out.println("  " + i);
				tn.commit();
			}
		}
		in.close();
		out.println("import done. " + (i - 2) + " books imported.");
	}

	private Timestamp parseDate(final String d) {
		try {
			final Date dt = yearMonthDay.parse(d);
			return new Timestamp(dt.getTime());
		} catch (final ParseException ok) {
		}
		try {
			final Date dt = yearMonth.parse(d);
			return new Timestamp(dt.getTime());
		} catch (final ParseException ok) {
		}
		try {
			final Date dt = year.parse(d);
			return new Timestamp(dt.getTime());
		} catch (final ParseException ok) {
		}
		return null;
	}
}