-- Required before running the experiment (run once in pgAdmin on the SafeWalk DB).
-- IncidentReportRepository.findNearBy builds the geography on the fly:
--   ST_SetSRID(ST_MakePoint(ir.longitude, ir.latitude),4326)::geography
-- Without an index on exactly that expression every segment lookup scans the whole
-- table (~150 s per /routing/recommend call with 29k incidents). This index makes
-- ST_DWithin use a GiST lookup instead. No application change is needed.
CREATE INDEX IF NOT EXISTS idx_incident_report_geog ON incident_report
  USING GIST ((ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography));
ANALYZE incident_report;
