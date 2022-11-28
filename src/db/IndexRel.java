package db;

/** Index of relation column. */
public class IndexRel extends Index {
	final private DbRelation rel;

	public IndexRel(final DbRelation r) {
		rel = r;
	}

	@Override
	void init(final DbClass c) {
		if (rel.relFld == null)
			throw new RuntimeException("Relation " + rel.name + " in class " + cls.getName()
					+ " can not be indexed. Is relation type RefN?");
		if (!rel.relFld.cls.equals(cls)) {
			throw new RuntimeException("Relation " + rel.name + " in class " + cls.getName()
					+ " can not be indexed because the relation creates the column in a different table. Is relation type AggN? In that case the column is already indexed.");
		}
		fields.add(rel.relFld);
	}
}
