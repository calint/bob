package db.test;

import java.io.FileReader;
import java.util.List;

import db.Db;
import db.DbTransaction;
import imp.CsvReader;

// import games
public class import_games extends TestCase {

    private final String filePath;

    public import_games() {
        this("csv/steam_games_sample.csv");
    }

    public import_games(final String path) {
        filePath = path;
    }

    @Override
    public String getTestName() {
        return getClass().getName() + " " + filePath;
    }

    @Override
    public void doRun() throws Throwable {
        final DbTransaction tn = Db.currentTransaction();

        Db.log("importing " + filePath);
        final FileReader in = new FileReader(filePath);
        final CsvReader csv = new CsvReader(in, ';', '"');
        List<String> ls = csv.nextRecord(); // skip headers
        int i = 2;
        while (true) {
            ls = csv.nextRecord();
            if (ls == null) {
                break;
            }

            final Game o = (Game) tn.create(Game.class);
            o.setName(ls.get(1));
            o.setDescription(ls.get(2));

            if (++i % 100 == 0) {
                Db.log("  " + i);
                tn.commit();
            }
        }
        in.close();
        Db.log("done importing " + (i - 2) + " records from " + filePath);
    }
}