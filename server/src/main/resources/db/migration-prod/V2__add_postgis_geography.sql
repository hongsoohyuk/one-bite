-- V2: PostGIS geography 컬럼 추가 (PostgreSQL 전용)

CREATE EXTENSION IF NOT EXISTS postgis;

-- geography 컬럼 추가 (기존 lat/lng에서 생성)
ALTER TABLE split_requests ADD COLUMN location geography(POINT, 4326);

-- 기존 데이터 변환
UPDATE split_requests
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL;

-- 공간 인덱스 생성
CREATE INDEX idx_split_location_geo ON split_requests USING GIST (location);

-- lat/lng 변경 시 location 자동 동기화 트리거
CREATE OR REPLACE FUNCTION sync_split_location()
RETURNS TRIGGER AS $$
BEGIN
    NEW.location := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_split_location
    BEFORE INSERT OR UPDATE OF latitude, longitude ON split_requests
    FOR EACH ROW
    EXECUTE FUNCTION sync_split_location();
