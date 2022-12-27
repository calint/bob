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

			final String authors = ls.get(2);
			if (authors.length() > Book.authors.getSize())
				throw new RuntimeException("record " + i + " has size of authors " + authors.length()
						+ " but field length is " + Book.authors.getSize());

			final String publisher = ls.get(5);
			if (publisher.length() > Book.publisher.getSize())
				throw new RuntimeException("record " + i + " has size of publisher " + publisher.length()
						+ " but field length is " + Book.publisher.getSize());

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
				}
				if (authorsSb.length() > 1) { // remove last delimiter
					authorsSb.setLength(authorsSb.length() - 1);
				}
				o.setAuthors(authorsSb.toString());
			} else {
				o.setAuthors("");
			}
			o.setPublisher(ls.get(5));
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
					final List<DbObject> cls = tn.get(BookCategory.class, new Query(BookCategory.name, Query.EQ, s),
							null, null);
					final BookCategory bc;
					if (cls.isEmpty()) {
						bc = (BookCategory) tn.create(BookCategory.class);
						bc.setName(s);
					} else {
						bc = (BookCategory) cls.get(0);
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
			sb.append(o.getName()).append(" ").append(o.getAuthors()).append(" ").append(o.getPublisher());
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