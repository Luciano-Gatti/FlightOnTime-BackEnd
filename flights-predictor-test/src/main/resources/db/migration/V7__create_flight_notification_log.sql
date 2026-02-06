-- NOTIFICATION LOG (dedup notificaciones)
CREATE TABLE flight_notification_log (
  id BIGINT NOT NULL AUTO_INCREMENT,

  user_id BIGINT NOT NULL,
  flight_request_id BIGINT NOT NULL,

  type VARCHAR(20) NOT NULL, -- T12H
  sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  CONSTRAINT fk_flight_notif_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_flight_notif_request
    FOREIGN KEY (flight_request_id) REFERENCES flight_request(id),

  UNIQUE KEY uq_flight_notif_dedup (user_id, flight_request_id, type),
  KEY idx_flight_notif_sent_at (sent_at)
) ENGINE=InnoDB;