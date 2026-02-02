CREATE TABLE flight_prediction (
  id BIGINT NOT NULL AUTO_INCREMENT,

  flight_request_id BIGINT NOT NULL,

  forecast_bucket_utc TIMESTAMP NOT NULL,
  predicted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  predicted_status VARCHAR(20) NOT NULL,      -- ON_TIME / DELAYED
  predicted_probability DOUBLE NULL,
  model_version VARCHAR(50) NULL,

  source VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',

  PRIMARY KEY (id),

  CONSTRAINT fk_flight_prediction_request
    FOREIGN KEY (flight_request_id) REFERENCES flight_request(id),

  UNIQUE KEY uq_flight_prediction_bucket (flight_request_id, forecast_bucket_utc),
  KEY idx_flight_prediction_request (flight_request_id),
  KEY idx_flight_prediction_bucket (forecast_bucket_utc)
) ENGINE=InnoDB;