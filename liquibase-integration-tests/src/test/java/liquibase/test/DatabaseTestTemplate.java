package liquibase.test;

import java.util.Set;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.ExecutorService;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.lockservice.*;

import org.junit.ComparisonFailure;

public class DatabaseTestTemplate {
    public void testOnAvailableDatabases(final DatabaseTest test) throws Exception {
        test(test, DatabaseTestContext.getInstance().getAvailableDatabases());
    }

    public void testOnAllDatabases(final DatabaseTest test) throws Exception {
        test(test, TestContext.getInstance().getAllDatabases());
    }

    public void test(final DatabaseTest test, final Set<Database> databasesToTestOn) throws Exception {
        for (final Database database : databasesToTestOn) {
            if (database instanceof SQLiteDatabase) {
                continue; //todo: find how to get tests to run correctly on SQLite
            }
            final JdbcExecutor writeExecutor = new JdbcExecutor();
            writeExecutor.setDatabase(database);
            ExecutorService.getInstance().setExecutor(database, writeExecutor);
            final LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            lockService.reset();
            if (database.getConnection() != null) {
                lockService.forceReleaseLock();
            }

            try {
                test.performTest(database);
            } catch (final ComparisonFailure e) {
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                final ComparisonFailure newError = new ComparisonFailure(newMessage, e.getExpected(), e.getActual());
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (final AssertionError e) {
                e.printStackTrace();
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                final AssertionError newError = new AssertionError(newMessage);
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (final MigrationFailedException e) {
                e.printStackTrace();
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                final AssertionError newError = new AssertionError(newMessage);
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (final Exception e) {
                e.printStackTrace();
                String newMessage = "Database Test Exception on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }
                
                final Exception newError = e.getClass().getConstructor(String.class).newInstance(newMessage);
                if (e.getCause() == null) {
                    newError.setStackTrace(e.getStackTrace());
                } else {
                    newError.setStackTrace(e.getCause().getStackTrace());                    
                }
                throw newError;
            } finally {
                if (database.getConnection() != null && !database.getAutoCommitMode()) {
                    database.rollback();
                }
            }
        }
    }
}
