package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.*;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorHsql extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement>
{
    @Override
    public int getPriority()
    {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(final FindForeignKeyConstraintsStatement statement, final Database database)
    {
        return database instanceof HsqlDatabase;
    }

    public ValidationErrors validate(final FindForeignKeyConstraintsStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain)
    {
        final ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", statement.getBaseTableName());
        return validationErrors;
    }

    public Sql[] generateSql(final FindForeignKeyConstraintsStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain)
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ");
        sb.append("FKTABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("FKCOLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("PKTABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("PKCOLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
        sb.append("FK_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE ");
        sb.append("WHERE FKTABLE_NAME = '").append(statement.getBaseTableName().toUpperCase()).append("'");

        return new Sql[]
            {
                new UnparsedSql(sb.toString())
            };
    }
}
