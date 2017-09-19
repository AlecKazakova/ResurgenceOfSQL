# FINDINGS
For a constant number of users (10000) the following methods for querying were tested.

### select all, filter in java

`SELECT * FROM friendship` and `SELECT * FROM user_checkin` are run and the results are used to
compute the number of checkins with friends.

| Checkins      | ms          |
| ------------- |-------------|
|10|817|
|100|810|
|1000|937|
|10000|4428|
|20000|10475|
|30000|17616|
|50000|31400|

Although this still involves SQLite, a better base case would be assuming we have the data given to us and then
performing the query.

### filter in java

The same, but assume the data is given to us and only measure the logic of finding check ins with friends

| Checkins      | ms          |
| ------------- |-------------|
|10|370|
|100|354|
|1000|510|
|10000|3898|
|20000|10151|
|30000|15607|
|50000|29984|

So our queries are accounting for somewhere around 10% of the initial implementation.

### select only friends, filter in java

Use SQL to select only friendships:

```sql
SELECT friend1
FROM friendship
WHERE friend2 = :my_id
UNION
SELECT friend2
FROM friendship
WHERE friend1 = :my_id
```

Then `SELECT * FROM user_checkin` and filter in java

| Checkins      | ms          |
| ------------- |-------------|
|10|15|
|100|20|
|1000|137|
|10000|3089|
|20000|8197|
|30000|13897|
|50000|23921|

The queries with small number of checkins are so significantly faster because the number of users is constant
at 10000. Since we're only selecting friends we're loading a much smaller number of Users into memory and operating
on them. Time to switch to exclusively sql...

### Use subqueries

We're going to abuse sqlite subqueries to do what is essentially `Set.contains`

```sql
SELECT count(*)
FROM checkin
WHERE _id IN (
  SELECT checkin_id
  FROM user_checkin
  WHERE user_id IN (
    SELECT friend1
    FROM friendship
    WHERE friend2 = :my_id
    UNION
    SELECT friend2
    FROM friendship
    WHERE friend1 = :my_id
  )
);
```

| Checkins      | ms          |
| ------------- |-------------|
|10|13|
|100|12|
|1000|15|
|10000|65|
|20000|138|
|30000|215|
|50000|402|

Huge increase in performance but not easy to read - can we make it more legible?

### Same as before but use join/distinct to remove a subquery

In sqlite there is always a way to turn a filtering subquery into a join, so lets do just that.

```sql
SELECT count(DISTINCT _id)
FROM checkin
JOIN user_checkin ON (_id = checkin_id)
WHERE user_id IN (
  SELECT friend1
  FROM friendship
  WHERE friend2 = :my_id
  UNION
  SELECT friend2
  FROM friendship
  WHERE friend1 = :my_id
);
```

Important to note that we are doing `count(DISTINCT _id)` instead of `count(_id)` because the query returns a row
for every user_checkin - the join does a cross product.

| Checkins      | ms          |
| ------------- |-------------|
|10|12|
|100|13|
|1000|15|
|10000|71|
|20000|154|
|30000|231|
|50000|448|

### Same as before but use join to remove other subquery

```sql
SELECT count(DISTINCT checkin._id)
FROM checkin
JOIN user_checkin ON (_id = checkin_id)
JOIN friendship ON (
  (user_id = friend1 AND friend2 = :my_id) OR
  (user_id = friend2 AND friend1 = :my_id)
);
```

| Checkins      | ms          |
| ------------- |-------------|
|10|0|
|100|0|
|1000|8|
|10000|129|
|20000|317|
|30000|500|
|50000|894|

Since we're only selecting the checkin id and thats also stored in the user_checkin table we can remove
the select form the checkin table

### Remove first unused table

```sql
SELECT count(DISTINCT checkin_id)
FROM user_checkin
JOIN friendship ON (
  (user_id = friend1 AND friend2 = :my_id) OR
  (user_id = friend2 AND friend1 = :my_id)
);
```

| Checkins      | ms          |
| ------------- |-------------|
|10|0|
|100|0|
|1000|7|
|10000|114|
|20000|288|
|30000|454|
|50000|806|

Whats going on? Why are the joins taking almost twice as long as the query with two subquerys? Thankfully SQLite has the
perfect tool for this use case: `EXPLAIN QUERY PLAN`

### Explain Query Plan

By writing `EXPLAIN QUERY PLAN` before any sqlite query you can get a readable set of instructions that SQLite internally
is running to perform your query. The actual virtual machine code can also by analyzed by writing `EXPLAIN` before any
SQLite statement. To figure out why our join query is taking longer lets analyze the last query we wrote as well as the
first query we wrote with two subqueries which ran the fastest.

Our join query:

```sql
EXPLAIN QUERY PLAN 
SELECT count(DISTINCT checkin_id)
FROM user_checkin
JOIN friendship ON (
  (user_id = friend1 AND friend2 = :my_id) OR
  (user_id = friend2 AND friend1 = :my_id)
);
```

and its output:

|selectid|order|from|detail|
|-|-|-|-|
|"0"	|"0"|	"0"	|"SCAN TABLE user_checkin"|
|"0"|	"1"	|"1"|	"SEARCH TABLE friendship USING COVERING INDEX sqlite_autoindex_friendship_1 (friend1=? AND friend2=?)"|
|"0"	|"1"	|"1"|	"SEARCH TABLE friendship USING COVERING INDEX sqlite_autoindex_friendship_1 (friend1=? AND friend2=?)"|

The detail tells us the instruction that is being run, in this case a search or a scan. A "SCAN" of a table is a full
table scan in which we visit every row. A "SEARCH" of a table means we only visit a subset of the rows for a table,
using an index to determine the subset we're visiting. The "order" column tells us the nesting order of that instruction.
In this way it's easy to think of the query plan as a loop. The loop begins by iterating every row of the "user_checkin"
table (since it is a scan), and for each row it iterates over a subset of the friendship table, twice, wth respect to an
an index. If you had created an index that the search could take advantage of SQLite detects that and uses your index, but
SQLite is also capable of creating transient SQLite indexes for a single query. And so for this query sqlite creates
a transient (temporary) covering index. Covering means it multiple columns are contained by the index.

Alright, so to recap sqlite iterates over every row of user_checkin and then performs two searches. The first is for rows
in friendship where friend1 = my_id and friend2 = user_id and the second search is for rows in friendship where
friend1 = user_id and friend2 = my_id.

Now lets take a look at the subquery query plan:
```sql
EXPLAIN QUERY PLAN
SELECT count(*)
FROM checkin
WHERE _id IN (
  SELECT checkin_id
  FROM user_checkin
  WHERE user_id IN (
    SELECT friend1
    FROM friendship
    WHERE friend2 = :my_id
    UNION
    SELECT friend2
    FROM friendship
    WHERE friend1 = :my_id
  )
);
```

which has the output:

|selectid|order|from|detail|
|-|-|-|-|
|"0"|	"0"|	"0"|	"SEARCH TABLE checkin USING INTEGER PRIMARY KEY (rowid=?)"|
|"0"|	"0"|	"0"|	"EXECUTE LIST SUBQUERY 1"|
|"1"|	"0"|	"0"|	"SCAN TABLE user_checkin"|
|"1"|	"0"|	"0"|	"EXECUTE LIST SUBQUERY 2"|
|"3"|	"0"|	"0"|	"SCAN TABLE friendship USING COVERING INDEX sqlite_autoindex_friendship_1"|
|"4"|	"0"|	"0"|	"SEARCH TABLE friendship USING COVERING INDEX sqlite_autoindex_friendship_1 (friend1=?)"|
|"2"|	"0"|	"0"|	"COMPOUND SUBQUERIES 3 AND 4 USING TEMP B-TREE (UNION)"|

Now we're seeing values for the "selectid" column. This column is just an identifier for the subquery. Compound queries
are considered a subquery so the subqueries correspond to:

Subquery 1:
```sql
SELECT checkin_id
FROM user_checkin
WHERE user_id IN (
  SELECT friend1
  FROM friendship
  WHERE friend2 = :my_id
  UNION
  SELECT friend2
  FROM friendship
  WHERE friend1 = :my_id
)
```

Subquery 2:
```sql
SELECT friend1
FROM friendship
WHERE friend2 = :my_id
UNION
SELECT friend2
FROM friendship
WHERE friend1 = :my_id
```

Subquery 3:
```sql
SELECT friend1
FROM friendship
WHERE friend2 = :my_id
```

Subquery 4:
```sql
SELECT friend2
FROM friendship
WHERE friend1 = :my_id
```

We're also seeing a new command in the detail column: "EXECUTE"

Execute corresponds to sqlite performing a query. There are result types for an execute: SCALAR and LIST. In our case both
subqueries are LIST as they return a result set, a SCALAR subquery would look like `SELECT 0`

There are two types of subqueries, CORRELATED and non correlated. A correlated subquery will appear as
`EXECUTE CORRELATED LIST SUBQUERY` in the query plan. Correlated queries are run for each row in the iteration. In our case,
both subqueries are not correlated so they are only executed once and the result is stored to be used by the query.

So whats happening in the full query plan. First we search the checkin table, in this case we're searching using the
primary key index (_id) for its presence in subquery 1. So we execute the subquery.

The first subquery is executed which scans the user_checkin table (meaning it iterates over ever row). It executes subquery 2
(which is the compound subquery).

The compound subquery executes subqueries 3 and 4 and unions them with a B-Tree. The union operator does not include
duplicate rows, which is where a B-Tree is used to prevent duplicates.

The autoindex created for subqueries 3 and 4 does not cover `friend2` which is why subquery 3 scans the friendship table
while subquery 4 searches.

The important thing here is that none of these queries are nested, they all have order 0. SQLite executes the subqueries
and stores the results while scanning the user_checkin table. Where in the join we were doing a nested search of the
friendship table, we are only doing a top level scan of the friendship table.

There's some interesting things to note here though... we can ditch the checkin table from the query as we did before to get
rid of the initial search and promote subquery 1. We can also create our own indexes for friend2 and user_id so
that we only ever do searches instead of scans.

### Creating indexes

Lets create the two indexes to increase the performance of our query.

```sql
CREATE INDEX userIdIndex ON user_checkin(user_id);

CREATE INDEX friend2Index ON friendship(friend2);
```

And now lets see how our query looks with explain query plan:

```sql
EXPLAIN QUERY PLAN
SELECT count(DISTINCT checkin_id)
FROM user_checkin
WHERE user_id IN (
  SELECT friend1
  FROM friendship
  WHERE friend2 = :my_id
  UNION
  SELECT friend2
  FROM friendship
  WHERE friend1 = :my_id
);
```

outputs:

|selectid|order|from|detail|
|-|-|-|-|
|"0"|	"0"|	"0"|	"SEARCH TABLE user_checkin USING INDEX userIdIndex (user_id=?)"|
|"0"|	"0"|	"0"|	"EXECUTE LIST SUBQUERY 1"|
|"2"|	"0"|	"0"|	"SEARCH TABLE friendship USING INDEX friend2Index (friend2=?)"|
|"3"|	"0"|	"0"|	"SEARCH TABLE friendship USING COVERING INDEX sqlite_autoindex_friendship_1 (friend1=?)"|
|"1"|	"0"|	"0"|	"COMPOUND SUBQUERIES 2 AND 3 USING TEMP B-TREE (UNION)"|

You can see that our two indexes we created are being used to search instead of scan, and there is one less subquery being
executed since we removed the select from checkin.

And the runtimes:

| Checkins      | ms          |
| ------------- |-------------|
|10|7|
|100|7|
|1000|10|
|10000|59|
|20000|106|
|30000|169|
|50000|331|

Hopefully it makes sense why this is the fastest of all the queries we wrote. There are smaller optimizations that could
be made by removing the compound UNION operator and instead using a single select on the `friendship` table.
