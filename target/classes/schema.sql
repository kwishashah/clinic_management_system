-- Schema bootstrap for first-time installs. All statements are idempotent.

CREATE TABLE IF NOT EXISTS users (
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS PatientHistory (
    patient_id            INT AUTO_INCREMENT PRIMARY KEY,
    patient_name          VARCHAR(150),
    mobile_number         VARCHAR(20),
    age                   INT,
    gender                VARCHAR(20),
    marital_status        VARCHAR(20),
    address               TEXT,
    occupation            VARCHAR(100),
    blood_group           VARCHAR(10),
    height                FLOAT,
    weight                FLOAT,
    suffering_duration    VARCHAR(100),
    main_disease          TEXT,
    complications         TEXT,
    symptoms              TEXT,
    pain_points           TEXT,
    tongue                VARCHAR(100),
    stool                 VARCHAR(100),
    urine                 VARCHAR(100),
    nails                 VARCHAR(100),
    navel                 VARCHAR(100),
    neurotherapy_required VARCHAR(100),
    previous_treatment    TEXT,
    medicines             TEXT,
    detailed_history      TEXT,
    examination           TEXT,
    bp                    VARCHAR(20),
    pulse                 VARCHAR(20),
    o2                    VARCHAR(20),
    temperature           VARCHAR(20),
    user_id               INT NOT NULL,
    reports               TEXT,
    media                 TEXT,
    patient_story         TEXT,
    remarks               TEXT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- Covering index for the paginated patient listing on the Doctor Dashboard.
-- Supports both the WHERE user_id = ? filter and the ORDER BY patient_id DESC sort
-- so each LIMIT/OFFSET page reads ~50 rows instead of doing a full scan + filesort.
-- Wrapped in an information_schema check to stay idempotent across re-runs (older
-- MySQL versions do not support CREATE INDEX IF NOT EXISTS).
SET @ix := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name   = 'PatientHistory'
      AND index_name   = 'idx_patient_user_created'
);

SET @sql := IF(
    @ix = 0,
    'CREATE INDEX idx_patient_user_created ON PatientHistory (user_id, patient_id)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS NeurotherapySessions (
    session_id      INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    session_number  INT,
    session_date    DATE,
    treatment_given TEXT,
    pain_before     TEXT,
    pain_after      TEXT,
    session_summary TEXT,
    CONSTRAINT fk_session_patient FOREIGN KEY (patient_id) REFERENCES PatientHistory (patient_id)
);
