// Switch to the desired database
db = db.getSiblingDB('my_database');

// Create a collection with an initial record
db.users.insertMany([
  { name: "Admin User", role: "superuser" },
  { name: "Test User", role: "guest" }
]);

// Optional: Create an additional empty collection
db.createCollection('logs');