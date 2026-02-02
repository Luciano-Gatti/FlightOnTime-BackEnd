ALTER TABLE flight_prediction
  ADD COLUMN confidence VARCHAR(20) NULL AFTER predicted_probability,
  ADD COLUMN threshold_used DOUBLE NULL AFTER confidence;
