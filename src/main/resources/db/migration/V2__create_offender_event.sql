DROP TABLE IF EXISTS OFFENDER_EVENT;

CREATE TABLE OFFENDER_EVENT
(
  OFFENDER_EVENT_ID               BIGSERIAL       NOT NULL,
  OFFENDER_NO                     VARCHAR( 10)    NOT NULL,
  EVENT_ID                        BIGINT          NOT NULL,
  EVENT_DATE                      DATE            NOT NULL,
  EVENT_TYPE                      VARCHAR(20)     NOT NULL,
  PERIOD                          VARCHAR(2)      NOT NULL,
  CURRENT_LOCATION                CHAR(1),
  PRISON_ID                       VARCHAR(6)      NOT NULL
);

COMMENT ON TABLE OFFENDER_EVENT IS 'Records the current (or last) activity for an offender';


CREATE INDEX OFFENDER_EVENT_ON_IDX ON OFFENDER_EVENT (OFFENDER_NO, EVENT_DATE);
CREATE UNIQUE INDEX OFFENDER_EVENT_EI_IDX ON OFFENDER_EVENT (EVENT_ID, EVENT_TYPE);
CREATE INDEX OFFENDER_EVENT_AI_IDX ON OFFENDER_EVENT (PRISON_ID);