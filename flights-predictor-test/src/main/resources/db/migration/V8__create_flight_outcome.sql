-- FLIGHT OUTCOME (real)
CREATE TABLE flight_outcome (
  id BIGINT NOT NULL AUTO_INCREMENT,

  flight_request_id BIGINT NOT NULL,

  actual_status VARCHAR(20) NOT NULL, -- ON_TIME / DELAYED / CANCELLED
  actual_departure_utc TIMESTAMP NULL,
  delay_minutes INT NULL,

  source VARCHAR(30) NOT NULL DEFAULT 'AERODATABOX',
  fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  CONSTRAINT fk_flight_outcome_request
    FOREIGN KEY (flight_request_id) REFERENCES flight_request(id),

  UNIQUE KEY uq_flight_outcome_request (flight_request_id),
  KEY idx_flight_outcome_status (actual_status)
) ENGINE=InnoDB;