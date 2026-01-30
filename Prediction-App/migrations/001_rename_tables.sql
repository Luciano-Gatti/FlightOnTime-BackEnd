-- Rename tables while preserving data and rebuilding foreign keys with updated names.
-- This script drops and recreates foreign keys that reference the renamed tables.

DELIMITER $$

CREATE PROCEDURE rename_tables_with_fks()
BEGIN
  DECLARE done INT DEFAULT 0;

  DECLARE fk_name VARCHAR(255);
  DECLARE new_fk_name VARCHAR(255);
  DECLARE tbl_name VARCHAR(255);
  DECLARE new_tbl_name VARCHAR(255);
  DECLARE ref_tbl_name VARCHAR(255);
  DECLARE new_ref_tbl_name VARCHAR(255);
  DECLARE col_list TEXT;
  DECLARE ref_col_list TEXT;
  DECLARE update_rule VARCHAR(20);
  DECLARE delete_rule VARCHAR(20);

  DECLARE cur_drop CURSOR FOR
    SELECT constraint_name, table_name
    FROM fk_recreate;

  DECLARE cur_add CURSOR FOR
    SELECT
      constraint_name,
      new_constraint_name,
      table_name,
      new_table_name,
      referenced_table_name,
      new_referenced_table_name,
      column_names,
      referenced_column_names,
      update_rule,
      delete_rule
    FROM fk_recreate;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  CREATE TEMPORARY TABLE rename_map (
    old_name VARCHAR(255) PRIMARY KEY,
    new_name VARCHAR(255) NOT NULL
  );

  INSERT INTO rename_map (old_name, new_name) VALUES
    ('request', 'flight_request'),
    ('prediction', 'flight_prediction'),
    ('user_prediction', 'user_prediction_snapshot'),
    ('flight_follow', 'flight_subscription'),
    ('flight_actual', 'flight_outcome'),
    ('notification_log', 'flight_notification_log');

  CREATE TEMPORARY TABLE fk_recreate AS
    SELECT
      rc.CONSTRAINT_NAME AS constraint_name,
      rc.TABLE_NAME AS table_name,
      rc.REFERENCED_TABLE_NAME AS referenced_table_name,
      rc.UPDATE_RULE AS update_rule,
      rc.DELETE_RULE AS delete_rule,
      GROUP_CONCAT(CONCAT('`', kcu.COLUMN_NAME, '`') ORDER BY kcu.ORDINAL_POSITION SEPARATOR ', ') AS column_names,
      GROUP_CONCAT(CONCAT('`', kcu.REFERENCED_COLUMN_NAME, '`') ORDER BY kcu.ORDINAL_POSITION SEPARATOR ', ') AS referenced_column_names,
      COALESCE(rm_table.new_name, rc.TABLE_NAME) AS new_table_name,
      COALESCE(rm_ref.new_name, rc.REFERENCED_TABLE_NAME) AS new_referenced_table_name,
      REPLACE(
        REPLACE(
          REPLACE(
            REPLACE(
              REPLACE(
                REPLACE(
                  rc.CONSTRAINT_NAME,
                  'request', 'flight_request'
                ),
                'prediction', 'flight_prediction'
              ),
              'user_prediction', 'user_prediction_snapshot'
            ),
            'flight_follow', 'flight_subscription'
          ),
          'flight_actual', 'flight_outcome'
        ),
        'notification_log', 'flight_notification_log'
      ) AS new_constraint_name
    FROM information_schema.REFERENTIAL_CONSTRAINTS rc
    JOIN information_schema.KEY_COLUMN_USAGE kcu
      ON kcu.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA
     AND kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME
     AND kcu.TABLE_NAME = rc.TABLE_NAME
    LEFT JOIN rename_map rm_table
      ON rc.TABLE_NAME = rm_table.old_name
    LEFT JOIN rename_map rm_ref
      ON rc.REFERENCED_TABLE_NAME = rm_ref.old_name
    WHERE rc.CONSTRAINT_SCHEMA = DATABASE()
      AND (
        rc.TABLE_NAME IN (
          'request',
          'prediction',
          'user_prediction',
          'flight_follow',
          'flight_actual',
          'notification_log'
        )
        OR rc.REFERENCED_TABLE_NAME IN (
          'request',
          'prediction',
          'user_prediction',
          'flight_follow',
          'flight_actual',
          'notification_log'
        )
      )
    GROUP BY
      rc.CONSTRAINT_NAME,
      rc.TABLE_NAME,
      rc.REFERENCED_TABLE_NAME,
      rc.UPDATE_RULE,
      rc.DELETE_RULE,
      rm_table.new_name,
      rm_ref.new_name;

  OPEN cur_drop;
  drop_loop: LOOP
    FETCH cur_drop INTO fk_name, tbl_name;
    IF done = 1 THEN
      LEAVE drop_loop;
    END IF;

    SET @sql = CONCAT('ALTER TABLE `', tbl_name, '` DROP FOREIGN KEY `', fk_name, '`');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur_drop;

  RENAME TABLE
    `request` TO `flight_request`,
    `prediction` TO `flight_prediction`,
    `user_prediction` TO `user_prediction_snapshot`,
    `flight_follow` TO `flight_subscription`,
    `flight_actual` TO `flight_outcome`,
    `notification_log` TO `flight_notification_log`;

  SET done = 0;

  OPEN cur_add;
  add_loop: LOOP
    FETCH cur_add INTO
      fk_name,
      new_fk_name,
      tbl_name,
      new_tbl_name,
      ref_tbl_name,
      new_ref_tbl_name,
      col_list,
      ref_col_list,
      update_rule,
      delete_rule;
    IF done = 1 THEN
      LEAVE add_loop;
    END IF;

    SET @sql = CONCAT(
      'ALTER TABLE `', new_tbl_name, '` ',
      'ADD CONSTRAINT `', new_fk_name, '` ',
      'FOREIGN KEY (', col_list, ') ',
      'REFERENCES `', new_ref_tbl_name, '` (', ref_col_list, ') ',
      'ON UPDATE ', update_rule, ' ',
      'ON DELETE ', delete_rule
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur_add;

  DROP TEMPORARY TABLE IF EXISTS fk_recreate;
  DROP TEMPORARY TABLE IF EXISTS rename_map;
END$$

DELIMITER ;

CALL rename_tables_with_fks();
DROP PROCEDURE rename_tables_with_fks;
