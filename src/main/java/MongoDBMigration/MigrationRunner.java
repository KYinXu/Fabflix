package MongoDBMigration;

import config.MongoDBConnectionConfig;
import MongoDBMigration.config.MySQLConnectionConfig;
import MongoDBMigration.migrators.*;

/**
 * Main entry point for MySQL to MongoDB migration
 * Orchestrates the migration process and coordinates all migrators
 */
public class MigrationRunner {
    
    // Track total migration time
    private long migrationStartTime;
    private long migrationEndTime;
    
    public static void main(String[] args) {
        MigrationRunner runner = new MigrationRunner();
        //runner.runPartialMigration(new String[]{"movies"}, 1000L);
        runner.fullMigration();
    }  
    
    public void fullMigration() {
        runFullMigration();
    }

    /**
     * Test MySQL connection and print counts of tables from createtable.sql
     */
    public void testSQLConnection() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MYSQL DATABASE TABLE COUNTS");
        System.out.println("=".repeat(60) + "\n");
        
        try {
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            
            // Test connection
            System.out.println("Connecting to MySQL database...");
            java.sql.Connection conn = mysqlConfig.getConnection();
            System.out.println("✓ Connected successfully!\n");
            
            // Tables from createtable.sql
            String[] tables = {
                "movies",
                "stars",
                "stars_in_movies",
                "genres",
                "genres_in_movies",
                "customers",
                "sales",
                "credit_cards",
                "ratings"
            };
            
            System.out.println("Table Counts:");
            System.out.println("-".repeat(60));
            
            long totalRecords = 0;
            
            // Get count for each table
            for (String tableName : tables) {
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName)) {
                    
                    if (rs.next()) {
                        long count = rs.getLong("count");
                        totalRecords += count;
                        
                        System.out.printf("  %-30s %,10d rows\n", tableName, count);
                    }
                } catch (Exception e) {
                    System.out.printf("  %-30s ERROR: %s\n", tableName, e.getMessage());
                }
            }
            
            System.out.println("-".repeat(60));
            System.out.printf("  %-30s %,10d\n", "TOTAL TABLES:", tables.length);
            System.out.printf("  %-30s %,10d\n", "TOTAL RECORDS:", totalRecords);
            System.out.println("-".repeat(60));
            
            // Close connection
            mysqlConfig.closeConnection(conn);
            
            System.out.println("\n✅ MySQL connection test: SUCCESS\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ MySQL connection test: FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run MongoDB connection test with example documents
     */
    public void testMongoConnection() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX DATABASE CONNECTION TEST");
        System.out.println("=".repeat(60) + "\n");
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create test migrator
            MovieMigrator movieMigrator = new MovieMigrator(mysqlConfig, mongoConfig);
            
            // Run connection tests
            movieMigrator.runConnectionTest();
            
            // Push example documents
            movieMigrator.migrate();

            System.out.println("\n✅ All connection tests passed!\n");
            System.out.println("📊 Example documents are in collection: " + movieMigrator.getCollectionName());
            System.out.println("   You can view them in MongoDB Compass or mongosh\n");
            System.out.println("You can now run your migration scripts safely.\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Connection test failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Run full migration for all collections in the proper order
     */
    public void runFullMigration() {
        String[] allCollections = {
            "genres",      // First: reference data
            "movies",      // Second: main entities with genre references
            "stars",       // Third: stars (can be independent)
            "customers",   // Fourth: customers
            "sales"        // Last: transactional data with references
        };
        runPartialMigration(allCollections, null, true);
    }
    
    /**
     * Run migration for specific collections
     * @param collections Array of collection names to migrate (e.g., "movies", "stars", etc.)
     */
    public void runPartialMigration(String[] collections) {
        runPartialMigration(collections, null, false);
    }
    
    /**
     * Run migration for specific collections with a record limit
     * @param collections Array of collection names to migrate (e.g., "movies", "stars", etc.)
     * @param limit Maximum number of records to migrate per collection (null for no limit)
     */
    public void runPartialMigration(String[] collections, Long limit) {
        runPartialMigration(collections, limit, false);
    }
    
    /**
     * Run migration for specific collections with a record limit
     * @param collections Array of collection names to migrate (e.g., "movies", "stars", etc.)
     * @param limit Maximum number of records to migrate per collection (null for no limit)
     * @param isFullMigration Whether this is a full migration (affects console output)
     */
    private void runPartialMigration(String[] collections, Long limit, boolean isFullMigration) {
        if (collections == null || collections.length == 0) {
            System.out.println("No collections specified for migration.");
            return;
        }
        
        // Start timer
        migrationStartTime = System.currentTimeMillis();
        
        System.out.println("\n" + "=".repeat(60));
        if (isFullMigration) {
            System.out.println("  FULL MIGRATION");
        } else {
            System.out.println("  PARTIAL MIGRATION");
        }
        System.out.println("=".repeat(60) + "\n");
        System.out.println("Collections to migrate: " + String.join(", ", collections));
        if (limit != null) {
            System.out.println("Record limit per collection: " + limit);
        }
        System.out.println("Migration started at: " + new java.util.Date(migrationStartTime));
        System.out.println();
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Process each collection
            for (String collection : collections) {
                try {
                    switch (collection.toLowerCase()) {
                        case "movies":
                            MovieMigrator movieMigrator = new MovieMigrator(mysqlConfig, mongoConfig);
                            if (limit != null) {
                                movieMigrator.setMigrationLimit(limit);
                            }
                            movieMigrator.migrate();
                            break;
                            
                        case "stars":
                            StarMigrator starMigrator = new StarMigrator(mysqlConfig, mongoConfig);
                            if (limit != null) {
                                starMigrator.setMigrationLimit(limit);
                            }
                            starMigrator.migrate();
                            break;
                            
                        case "genres":
                            GenreMigrator genreMigrator = new GenreMigrator(mysqlConfig, mongoConfig);
                            if (limit != null) {
                                genreMigrator.setMigrationLimit(limit);
                            }
                            genreMigrator.migrate();
                            break;
                            
                        case "customers":
                            CustomerMigrator customerMigrator = new CustomerMigrator(mysqlConfig, mongoConfig);
                            if (limit != null) {
                                customerMigrator.setMigrationLimit(limit);
                            }
                            customerMigrator.migrate();
                            break;
                            
                        case "sales":
                            SalesMigrator salesMigrator = new SalesMigrator(mysqlConfig, mongoConfig);
                            if (limit != null) {
                                salesMigrator.setMigrationLimit(limit);
                            }
                            salesMigrator.migrate();
                            break;
                            
                        default:
                            System.out.println("⚠️  Unknown collection: " + collection + "\n");
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("✗ Failed to migrate " + collection);
                    System.err.println("  Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // End timer before verification/reporting
            migrationEndTime = System.currentTimeMillis();
            
            // Run verification after migration (accounting for limit)
            verifyMigration(collections, limit);
            
            // Generate summary report (accounting for limit)
            generateReport(collections, limit);
            
        } catch (Exception e) {
            System.err.println("\n❌ Partial migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verify all migrated collections
     */
    public void verifyMigration() {
        verifyMigration(new String[]{"movies", "stars", "genres", "customers", "sales"}, null);
    }
    
    /**
     * Verify specific collections after migration
     * @param collections Array of collection names to verify
     */
    public void verifyMigration(String[] collections) {
        verifyMigration(collections, null);
    }
    
    /**
     * Verify specific collections after migration with limit awareness
     * @param collections Array of collection names to verify
     * @param limit The migration limit that was used (null if full migration)
     */
    public void verifyMigration(String[] collections, Long limit) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATION VERIFICATION");
        System.out.println("=".repeat(60) + "\n");
        
        if (collections == null || collections.length == 0) {
            System.out.println("No collections specified for verification.");
            return;
        }
        
        if (limit != null) {
            System.out.println("Note: Partial migration with limit of " + limit + " records per collection");
            System.out.println("Verification will check if MongoDB count <= limit\n");
        }
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            int passedCount = 0;
            int failedCount = 0;
            int partialCount = 0;
            
            // Verify each collection
            for (String collection : collections) {
                try {
                    boolean valid = false;
                    long sourceCount = 0;
                    long destCount = 0;
                    
                    BaseMigrator migrator = null;
                    
                    switch (collection.toLowerCase()) {
                        case "movies":
                            migrator = new MovieMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "stars":
                            migrator = new StarMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "genres":
                            migrator = new GenreMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "customers":
                            migrator = new CustomerMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "sales":
                            migrator = new SalesMigrator(mysqlConfig, mongoConfig);
                            break;
                        default:
                            System.out.println(collection + ": ⚠️  Unknown collection - skipping");
                            continue;
                    }
                    
                    // Get counts and validate
                    sourceCount = migrator.getSourceCount();
                    destCount = migrator.getDestinationCount();
                    
                    if (limit != null) {
                        // For partial migration, verify count is correct relative to limit
                        long expectedCount = Math.min(limit, sourceCount);
                        valid = (destCount == expectedCount);
                        
                        System.out.println("Validating " + collection + " (partial migration):");
                        System.out.println("  MySQL total:       " + sourceCount);
                        System.out.println("  Migration limit:   " + limit);
                        System.out.println("  Expected in Mongo: " + expectedCount);
                        System.out.println("  Actual in Mongo:   " + destCount);
                        
                        if (valid) {
                            System.out.println("✓ Partial migration validated: " + destCount + " records migrated");
                            partialCount++;
                        } else {
                            System.out.println("✗ Count mismatch: expected " + expectedCount + ", got " + destCount);
                        }
                    } else {
                        // For full migration, verify counts match exactly
                        valid = migrator.validate();
                    }
                    
                    if (valid) {
                        passedCount++;
                    } else {
                        failedCount++;
                    }
                    
                } catch (Exception e) {
                    System.err.println("✗ Failed to verify " + collection);
                    System.err.println("  Error: " + e.getMessage());
                    failedCount++;
                }
                
                System.out.println(); // Spacing between collections
            }
            
            // Summary
            System.out.println("=".repeat(60));
            System.out.println("  VERIFICATION SUMMARY");
            System.out.println("=".repeat(60));
            System.out.println("  Collections checked: " + (passedCount + failedCount));
            System.out.println("  Passed:              " + passedCount);
            if (limit != null && partialCount > 0) {
                System.out.println("  Partial migrations:  " + partialCount);
            }
            System.out.println("  Failed:              " + failedCount);
            if (migrationStartTime > 0 && migrationEndTime > 0) {
                System.out.println("  Migration time:      " + formatDuration(migrationEndTime - migrationStartTime));
            }
            System.out.println("=".repeat(60) + "\n");
            
            if (failedCount == 0 && passedCount > 0) {
                if (limit != null) {
                    System.out.println("✅ Partial migration verification passed!\n");
                } else {
                    System.out.println("✅ All verifications passed!\n");
                }
            } else if (failedCount > 0) {
                System.out.println("⚠️  Some verifications failed. Please check the logs above.\n");
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ Verification failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate migration report for all collections
     */
    public void generateReport() {
        generateReport(new String[]{"movies", "stars", "genres", "customers", "sales"}, null);
    }
    
    /**
     * Generate detailed migration report for specific collections
     * @param collections Array of collection names to report on
     */
    public void generateReport(String[] collections) {
        generateReport(collections, null);
    }
    
    /**
     * Generate detailed migration report for specific collections with limit awareness
     * @param collections Array of collection names to report on
     * @param limit The migration limit that was used (null if full migration)
     */
    public void generateReport(String[] collections, Long limit) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATION REPORT");
        if (limit != null) {
            System.out.println("  (Partial Migration - Limit: " + limit + " per collection)");
        }
        System.out.println("=".repeat(60) + "\n");
        
        if (collections == null || collections.length == 0) {
            System.out.println("No collections specified for report.");
            return;
        }
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            long totalSourceRecords = 0;
            long totalDestRecords = 0;
            int migratedCollections = 0;
            
            System.out.println("Collection Statistics:");
            System.out.println("-".repeat(60));
            System.out.printf("  %-20s %15s %15s %10s\n", "Collection", "MySQL", "MongoDB", "Status");
            System.out.println("-".repeat(60));
            
            // Get stats for each collection
            for (String collection : collections) {
                try {
                    long sourceCount = 0;
                    long destCount = 0;
                    String status = "N/A";
                    
                    BaseMigrator migrator = null;
                    
                    switch (collection.toLowerCase()) {
                        case "movies":
                            migrator = new MovieMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "stars":
                            migrator = new StarMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "genres":
                            migrator = new GenreMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "customers":
                            migrator = new CustomerMigrator(mysqlConfig, mongoConfig);
                            break;
                        case "sales":
                            migrator = new SalesMigrator(mysqlConfig, mongoConfig);
                            break;
                        default:
                            status = "???";
                            break;
                    }
                    
                    if (migrator != null) {
                        sourceCount = migrator.getSourceCount();
                        destCount = migrator.getDestinationCount();
                        
                        if (limit != null) {
                            // For partial migration, check if dest matches expected limit
                            long expectedCount = Math.min(limit, sourceCount);
                            status = (destCount == expectedCount && destCount > 0) ? "✓" : "✗";
                        } else {
                            // For full migration, check if counts match exactly
                            status = (sourceCount == destCount && destCount > 0) ? "✓" : "✗";
                        }
                        
                        if (destCount > 0) {
                            migratedCollections++;
                            totalSourceRecords += sourceCount;
                            totalDestRecords += destCount;
                        }
                    }
                    
                    System.out.printf("  %-20s %,15d %,15d %10s\n", 
                        collection, sourceCount, destCount, status);
                    
                } catch (Exception e) {
                    System.out.printf("  %-20s %15s %15s %10s\n", 
                        collection, "ERROR", "ERROR", "✗");
                }
            }
            
            System.out.println("-".repeat(60));
            System.out.printf("  %-20s %,15d %,15d\n", "TOTALS:", totalSourceRecords, totalDestRecords);
            System.out.println("-".repeat(60));
            
            // Summary
            System.out.println("\nSummary:");
            System.out.println("  Collections with data: " + migratedCollections);
            System.out.println("  Total MySQL records:   " + String.format("%,d", totalSourceRecords));
            System.out.println("  Total MongoDB docs:    " + String.format("%,d", totalDestRecords));
            if (migrationStartTime > 0 && migrationEndTime > 0) {
                long durationMs = migrationEndTime - migrationStartTime;
                System.out.println("  Total migration time:  " + formatDuration(durationMs));
                
                // Calculate throughput
                if (durationMs > 0 && totalDestRecords > 0) {
                    double recordsPerSecond = (totalDestRecords * 1000.0) / durationMs;
                    System.out.println("  Average throughput:    " + String.format("%,.0f", recordsPerSecond) + " docs/sec");
                }
            }
            
            if (limit != null) {
                // Calculate expected records for partial migration
                long expectedTotal = 0;
                for (String collection : collections) {
                    try {
                        long count = 0;
                        BaseMigrator m = null;
                        
                        switch (collection.toLowerCase()) {
                            case "movies":
                                m = new MovieMigrator(mysqlConfig, mongoConfig);
                                break;
                            case "stars":
                                m = new StarMigrator(mysqlConfig, mongoConfig);
                                break;
                            case "genres":
                                m = new GenreMigrator(mysqlConfig, mongoConfig);
                                break;
                            case "customers":
                                m = new CustomerMigrator(mysqlConfig, mongoConfig);
                                break;
                            case "sales":
                                m = new SalesMigrator(mysqlConfig, mongoConfig);
                                break;
                        }
                        
                        if (m != null) {
                            count = m.getSourceCount();
                            expectedTotal += Math.min(limit, count);
                        }
                    } catch (Exception e) {
                        // Skip
                    }
                }
                
                System.out.println("  Expected (with limit): " + String.format("%,d", expectedTotal));
                
                if (expectedTotal == totalDestRecords && totalDestRecords > 0) {
                    System.out.println("  Migration status:      Complete (within limit)");
                    System.out.println("\n✅ Partial migration completed successfully!");
                } else if (totalDestRecords > 0) {
                    double percentage = (totalDestRecords * 100.0) / expectedTotal;
                    System.out.println("  Migration progress:    " + String.format("%.1f%%", percentage));
                    System.out.println("\n⚠️  Partial migration incomplete or has discrepancies");
                } else {
                    System.out.println("  Migration progress:    0.0%");
                    System.out.println("\n⚠️  No data has been migrated yet");
                }
            } else {
                // Full migration reporting
                if (totalSourceRecords == totalDestRecords && totalDestRecords > 0) {
                    double percentage = 100.0;
                    System.out.println("  Migration progress:    " + String.format("%.1f%%", percentage));
                    System.out.println("\n✅ Migration complete and verified!");
                } else if (totalDestRecords > 0) {
                    double percentage = (totalDestRecords * 100.0) / totalSourceRecords;
                    System.out.println("  Migration progress:    " + String.format("%.1f%%", percentage));
                    System.out.println("\n⚠️  Migration incomplete or has discrepancies");
                } else {
                    System.out.println("  Migration progress:    0.0%");
                    System.out.println("\n⚠️  No data has been migrated yet");
                }
            }
            
            System.out.println("\n" + "=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Report generation failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Format duration in milliseconds to human-readable format
     * @param durationMs Duration in milliseconds
     * @return Formatted string (e.g., "1h 23m 45s" or "23m 45s" or "45.2s")
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 0) {
            return "N/A";
        }
        
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else if (seconds > 0) {
            double secs = durationMs / 1000.0;
            return String.format("%.1fs", secs);
        } else {
            return durationMs + "ms";
        }
    }
}

