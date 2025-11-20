# MongoDB Migration Guide

## Overview
This guide explains the new MongoDB Document models and how to transition from the old SQL-based models while keeping MySQL operational.

## MongoDB Document Models (Production-Ready)

All MongoDB models are in the `models/` package with a **"Document"** suffix to distinguish them from MySQL models:

### 1. **MovieDocument.java**
- **Collection**: `movies`
- **Primary Key**: `String id` (movie ID like "tt1234567")
- **Embedded Documents**: 
  - `Rating` - contains score and voteCount
  - `Star[]` - array of stars with id, name, birthYear
  - `Genre[]` - array of genres with id and name
- **Use Case**: Main movie browsing, search, and detail pages

### 2. **StarDocument.java**
- **Collection**: `stars`
- **Primary Key**: `String id` (star ID)
- **Fields**: 
  - `List<String> movies` - array of movie IDs
  - `int movieCount` - denormalized count
- **Use Case**: Star detail pages, star search

### 3. **CustomerDocument.java**
- **Collection**: `customers`
- **Primary Key**: `ObjectId id` (MongoDB auto-generated)
- **Embedded Document**: `CreditCard` with id, firstName, lastName, expiration
- **Migration Field**: `Integer mysqlId` - keeps original MySQL ID during transition
- **Use Case**: User accounts, authentication, checkout

### 4. **EmployeeDocument.java**
- **Collection**: `employees`
- **Primary Key**: `String email`
- **New Field**: `String role` - for authorization ("admin", "staff", etc.)
- **Use Case**: Admin/staff authentication

### 5. **SaleDocument.java**
- **Collection**: `sales`
- **Primary Key**: `ObjectId id`
- **References**: 
  - `ObjectId customerId` - reference to customer
  - `String movieId` - reference to movie
- **Denormalized Fields** (for reporting):
  - `customerEmail`, `customerName`, `movieTitle`
- **Use Case**: Transaction history, sales reports

### 6. **GenreDocument.java**
- **Collection**: `genres`
- **Primary Key**: `Integer id` (keeps MySQL ID for compatibility)
- **New Field**: `int movieCount` - denormalized for performance
- **Use Case**: Genre listing, statistics

---

## Current Model Structure

### ✅ Old MySQL Models (KEEP - Still in Use)
These models work with your current MySQL/JDBC setup:

| File | Purpose | Status |
|------|---------|--------|
| `Star.java` | MySQL star DTO | ✅ **ACTIVE** - Keep for MySQL |
| `Genre.java` | MySQL genre DTO | ✅ **ACTIVE** - Keep for MySQL |
| `Rating.java` | MySQL rating DTO | ✅ **ACTIVE** - Keep for MySQL |
| `MovieList.java` | MySQL movie list DTO | ✅ **ACTIVE** - Keep for MySQL |
| `MovieResponse.java` | MySQL movie response DTO | ✅ **ACTIVE** - Keep for MySQL |

### ✅ New MongoDB Models (Ready to Use)
These models are for MongoDB operations:

| File | Purpose | Status |
|------|---------|--------|
| `MovieDocument.java` | MongoDB movie document | ✅ **READY** - Use for MongoDB |
| `StarDocument.java` | MongoDB star document | ✅ **READY** - Use for MongoDB |
| `GenreDocument.java` | MongoDB genre document | ✅ **READY** - Use for MongoDB |
| `CustomerDocument.java` | MongoDB customer document | ✅ **READY** - Use for MongoDB |
| `EmployeeDocument.java` | MongoDB employee document | ✅ **READY** - Use for MongoDB |
| `SaleDocument.java` | MongoDB sale document | ✅ **READY** - Use for MongoDB |

---

## Dual Database Strategy

During migration, you'll run **both databases simultaneously**:

```
┌─────────────────────────────────────────┐
│           Your Servlets                  │
└─────────────┬───────────────────────────┘
              │
              ├─────────────┬──────────────┐
              ▼             ▼              ▼
         ┌─────────┐  ┌──────────┐  ┌──────────┐
         │  MySQL  │  │ MongoDB  │  │  Mixed   │
         │ Servlet │  │ Servlet  │  │ Servlet  │
         └────┬────┘  └────┬─────┘  └─┬────┬───┘
              │            │           │    │
              ▼            ▼           ▼    ▼
         ┌─────────┐  ┌──────────┐  MySQL MongoDB
         │  MySQL  │  │ MongoDB  │
         │   DB    │  │    DB    │
         └─────────┘  └──────────┘
```

### Example: MySQL Servlet (Current)
```java
// Uses old models: Star.java, Genre.java, etc.
public class MovieServlet extends HttpServlet {
    private DataSource dataSource; // JDBC
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Connection conn = dataSource.getConnection();
        // SQL queries with MovieList, Star, Genre, Rating classes
    }
}
```

### Example: MongoDB Servlet (New)
```java
// Uses new models: MovieDocument.java, etc.
import models.MovieDocument;

public class MovieMongoServlet extends HttpServlet {
    private MongoDatabase database;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        MongoCollection<MovieDocument> collection = 
            database.getCollection("movies", MovieDocument.class);
        
        MovieDocument movie = collection.find(eq("_id", movieId)).first();
        String json = gson.toJson(movie);
        response.getWriter().write(json);
    }
}
```

---

## MongoDB Annotations Used

All Document models use **POJO codec annotations** (compatible with MongoDB Java Driver 4.x+):

```java
@BsonId              // Marks the primary key field
@BsonProperty("...")  // Maps field to MongoDB document property
```

### Alternative: Spring Data MongoDB
If you're using Spring Data MongoDB, you can replace annotations with:
```java
@Document(collection = "movies")  // Class level
@Id                               // Instead of @BsonId  
@Field("...")                     // Instead of @BsonProperty
```

---

## How to Use MongoDB Documents in Your Servlets

### Example 1: Movie Lookup

**Before (MySQL with old Star.java):**
```java
import models.Star;
import models.Genre;
import models.Rating;

// Complex JOIN query needed
String query = "SELECT m.*, r.ratings, r.vote_count, " +
               "GROUP_CONCAT(DISTINCT g.name) as genres, " +
               "GROUP_CONCAT(DISTINCT s.name) as stars " +
               "FROM movies m " +
               "LEFT JOIN ratings r ON m.id = r.movie_id " +
               "LEFT JOIN genres_in_movies gim ON m.id = gim.movie_id " +
               "LEFT JOIN genres g ON gim.genre_id = g.id " +
               "LEFT JOIN stars_in_movies sim ON m.id = sim.movie_id " +
               "LEFT JOIN stars s ON sim.star_id = s.id " +
               "WHERE m.id = ? " +
               "GROUP BY m.id";
// ... 50+ lines of ResultSet processing
```

**After (MongoDB with new MovieDocument.java):**
```java
import models.MovieDocument;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;

MongoCollection<MovieDocument> movieCollection = 
    database.getCollection("movies", MovieDocument.class);

MovieDocument movie = movieCollection.find(eq("_id", movieId)).first();

// Stars, genres, and rating are already embedded!
// No joins needed!
String json = gson.toJson(movie);
response.getWriter().write(json);
```

### Example 2: Customer with Credit Card

**Before (MySQL):**
```java
// Need to join customers and credit_cards tables
String query = "SELECT c.*, cc.* FROM customers c " +
               "LEFT JOIN credit_cards cc ON c.credit_card_id = cc.id " +
               "WHERE c.email = ? AND c.password = ?";
```

**After (MongoDB with CustomerDocument.java):**
```java
import models.CustomerDocument;

MongoCollection<CustomerDocument> customerCollection = 
    database.getCollection("customers", CustomerDocument.class);

CustomerDocument customer = customerCollection
    .find(and(eq("email", email), eq("password", hashedPassword)))
    .first();

// Credit card is already embedded!
if (customer != null && customer.getCreditCard() != null) {
    Date expiration = customer.getCreditCard().getExpiration();
    // No second query needed!
}
```

---

## Database Connection Setup

### MongoDB Connection Manager

```java
package config;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientSettings;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.*;

public class MongoDBConnectionManager {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    public static void initialize(String connectionString, String dbName) {
        // Configure POJO codec for automatic Document model mapping
        CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(dbName)
                             .withCodecRegistry(pojoCodecRegistry);
    }
    
    public static MongoDatabase getDatabase() {
        if (database == null) {
            initialize("mongodb://localhost:27017", "moviedb");
        }
        return database;
    }
    
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
```

### Usage in Servlet Context
```java
// In your ServletContextListener
@WebListener
public class AppContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize both databases during migration
        
        // MySQL (existing)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // ... MySQL DataSource setup
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // MongoDB (new)
        MongoDBConnectionManager.initialize(
            "mongodb://localhost:27017",
            "moviedb"
        );
        
        sce.getServletContext().setAttribute("mongoDatabase", 
            MongoDBConnectionManager.getDatabase());
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoDBConnectionManager.close();
    }
}
```

---

## Migration Phases

### Phase 1: Dual-Write ✅ (Current - Safe)
1. ✅ Keep all old MySQL models intact
2. ✅ Use new MongoDB Document models for MongoDB operations
3. ✅ Run both databases in parallel
4. Write new data to **both** databases
5. Read from MySQL (no user impact)

**Status**: Your current setup - **safe to continue using MySQL**

### Phase 2: Dual-Read (Testing)
1. Create new servlets that read from MongoDB (e.g., `MovieMongoServlet`)
2. Test thoroughly alongside existing MySQL servlets
3. Compare results between MySQL and MongoDB
4. Keep writing to both databases

### Phase 3: MongoDB Primary (Cutover)
1. Update existing servlets to use MongoDB Document models
2. Keep MySQL as backup (read-only)
3. Stop writing to MySQL

### Phase 4: Full Migration (Complete)
1. Remove MySQL connection code
2. Archive old MySQL models or delete them
3. Decommission MySQL database

---

## Mapper Pattern (Optional)

If you want to gradually transition, create mappers between old and new models:

```java
package utils;

import models.*;

public class ModelMapper {
    
    // Convert MongoDB Document → MySQL DTO (for backward compatibility)
    public static Star toMySQLStar(MovieDocument.Star mongoStar) {
        Star star = new Star();
        star.setId(mongoStar.getId());
        star.setName(mongoStar.getName());
        star.setBirth_year(mongoStar.getBirthYear());
        return star;
    }
    
    // Convert MySQL DTO → MongoDB Document (for migration)
    public static MovieDocument.Star toMongoStar(Star mysqlStar) {
        return new MovieDocument.Star(
            mysqlStar.getId(),
            mysqlStar.getName(),
            mysqlStar.getBirth_year()
        );
    }
}
```

---

## Required Indexes for Performance

Create these indexes in MongoDB for optimal performance:

```javascript
// In MongoDB shell or Compass
use moviedb;

// Movies collection
db.movies.createIndex({ title: "text" });
db.movies.createIndex({ year: 1, title: 1 });
db.movies.createIndex({ "genres.name": 1 });
db.movies.createIndex({ "stars.id": 1 });

// Stars collection
db.stars.createIndex({ name: "text" });
db.stars.createIndex({ name: 1 });

// Customers collection
db.customers.createIndex({ email: 1 }, { unique: true });
db.customers.createIndex({ mysqlId: 1 }); // For migration lookups

// Sales collection
db.sales.createIndex({ customerId: 1, saleDate: -1 });
db.sales.createIndex({ movieId: 1, saleDate: -1 });
db.sales.createIndex({ saleDate: -1 });

// Employees collection
db.employees.createIndex({ email: 1 }, { unique: true });

// Genres collection
db.genres.createIndex({ name: 1 });
```

---

## Best Practices

### 1. **Denormalization is Your Friend**
- ✅ Embed frequently accessed data (stars in movies)
- ✅ Keep counts (movieCount in StarDocument)
- ✅ Store display fields (customerName in SaleDocument)

### 2. **Handling Updates to Embedded Data**
When updating embedded data, update both places:

```java
// Update star name in stars collection
starCollection.updateOne(
    eq("_id", starId), 
    set("name", newName)
);

// Also update in all movies where this star appears
movieCollection.updateMany(
    eq("stars.id", starId),
    set("stars.$.name", newName)
);
```

### 3. **Security**
- ✅ Hash passwords before storing (bcrypt, argon2)
- ✅ Consider encrypting credit card data
- ✅ Use environment variables for connection strings
- ✅ Implement proper authentication and authorization

### 4. **Testing**
- ✅ Test MongoDB servlets alongside MySQL servlets
- ✅ Compare data consistency between both databases
- ✅ Use migration verifiers in `MongoDBMigration/validators/`

---

## Quick Reference: Model Files

```
src/main/java/models/
├── Star.java              ← MySQL model (OLD - KEEP)
├── Genre.java             ← MySQL model (OLD - KEEP)
├── Rating.java            ← MySQL model (OLD - KEEP)
├── MovieList.java         ← MySQL model (OLD - KEEP)
├── MovieResponse.java     ← MySQL model (OLD - KEEP)
│
├── MovieDocument.java     ← MongoDB model (NEW)
├── StarDocument.java      ← MongoDB model (NEW)
├── GenreDocument.java     ← MongoDB model (NEW)
├── CustomerDocument.java  ← MongoDB model (NEW)
├── EmployeeDocument.java  ← MongoDB model (NEW)
└── SaleDocument.java      ← MongoDB model (NEW)
```

---

## Next Steps

1. ✅ **Your MySQL servlets continue to work** with old models
2. **Create new MongoDB servlets** using Document models
3. **Test MongoDB functionality** without affecting MySQL
4. **Gradually migrate** servlet by servlet when confident
5. **Monitor performance** and adjust as needed

---

## Need Help?

If you need assistance with:
- Converting specific servlets to MongoDB
- Setting up connection pooling
- Creating data migration scripts
- Handling transactions
- Writing mappers between old and new models

Just ask! 🚀
