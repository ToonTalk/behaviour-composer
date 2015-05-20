SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `behaviour_composer_schema2` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
SHOW WARNINGS;
USE `behaviour_composer_schema2`;



-- -----------------------------------------------------
-- Table `behaviour_composer_schema2`.`micro_behaviour_copy_update`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `behaviour_composer_schema2`.`micro_behaviour_copy_update` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `behaviour_composer_schema2`.`micro_behaviour_copy_update` (
  `index` INT NOT NULL AUTO_INCREMENT ,
  `guid` CHAR(22) NOT NULL ,
  `text_area_index` INT NOT NULL ,
  `value` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`index`) ,
  INDEX `guid` (`guid` ASC) )
ENGINE = InnoDB;

SHOW WARNINGS;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
