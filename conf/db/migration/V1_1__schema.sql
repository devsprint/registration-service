DROP TABLE IF EXISTS developers;

CREATE TABLE developers (
  id UUID,
  first_name VARCHAR(256) NOT NULL,
  last_name VARCHAR(256) NOT NULL,
  birth_year INTEGER NOT NULL,
  gender VARCHAR(6) NOT NULL,
  address_street VARCHAR(256) NOT NULL,
  address_street_number VARCHAR(8) NOT NULL,
  address_other VARCHAR(256),
  address_city VARCHAR (256) NOT NULL,
  address_zip_code VARCHAR(256) NOT NULL,
  address_country VARCHAR(256) NOT NULL,
  phone_number VARCHAR(50) NOT NULL,
  skills VARCHAR(2048) NOT NULL
);

ALTER TABLE developers ADD CONSTRAINT developers_id PRIMARY KEY(id);