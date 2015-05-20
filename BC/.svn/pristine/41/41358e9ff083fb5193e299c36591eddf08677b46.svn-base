SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`egm_serial_number` (
  `serial_number` INT(11) NOT NULL AUTO_INCREMENT ,
  `guid` VARCHAR(22) NOT NULL ,
  PRIMARY KEY (`serial_number`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'EGM serial numbers';

ALTER TABLE `behaviour_composer_schema2`.`micro_behaviour` 
DROP INDEX `url` 
, ADD INDEX `url` (`url`(255) ASC) ;

ALTER TABLE `behaviour_composer_schema2`.`event_add_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_remove_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_activate_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_inactivate_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_activate_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_inactivate_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_add_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_browse_to_page` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_edit_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_load_model` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  , CHANGE COLUMN `model_guid` `model_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_remove_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_rename_micro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_rename_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL DEFAULT 'CURRENT_TIMESTAMP'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_start` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  , CHANGE COLUMN `read_only_session_guid` `read_only_session_guid` CHAR(22) ASCII NULL DEFAULT NULL  , CHANGE COLUMN `initial_read_only_session_guid` `initial_read_only_session_guid` CHAR(22) ASCII NULL DEFAULT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_undo` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_redo` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`compound_event_start` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`compound_event_stop` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL  ;

ALTER TABLE `behaviour_composer_schema2`.`event_toggle_include_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_add_to_model_list_box_macro_behaviour` CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
