// reviewed: 2024-08-05
//           2025-04-28
package db;

/** Index of relation column. */
public class IndexRel extends Index {

    /** Relation column. */
    private final DbRelation rel;

    public IndexRel(final DbRelation r) {
        rel = r;
    }

    @Override
    protected void init(final DbClass c) {
        if (rel.relFld == null) {
            throw new RuntimeException("Relation " + rel.name + " in class " + cls.getName()
                    + " can not be indexed. Is relation type RefN?");
        }
        if (!rel.relFld.cls.equals(cls)) {
            throw new RuntimeException("Relation " + rel.name + " in class " + cls.getName()
                    + " can not be indexed because the relation creates the column in a different table. Is relation type AggN? In that case the column is already indexed.");
        }
        fields.add(rel.relFld);
    }

}
