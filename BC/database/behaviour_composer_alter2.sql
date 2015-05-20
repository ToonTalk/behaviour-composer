SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `behaviour_composer_schema2`.`micro_behaviour` 
DROP INDEX `url` 
, ADD INDEX `url` (`url`(255) ASC) ;

ALTER TABLE `behaviour_composer_schema2`.`event_remove_micro_behaviour` ADD COLUMN ` text_area_updates_id` INT(11) NOT NULL  AFTER `user_id` , CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_activate_micro_behaviour` ADD COLUMN ` text_area_updates_id` INT(11) NOT NULL  AFTER `user_id` , CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`event_inactivate_micro_behaviour` ADD COLUMN `text_area_updates_id` INT(11) NOT NULL  AFTER `user_id` , CHANGE COLUMN `session_guid` `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.'  ;

ALTER TABLE `behaviour_composer_schema2`.`micro_behaviour_copy_update` CHANGE COLUMN `index` `id` INT(11) NOT NULL AUTO_INCREMENT  

, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`id`) ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
