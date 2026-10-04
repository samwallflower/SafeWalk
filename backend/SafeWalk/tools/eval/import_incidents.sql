-- SafeWalk evaluation import (police.uk -> incident_report). Run in the EVAL/DEV database only.
-- Loads T1 incidents of BOTH cities (they are in different places, so they do not interfere).
-- Prerequisites: run prepare_data.py for cityA and cityB; start the backend once so that
-- DataInitializer has created the 6+ incident categories and users 1..5.
-- Run with psql from tools/data/processed:   psql -d <db> -f ../../eval/import_incidents.sql

-- 1) staging table
DROP TABLE IF EXISTS stg_incident;
CREATE TABLE stg_incident (
  incident_id int, month text, period text, category text,
  lat double precision, lng double precision
);

-- 2) load both CSVs (client-side \copy; adjust the paths if you run from elsewhere)
\copy stg_incident FROM 'cityA_incidents.csv' CSV HEADER
\copy stg_incident FROM 'cityB_incidents.csv' CSV HEADER

-- 3) sanity: every category must match a seeded category (matched must be true for all)
SELECT s.category, count(*) AS rows, bool_or(c.id IS NOT NULL) AS matched
FROM stg_incident s LEFT JOIN incident_category c ON lower(c.name) = lower(s.category)
GROUP BY s.category;

-- 4) insert T1 only (the app then "knows" the same incidents the offline scorer uses).
--    Reports are spread round-robin over users 1..5 so user-facing code paths
--    (e.g. report.getUser().getId()) never see a NULL user. timestamp = month-15.
INSERT INTO incident_report
  (description, latitude, longitude, "timestamp", is_anonymous, upvotes, downvotes, user_id, category_id, status)
SELECT 'police.uk ' || s.category || ' ' || s.month,
       s.lat, s.lng,
       (s.month || '-15 12:00:00')::timestamp,
       true, 0, 0,
       1 + (row_number() OVER (ORDER BY s.month, s.lat, s.lng) % 5),
       c.id, 'ACTIVE'
FROM stg_incident s
JOIN incident_category c ON lower(c.name) = lower(s.category)
WHERE s.period = 'T1';

-- 5) verify (compare with T1 counts printed by prepare_data.py)
SELECT count(*) FROM incident_report;
SELECT c.name, count(*) FROM incident_report r JOIN incident_category c ON c.id = r.category_id GROUP BY c.name;

DROP TABLE stg_incident;
