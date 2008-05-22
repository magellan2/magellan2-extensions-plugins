-----------------------------------------------------------------------------
-- report
-----------------------------------------------------------------------------
drop table report;

CREATE TABLE report
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    filename VARCHAR(1024) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (filename));

-----------------------------------------------------------------------------
-- building_statistics_data
-----------------------------------------------------------------------------
drop table building_statistics_data;

CREATE TABLE building_statistics_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    building_id BIGINT NOT NULL,
    turn INTEGER NOT NULL,
    name VARCHAR(50),
    description VARCHAR(255),
    size INTEGER,
    owner VARCHAR(20),
    inmates INTEGER,
    PRIMARY KEY(id),
    UNIQUE (building_id, turn));

-----------------------------------------------------------------------------
-- building_statistics
-----------------------------------------------------------------------------
drop table building_statistics;

CREATE TABLE building_statistics
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    report_id BIGINT NOT NULL,
    building_number VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (report_id, building_number));

-----------------------------------------------------------------------------
-- faction_statistics_data
-----------------------------------------------------------------------------
drop table faction_statistics_data;

CREATE TABLE faction_statistics_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    faction_id BIGINT NOT NULL,
    turn INTEGER NOT NULL,
    name VARCHAR(50),
    description VARCHAR(255),
    persons INTEGER,
    units INTEGER,
    race VARCHAR(50),
    heroes INTEGER,
    max_heroes INTEGER,
    max_migrants INTEGER,
    average_score INTEGER,
    score INTEGER,
    PRIMARY KEY(id),
    UNIQUE (faction_id, turn));

-----------------------------------------------------------------------------
-- faction_statistics
-----------------------------------------------------------------------------
drop table faction_statistics;

CREATE TABLE faction_statistics
(
    id NOT NULL GENERATED ALWAYS AS IDENTITY BIGINT ,
    report_id BIGINT NOT NULL,
    faction_number VARCHAR(20) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (report_id, faction_number));

-----------------------------------------------------------------------------
-- ship_statistics_data
-----------------------------------------------------------------------------
drop table ship_statistics_data;

CREATE TABLE ship_statistics_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    ship_id BIGINT NOT NULL,
    turn INTEGER NOT NULL,
    name VARCHAR(50),
    description VARCHAR(255),
    size INTEGER,
    owner VARCHAR(20),
    region VARCHAR(20),
    passengers INTEGER,
    max_cargo INTEGER,
    cargo INTEGER,
    capacity INTEGER,
    damage_ratio INTEGER,
    PRIMARY KEY(id),
    UNIQUE (ship_id, turn));

-----------------------------------------------------------------------------
-- ship_statistics
-----------------------------------------------------------------------------
drop table ship_statistics;

CREATE TABLE ship_statistics
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    report_id BIGINT NOT NULL,
    ship_number VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (report_id, ship_number));

-----------------------------------------------------------------------------
-- region_statistics_data
-----------------------------------------------------------------------------
drop table region_statistics_data;

CREATE TABLE region_statistics_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    region_id BIGINT NOT NULL,
    turn INTEGER NOT NULL,
    name VARCHAR(50),
    description VARCHAR(255),
    max_recruits INTEGER,
    max_luxuries INTEGER,
    max_entertain INTEGER,
    stones INTEGER,
    trees INTEGER,
    sprouts INTEGER,
    silver INTEGER,
    peasants INTEGER,
    inhabitants INTEGER,
    iron INTEGER,
    laen INTEGER,
    herb VARCHAR(50),
    PRIMARY KEY(id),
    UNIQUE (region_id, turn));

-----------------------------------------------------------------------------
-- region_statistics_ship_data
-----------------------------------------------------------------------------
drop table region_statistics_ship_data;

CREATE TABLE region_statistics_ship_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    turn_id BIGINT NOT NULL,
    ship_number VARCHAR(50) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (turn_id, ship_number));

-----------------------------------------------------------------------------
-- region_statistics_prices_data
-----------------------------------------------------------------------------
drop table region_statistics_prices_data;

CREATE TABLE region_statistics_prices_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    turn_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    luxury_item VARCHAR(50) NOT NULL,
    price INTEGER NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (turn_id, luxury_item));

-----------------------------------------------------------------------------
-- region_statistics_resources_data
-----------------------------------------------------------------------------
drop table region_statistics_resources_data;

CREATE TABLE region_statistics_resources_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    turn_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    skill_level INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (turn_id, item_type));

-----------------------------------------------------------------------------
-- region_statistics
-----------------------------------------------------------------------------
drop table region_statistics;

CREATE TABLE region_statistics
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    report_id BIGINT NOT NULL,
    region_number VARCHAR(20) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (report_id, region_number));

-----------------------------------------------------------------------------
-- unit_statistics_data
-----------------------------------------------------------------------------
drop table unit_statistics_data;

CREATE TABLE unit_statistics_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    unit_id BIGINT NOT NULL,
    turn INTEGER NOT NULL,
    name VARCHAR(50),
    description VARCHAR(255),
    persons INTEGER,
    faction VARCHAR(20),
    region VARCHAR(20),
    building VARCHAR(20),
    ship VARCHAR(20),
    race VARCHAR(50),
    weight INTEGER,
    aura INTEGER,
    health VARCHAR(20),
    hero INTEGER,
    guard INTEGER,
    PRIMARY KEY(id),
    UNIQUE (unit_id, turn));

-----------------------------------------------------------------------------
-- unit_statistics_skill_data
-----------------------------------------------------------------------------
drop table unit_statistics_skill_data;

CREATE TABLE unit_statistics_skill_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    turn_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    skill VARCHAR(50) NOT NULL,
    level INTEGER NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (turn_id, skill));

-----------------------------------------------------------------------------
-- unit_statistics_item_data
-----------------------------------------------------------------------------
drop table unit_statistics_item_data;

CREATE TABLE unit_statistics_item_data
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    turn_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (turn_id, item_type));

-----------------------------------------------------------------------------
-- unit_statistics
-----------------------------------------------------------------------------
drop table unit_statistics;

CREATE TABLE unit_statistics
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    report_id BIGINT NOT NULL,
    unit_number VARCHAR(20) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (report_id, unit_number));

ALTER TABLE building_statistics_data
    ADD CONSTRAINT building_statistics_data_FK_1 
    FOREIGN KEY (building_id)
    REFERENCES building_statistics (id)
;

ALTER TABLE building_statistics
    ADD CONSTRAINT building_statistics_FK_1 
    FOREIGN KEY (report_id)
    REFERENCES report (id)
;

ALTER TABLE faction_statistics_data
    ADD CONSTRAINT faction_statistics_data_FK_1 
    FOREIGN KEY (faction_id)
    REFERENCES faction_statistics (id)
;

ALTER TABLE faction_statistics
    ADD CONSTRAINT faction_statistics_FK_1 
    FOREIGN KEY (report_id)
    REFERENCES report (id)
;

ALTER TABLE ship_statistics_data
    ADD CONSTRAINT ship_statistics_data_FK_1 
    FOREIGN KEY (ship_id)
    REFERENCES ship_statistics (id)
;

ALTER TABLE ship_statistics
    ADD CONSTRAINT ship_statistics_FK_1 
    FOREIGN KEY (report_id)
    REFERENCES report (id)
;

ALTER TABLE region_statistics_data
    ADD CONSTRAINT region_statistics_data_FK_1 
    FOREIGN KEY (region_id)
    REFERENCES region_statistics (id)
;

ALTER TABLE region_statistics_ship_data
    ADD CONSTRAINT region_statistics_ship_data_FK_1 
    FOREIGN KEY (turn_id)
    REFERENCES region_statistics_data (id)
;

ALTER TABLE region_statistics_prices_data
    ADD CONSTRAINT region_statistics_prices_data_FK_1 
    FOREIGN KEY (turn_id)
    REFERENCES region_statistics_data (id)
;

ALTER TABLE region_statistics_prices_data
    ADD CONSTRAINT region_statistics_prices_data_FK_2 
    FOREIGN KEY (region_id)
    REFERENCES region_statistics (id)
;

ALTER TABLE region_statistics_resources_data
    ADD CONSTRAINT region_statistics_resources_data_FK_1 
    FOREIGN KEY (turn_id)
    REFERENCES region_statistics_data (id)
;

ALTER TABLE region_statistics_resources_data
    ADD CONSTRAINT region_statistics_resources_data_FK_2 
    FOREIGN KEY (region_id)
    REFERENCES region_statistics (id)
;

ALTER TABLE region_statistics
    ADD CONSTRAINT region_statistics_FK_1 
    FOREIGN KEY (report_id)
    REFERENCES report (id)
;

ALTER TABLE unit_statistics_data
    ADD CONSTRAINT unit_statistics_data_FK_1 
    FOREIGN KEY (unit_id)
    REFERENCES unit_statistics (id)
;

ALTER TABLE unit_statistics_skill_data
    ADD CONSTRAINT unit_statistics_skill_data_FK_1 
    FOREIGN KEY (turn_id)
    REFERENCES unit_statistics_data (id)
;

ALTER TABLE unit_statistics_skill_data
    ADD CONSTRAINT unit_statistics_skill_data_FK_2 
    FOREIGN KEY (unit_id)
    REFERENCES unit_statistics (id)
;

ALTER TABLE unit_statistics_item_data
    ADD CONSTRAINT unit_statistics_item_data_FK_1 
    FOREIGN KEY (turn_id)
    REFERENCES unit_statistics_data (id)
;

ALTER TABLE unit_statistics_item_data
    ADD CONSTRAINT unit_statistics_item_data_FK_2 
    FOREIGN KEY (unit_id)
    REFERENCES unit_statistics (id)
;

ALTER TABLE unit_statistics
    ADD CONSTRAINT unit_statistics_FK_1 
    FOREIGN KEY (report_id)
    REFERENCES report (id)
;

