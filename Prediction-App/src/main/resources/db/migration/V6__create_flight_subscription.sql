-- FLIGHT SUBSCRIPTION (seguimiento para jobs)
CREATE TABLE flight_subscription (
  id BIGINT NOT NULL AUTO_INCREMENT,

  user_id BIGINT NOT NULL,
  flight_request_id BIGINT NOT NULL,

  refresh_mode VARCHAR(20) NOT NULL, -- T12_ONLY | T72_REFRESH

  -- Baseline = lo que el usuario vio originalmente (snapshot), no la prediction global
  baseline_snapshot_id BIGINT NULL,

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_flight_sub_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_flight_sub_request
    FOREIGN KEY (flight_request_id) REFERENCES flight_request(id),

  CONSTRAINT fk_flight_sub_baseline_snapshot
    FOREIGN KEY (baseline_snapshot_id) REFERENCES user_prediction_snapshot(id),

  UNIQUE KEY uq_flight_sub_user_request (user_id, flight_request_id),
  KEY idx_flight_sub_mode (refresh_mode),
  KEY idx_flight_sub_request (flight_request_id)
) ENGINE=InnoDB;