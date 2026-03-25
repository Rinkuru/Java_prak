CREATE TABLE IF NOT EXISTS person (
  id                BIGSERIAL       PRIMARY KEY,
  full_name         VARCHAR(255)    NOT NULL,
  home_address      VARCHAR(500),
  education         VARCHAR(255)    NOT NULL,
  status            BOOLEAN         NOT NULL,  -- ищет работу или нет
  desired_position  VARCHAR(255),
  desired_salary    NUMERIC(12, 2)
);

CREATE TABLE IF NOT EXISTS company (
  id          BIGSERIAL     PRIMARY KEY,
  name        VARCHAR(255)  NOT NULL UNIQUE,
  description TEXT
);

CREATE TABLE IF NOT EXISTS vacancy (
  id                  BIGSERIAL     PRIMARY KEY,
  company_id          BIGINT        NOT NULL,
  position            VARCHAR(255)  NOT NULL,
  salary              NUMERIC(12, 2) NOT NULL,
  required_education  VARCHAR(255),
  requirements        TEXT,
  status              BOOLEAN       NOT NULL,  -- открыта или закрыта

  FOREIGN KEY (company_id) REFERENCES company (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS work_experience (
  id          BIGSERIAL     PRIMARY KEY,
  person_id   BIGINT        NOT NULL,
  company_id  BIGINT        NOT NULL,
  position    VARCHAR(255)  NOT NULL,
  salary      NUMERIC(12, 2) NOT NULL,
  start_date  DATE          NOT NULL,
  end_date    DATE,

  FOREIGN KEY (person_id) REFERENCES person (id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (company_id) REFERENCES company (id) ON UPDATE CASCADE ON DELETE RESTRICT
);
