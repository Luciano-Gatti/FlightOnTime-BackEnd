CREATE TABLE user_prediction_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,

  user_id BIGINT NOT NULL,
  flight_request_id BIGINT NOT NULL,
  flight_prediction_id BIGINT NOT NULL,

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  source VARCHAR(20) NOT NULL DEFAULT 'USER_QUERY',

  PRIMARY KEY (id),

  CONSTRAINT fk_user_pred_snap_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_user_pred_snap_request
    FOREIGN KEY (flight_request_id) REFERENCES flight_request(id),

  CONSTRAINT fk_user_pred_snap_prediction
    FOREIGN KEY (flight_prediction_id) REFERENCES flight_prediction(id),

  KEY idx_user_pred_snap_user (user_id, created_at),
  KEY idx_user_pred_snap_request (flight_request_id),

  UNIQUE KEY uq_user_pred_snap_dedup (user_id, flight_prediction_id)
) ENGINE=InnoDB;