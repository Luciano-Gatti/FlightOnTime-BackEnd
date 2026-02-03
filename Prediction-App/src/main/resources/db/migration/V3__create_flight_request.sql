-- FLIGHT REQUEST (vuelo global; flight_number opcional)
CREATE TABLE flight_request (
  id BIGINT NOT NULL AUTO_INCREMENT,

  flight_date_utc TIMESTAMP NOT NULL,
  airline_code CHAR(2) NOT NULL,
  flight_number VARCHAR(10) NULL,

  origin_iata CHAR(3) NOT NULL,
  dest_iata CHAR(3) NOT NULL,

  distance DOUBLE NOT NULL,

  active BOOLEAN NOT NULL DEFAULT TRUE,
  closed_at TIMESTAMP NULL,

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  -- Identidad del vuelo SIN flight_number ni distance (flight_number es opcional)
  UNIQUE KEY uq_flight_request_unique (
    flight_date_utc, airline_code, origin_iata, dest_iata
  ),

  KEY idx_flight_request_active_date (active, flight_date_utc)
) ENGINE=InnoDB;