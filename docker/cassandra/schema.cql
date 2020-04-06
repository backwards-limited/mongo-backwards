CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE TABLE IF NOT EXISTS mykeyspace.user_by_first_name (
    firstname text PRIMARY KEY,
    email text,
    id uuid,
    lastname text
);

CREATE TABLE IF NOT EXISTS mykeyspace.user_by_id (
    id uuid PRIMARY KEY,
    email text,
    firstname text,
    lastname text
);