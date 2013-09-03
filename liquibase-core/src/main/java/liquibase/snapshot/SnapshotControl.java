package liquibase.snapshot;

import java.util.*;

import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class SnapshotControl {

    private Set<Class<? extends DatabaseObject>> types;

    public SnapshotControl(final Database database) {
        setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
    }

    @SafeVarargs
    public SnapshotControl(final Database database, final Class<? extends DatabaseObject>... types) {
        if (types == null || types.length == 0) {
            setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
        } else {
            setTypes(new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types)), database);
        }
    }

    public SnapshotControl(final Database database, final String types) {
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(types), database);
    }

    private void setTypes(final Set<Class<? extends DatabaseObject>> types, final Database database) {
        this.types = new HashSet<Class<? extends DatabaseObject>>();
        for (final Class<? extends DatabaseObject> type : types) {
            this.types.addAll(SnapshotGeneratorFactory.getInstance().getContainerTypes(type, database));
            this.types.add(type);
        }
    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldInclude(final Class<? extends DatabaseObject> type) {
        return type.equals(Catalog.class) || type.equals(Schema.class) || types.contains(type);
    }
}
