SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `behaviour_composer_schema2` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `behaviour_composer_schema2`;

-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`session`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`session` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`session` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `read_write_guid` CHAR(22) NOT NULL ,
  `read_only_guid` CHAR(22) NOT NULL ,
  `start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `update_time` TIMESTAMP NULL ,
  `comment` MEDIUMTEXT NULL ,
  `type` INT NOT NULL DEFAULT 0 COMMENT '0 is BC2, 1 is MoPiX' ,
  UNIQUE INDEX `read_only_guid` (`read_only_guid` ASC) ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'A session is a sequence of events.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`micro_behaviour` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `url` VARCHAR(2087) NOT NULL COMMENT 'See http://support.microsoft.com/kb/208427' ,
  `description` MEDIUMTEXT NULL COMMENT 'Name or description of the micro-behaviour scraped off the web page with the url.' ,
  `cached_code` MEDIUMTEXT NULL ,
  `cache_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `ancestor_micro_behaviour_id` INT NULL COMMENT 'The micro-behaviour that was edited to create this one.' ,
  PRIMARY KEY (`id`) ,
  INDEX `url` (`url`(255) ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`model`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`model` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`model` (
  `guid` CHAR(22) NOT NULL ,
  `session_guid` CHAR(22) NOT NULL ,
  `creation_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `ancestor_model_guid` CHAR(22) NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`guid`) )
ENGINE = InnoDB
COMMENT = 'Model creation';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`prototype_in_model`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`prototype_in_model` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`prototype_in_model` (
  `prototype_id` INT NOT NULL ,
  `model_guid` CHAR(22) NOT NULL ,
  `position` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`prototype_id`) ,
  UNIQUE INDEX `model_guid` (`model_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A table of prototypes in models.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`prototype`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`prototype` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`prototype` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `creation_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`micro_behaviour_in_prototype`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`micro_behaviour_in_prototype` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`micro_behaviour_in_prototype` (
  `prototype_id` INT NOT NULL AUTO_INCREMENT ,
  `micro-behaviour_id` INT NOT NULL ,
  PRIMARY KEY (`prototype_id`) ,
  UNIQUE INDEX `micro_behaviour_id` (`micro-behaviour_id` ASC) )
ENGINE = InnoDB
COMMENT = 'Records the micro-behaviours in a prototype.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`micro_behaviour_load`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`micro_behaviour_load` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`micro_behaviour_load` (
  `micro_behaviour_id` INT NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  `load_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`micro_behaviour_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`user` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `guid` CHAR(22) NOT NULL ,
  `user_agent` MEDIUMTEXT NULL ,
  `creation_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `guid` (`guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_add_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_add_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_add_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `micro_behaviour_id` INT NOT NULL ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  `text_area_updates_id` INT NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  `insertion_index` INT NOT NULL DEFAULT 9999999 ,
  `name_if_macro_behaviour` MEDIUMTEXT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been added to a prototype.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_remove_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_remove_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_remove_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `micro_behaviour_id` INT NOT NULL ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT NOT NULL ,
  `text_area_updates_id` INT NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  `name_if_macro_behaviour` MEDIUMTEXT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) ,
  INDEX `micro_behaviour_id` (`micro_behaviour_id` ASC) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been removed from a prototype.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_activate_macro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_activate_macro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_activate_macro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A macro-behaviour has been activated.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_inactivate_macro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_inactivate_macro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_inactivate_macro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A macro-behaviour has been inactivated.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_activate_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_activate_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_activate_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `micro_behaviour_id` INT NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  `text_area_updates_id` INT NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  `name_if_macro_behaviour` MEDIUMTEXT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been activated.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_inactivate_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_inactivate_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_inactivate_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `micro_behaviour_id` INT NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  `text_area_updates_id` INT NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  `name_if_macro_behaviour` MEDIUMTEXT NULL ,
  INDEX `session_guid` (`session_guid` ASC) ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been inactivated.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_add_macro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_add_macro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_add_macro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been added to a prototype.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_browse_to_page`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_browse_to_page` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_browse_to_page` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL ,
  `url` VARCHAR(2087) NOT NULL DEFAULT 'CURRENT_TIMESTAMP' ,
  `base_url` VARCHAR(2087) NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) ,
  INDEX `url` (`url`(255) ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_edit_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_edit_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_edit_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL ,
  `new_url` VARCHAR(2087) NOT NULL ,
  `old_url` VARCHAR(2087) NOT NULL ,
  `micro_behaviour_id` INT NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) ,
  INDEX `new_url` (`new_url`(255) ASC) ,
  INDEX `old_url` (`old_url`(255) ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_load_model`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_load_model` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_load_model` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `model_guid` CHAR(22) ASCII NOT NULL ,
  `replace_old_model` BIT NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) ,
  INDEX `model_guid` (`model_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_remove_macro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_remove_macro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_remove_macro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL COMMENT 'Name of the macro-behaviour at the time of the event.' ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `sessioin_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'A macro-behaviour has been removed from a model';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_rename_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_rename_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_rename_micro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `url_at_event_time` MEDIUMTEXT NOT NULL ,
  `old_name` MEDIUMTEXT NOT NULL ,
  `new_name` MEDIUMTEXT NOT NULL ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_rename_macro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_rename_macro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_rename_macro_behaviour` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL DEFAULT 'CURRENT_TIMESTAMP' ,
  `time` TIMESTAMP NOT NULL ,
  `new_name` MEDIUMTEXT NOT NULL ,
  `old_name` MEDIUMTEXT NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_start`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_start` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_start` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL COMMENT 'The read-write session GUID.' ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `read_only_session_guid` CHAR(22) ASCII NULL ,
  `initial_read_only_session_guid` CHAR(22) ASCII NULL ,
  `initial_model_guid` CHAR(22) NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB
COMMENT = 'The start of a session.';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_undo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_undo` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_undo` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_redo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_redo` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_redo` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`error`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`error` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`error` (
  `session_guid` CHAR(22) NULL ,
  `description` MEDIUMTEXT NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT NULL )
ENGINE = InnoDB
COMMENT = 'Record of all internal errors';


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`compound_event_start`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`compound_event_start` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`compound_event_start` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`compound_event_stop`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`compound_event_stop` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`compound_event_stop` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) ASCII NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`session_load`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`session_load` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`session_load` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_id` INT NOT NULL ,
  `user_id` INT NOT NULL ,
  `read_only` BOOLEAN NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  INDEX `user_id` (`user_id` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_update_text_area`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_update_text_area` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_update_text_area` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) NOT NULL ,
  `new_contents` MEDIUMTEXT NOT NULL ,
  `old_contents` MEDIUMTEXT NOT NULL ,
  `index_in_code` INT NOT NULL ,
  `micro_behaviour_url` TEXT NOT NULL ,
  `name` TEXT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `user_id` INT NOT NULL ,
  `tab_title` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `session_guid` (`session_guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`micro_behaviour_copy_update`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`micro_behaviour_copy_update` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`micro_behaviour_copy_update` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `guid` CHAR(22) NOT NULL ,
  `text_area_index` INT NOT NULL ,
  `value` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `guid` (`guid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`event_move_micro_behaviour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`event_move_micro_behaviour` ;

CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`event_move_micro_behaviour` (
  `key` INT NOT NULL AUTO_INCREMENT ,
  `session_guid` CHAR(22) NOT NULL ,
  `micro_behaviour_id` INT NOT NULL ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `macro_behaviour_name_at_event_time` MEDIUMTEXT NOT NULL ,
  `user_id` INT NOT NULL ,
  `up` BOOLEAN NOT NULL ,
  `text_area_updates_id` INT NOT NULL ,
  `containing_url` VARCHAR(2087) NULL ,
  `name_if_macro_behaviour` MEDIUMTEXT NULL ,
  PRIMARY KEY (`key`) )
ENGINE = InnoDB
COMMENT = 'A micro-behaviour has been moved up or down in a list';



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
