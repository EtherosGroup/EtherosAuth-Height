CREATE TABLE IF NOT EXISTS player_account(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS player_location (
    uuid TEXT NOT NULL PRIMARY KEY,
    world TEXT NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    yaw REAL NOT NULL,
    pitch REAL NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_world ON player_location (world);
