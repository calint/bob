package bob;

import java.util.ArrayList;
import java.util.List;
import b.a;

public class data{
	public final static List<String> ls=new ArrayList<String>();
	static{
		ls.add("file1.txt");
		ls.add("file+2.txt");
		ls.add("file"+a.id_path_separator+"2.txt");
		ls.add("another file.txt");
	}
}
