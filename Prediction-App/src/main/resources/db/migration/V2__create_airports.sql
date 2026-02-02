CREATE TABLE airports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  airport_iata CHAR(3) NOT NULL,
  airport_name VARCHAR(255),
  country VARCHAR(50),
  city_name VARCHAR(100),
  latitude DECIMAL(10,4) NOT NULL,
  longitude DECIMAL(10,4) NOT NULL,
  elevation DECIMAL(6,2),
  time_zone VARCHAR(50) NOT NULL,
  google_maps VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_airports_iata (airport_iata)
) ENGINE=InnoDB;