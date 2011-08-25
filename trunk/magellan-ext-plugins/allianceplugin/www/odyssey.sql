-- phpMyAdmin SQL Dump
-- version 3.3.7deb6
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 25. August 2011 um 13:57
-- Server Version: 5.0.51
-- PHP-Version: 5.2.6-1+lenny10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `odyssey`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `alliance`
--

CREATE TABLE IF NOT EXISTS `alliance` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(100) NOT NULL,
  `banner` varchar(255) default NULL,
  `email` varchar(100) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Daten für Tabelle `alliance`
--

INSERT INTO `alliance` (`id`, `name`, `banner`, `email`) VALUES
(1, 'Beispielallianz', 'Beispielbanner ', 'beispiel-allianz@adresse.de');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `alliance_faction_relation`
--

CREATE TABLE IF NOT EXISTS `alliance_faction_relation` (
  `alliance_id` int(11) NOT NULL,
  `faction_id` int(11) NOT NULL,
  `state` varchar(10) NOT NULL,
  UNIQUE KEY `alliance_id` (`alliance_id`,`faction_id`),
  KEY `alliance_faction_relation_FK_2` (`faction_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `alliance_faction_relation`
--

INSERT INTO `alliance_faction_relation` (`alliance_id`, `faction_id`, `state`) VALUES
(1, 1, ''),
(1, 2, ''),
(1, 3, '');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `faction`
--

CREATE TABLE IF NOT EXISTS `faction` (
  `id` int(11) NOT NULL auto_increment,
  `eressea_id` varchar(50) default NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `banner` varchar(255) default NULL,
  `type` varchar(50) NOT NULL,
  `persons` int(11) default NULL,
  `heros` int(11) default NULL,
  `age` int(11) default NULL,
  `locale` varchar(2) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `eressea_id` (`eressea_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Daten für Tabelle `faction`
--

INSERT INTO `faction` (`id`, `eressea_id`, `name`, `email`, `banner`, `type`, `persons`, `heros`, `age`, `locale`) VALUES
(1, 'bsp', 'Beispielpartei', 'beispiel-partei@adresse.de', 'Beispielbanner', 'Meermenschen', NULL, NULL, NULL, 'DE');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `faction_relation`
--

CREATE TABLE IF NOT EXISTS `faction_relation` (
  `faction1_id` int(11) NOT NULL,
  `faction2_id` int(11) NOT NULL,
  `state` varchar(20) NOT NULL,
  UNIQUE KEY `faction1_id` (`faction1_id`,`faction2_id`),
  KEY `faction_relation_FK_2` (`faction2_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `faction_relation`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `island`
--

CREATE TABLE IF NOT EXISTS `island` (
  `id` int(11) NOT NULL auto_increment,
  `mappart_id` int(11) NOT NULL,
  `name` varchar(100) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `mappart_id` (`mappart_id`,`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Daten für Tabelle `island`
--

INSERT INTO `island` (`id`, `mappart_id`, `name`) VALUES
(1, 1, '0');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `itemtype`
--

CREATE TABLE IF NOT EXISTS `itemtype` (
  `id` int(11) NOT NULL auto_increment,
  `type` varchar(20) NOT NULL,
  `name` varchar(100) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Daten für Tabelle `itemtype`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `map`
--

CREATE TABLE IF NOT EXISTS `map` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `lastchange` datetime NOT NULL,
  `alliance_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `map_FK_1` (`alliance_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Daten für Tabelle `map`
--

INSERT INTO `map` (`id`, `name`, `lastchange`, `alliance_id`) VALUES
(1, 'Eressea', '2011-08-25 12:20:24', 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `map_part`
--

CREATE TABLE IF NOT EXISTS `map_part` (
  `id` int(11) NOT NULL auto_increment,
  `map_id` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `lastchange` datetime NOT NULL,
  `round` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`,`round`),
  KEY `map_part_FK_1` (`map_id`),
  KEY `version` (`version`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Daten für Tabelle `map_part`
--

INSERT INTO `map_part` (`id`, `map_id`, `version`, `name`, `path`, `lastchange`, `round`) VALUES
(1, 1, 6, 'Eressea 2. Zeitalter', 'beispiel.cr.bz2', '2011-08-25 12:20:24', 736);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `region`
--

CREATE TABLE IF NOT EXISTS `region` (
  `id` int(11) NOT NULL auto_increment,
  `mappart_id` int(11) NOT NULL,
  `round` int(11) NOT NULL,
  `island_id` int(11) default NULL,
  `date` int(11) default NULL,
  `coordinate_x` int(11) NOT NULL,
  `coordinate_y` int(11) NOT NULL,
  `name` varchar(100) default NULL,
  `description` varchar(2048) default NULL,
  `type` varchar(30) NOT NULL,
  `trees` int(11) default NULL,
  `sprouts` int(11) default NULL,
  `mallorn` int(11) default NULL,
  `iron` int(11) default NULL,
  `laen` int(11) default NULL,
  `peasants` int(11) default NULL,
  `silver` int(11) default NULL,
  `horses` int(11) default NULL,
  `stones` int(11) default NULL,
  `wage` int(11) default NULL,
  `herb` int(11) default NULL,
  `herbs` int(11) default NULL,
  `recruits` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `mappart_id` (`mappart_id`,`round`,`coordinate_x`,`coordinate_y`),
  KEY `region_FK_2` (`island_id`),
  KEY `region_FK_3` (`herb`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Daten für Tabelle `region`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `region_faction_relation`
--

CREATE TABLE IF NOT EXISTS `region_faction_relation` (
  `region_id` int(11) NOT NULL,
  `faction_id` int(11) NOT NULL,
  `state` varchar(20) NOT NULL,
  UNIQUE KEY `region_id` (`region_id`,`faction_id`),
  KEY `region_faction_relation_FK_2` (`faction_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `region_faction_relation`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `region_item_relation`
--

CREATE TABLE IF NOT EXISTS `region_item_relation` (
  `region_id` int(11) NOT NULL,
  `itemtype_id` int(11) NOT NULL,
  `state` varchar(20) NOT NULL,
  `amount` int(11) default NULL,
  UNIQUE KEY `region_id` (`region_id`,`itemtype_id`),
  KEY `region_item_relation_FK_2` (`itemtype_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `region_item_relation`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL auto_increment,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `faction_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `user_FK_1` (`faction_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Daten für Tabelle `user`
--

INSERT INTO `user` (`id`, `username`, `password`, `faction_id`) VALUES
(1, 'beispiel-partei@adresse.de', 'password-md5', 1);
