--------------------------------------------------------
--  �t�@�C�����쐬���܂��� - �Ηj��-3��-24-2020   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table CART_ITEM_TABLE
--------------------------------------------------------

  CREATE TABLE "DEMOUSER"."CART_ITEM_TABLE" 
   (	"PRODUCT_ID" VARCHAR2(64 BYTE), 
	"PRODUCT_NAME" VARCHAR2(128 BYTE), 
	"PRICE" NUMBER, 
	"COUNT" NUMBER, 
	"CART_ID" VARCHAR2(64 BYTE), 
	"ID" VARCHAR2(160 BYTE)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index CART_ITEM_TABLE_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "DEMOUSER"."CART_ITEM_TABLE_PK" ON "DEMOUSER"."CART_ITEM_TABLE" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table CART_ITEM_TABLE
--------------------------------------------------------

  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("PRODUCT_ID" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("PRODUCT_NAME" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("PRICE" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("COUNT" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" MODIFY ("CART_ID" NOT NULL ENABLE);
  ALTER TABLE "DEMOUSER"."CART_ITEM_TABLE" ADD CONSTRAINT "CART_ITEM_TABLE_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;