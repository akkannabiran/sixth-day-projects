SELECT ID AS STORE_ID,
  STORE_NO,
  NAME,
  ADDRESS1,
  ADDRESS2,
  CITY,
  STATE,
  ZIP_CODE,
  PHONE_NUMBER,
  STORE_HOURS,
  FLG_DISPLAY,
  S2S_ELIGIBLE,
  STORE_DESC
FROM NM_STORE_LOCATION
WHERE STORE_TYPE='standard'
ORDER BY ID