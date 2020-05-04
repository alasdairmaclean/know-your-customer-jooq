CREATE TABLE ACCOUNT (
  ACCOUNT_ID VARCHAR(36) NOT NULL,
  ACCOUNT_NUMBER INTEGER NOT NULL,
  PRIMARY KEY (ACCOUNT_ID)
);
CREATE UNIQUE INDEX IDX_ACCOUNT_ACCOUNT_NUMBER ON ACCOUNT (ACCOUNT_NUMBER);

CREATE TABLE CUSTOMER (
  CUSTOMER_ID VARCHAR(36) NOT NULL,
  DATE_OF_BIRTH DATE NOT NULL,
  FORENAME VARCHAR(100) NOT NULL,
  SURNAME VARCHAR(100) NOT NULL,
  PRIMARY KEY (CUSTOMER_ID)
);
CREATE INDEX IDX_CUSTOMER_DATE_OF_BIRTH ON CUSTOMER (DATE_OF_BIRTH);
CREATE INDEX IDX_CUSTOMER_FORENAME ON CUSTOMER (FORENAME);
CREATE INDEX IDX_CUSTOMER_SURNAME ON CUSTOMER (SURNAME);

CREATE TABLE CUSTOMER_ACCOUNT (
  CUSTOMER_ID VARCHAR(36) NOT NULL,
  ACCOUNT_ID VARCHAR(36) NOT NULL,
  PRIMARY KEY (CUSTOMER_ID, ACCOUNT_ID)
);

CREATE INDEX IDX_CUSTOMER_ACCOUNT_CUSTOMER_ID ON CUSTOMER_ACCOUNT (CUSTOMER_ID);
CREATE INDEX IDX_CUSTOMER_ACCOUNT_ACCOUNT_ID ON CUSTOMER_ACCOUNT (ACCOUNT_ID);