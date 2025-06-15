DROP TABLE IF EXISTS "public"."codestore" CASCADE;
DROP TABLE IF EXISTS "public"."currency" CASCADE;
DROP TABLE IF EXISTS "public"."customer" CASCADE;
DROP TABLE IF EXISTS "public"."employee" CASCADE;
DROP TABLE IF EXISTS "public"."city" CASCADE;
DROP TABLE IF EXISTS "public"."orderelement" CASCADE;
DROP TABLE IF EXISTS "public"."orders" CASCADE;
DROP TABLE IF EXISTS "public"."permission" CASCADE;
DROP TABLE IF EXISTS "public"."address" CASCADE;
DROP TABLE IF EXISTS "public"."loginuser" CASCADE;
DROP TABLE IF EXISTS "public"."role" CASCADE;
DROP TABLE IF EXISTS "public"."product" CASCADE;
DROP TABLE IF EXISTS "public"."supplier" CASCADE;
DROP TABLE IF EXISTS "public"."orders_orderelement" CASCADE;
DROP TABLE IF EXISTS "public"."permission_role" CASCADE;
DROP TABLE IF EXISTS "public"."roles_permissions" CASCADE;
DROP TABLE IF EXISTS "public"."databasechangelog" CASCADE;
DROP TABLE IF EXISTS "public"."databasechangeloglock" CASCADE;
CREATE TABLE "public"."codestore" (
                                      "deletable" BOOLEAN NOT NULL,
                                      "deleted" BIGINT NOT NULL DEFAULT 0,
                                      "id" SERIAL,
                                      "parentcodestore_codestore_id" BIGINT NULL,
                                      "linkname" VARCHAR(255) NOT NULL,
                                      "name" VARCHAR(255) NOT NULL,
                                      CONSTRAINT "codestore_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."currency" (
                                     "validdate" DATE NOT NULL DEFAULT CURRENT_DATE,
                                     "basecurrency_codestore_id" SERIAL,
                                     "deleted" BIGINT NOT NULL DEFAULT 0,
                                     "id" SERIAL,
                                     "ratejson" TEXT NOT NULL,
                                     CONSTRAINT "currency_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."customer" (
                                     "address_address_id" SERIAL,
                                     "deleted" BIGINT NOT NULL DEFAULT 0,
                                     "id" SERIAL,
                                     "emailaddress" VARCHAR(255) NOT NULL,
                                     "firstname" VARCHAR(255) NOT NULL,
                                     "lastname" VARCHAR(255) NOT NULL,
                                     CONSTRAINT "customer_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."employee" (
                                     "salary" INTEGER NOT NULL,
                                     "deleted" BIGINT NOT NULL DEFAULT 0,
                                     "id" SERIAL,
                                     "user_loginuser_id" SERIAL,
                                     "firstname" VARCHAR(255) NOT NULL,
                                     "lastname" VARCHAR(255) NOT NULL,
                                     CONSTRAINT "employee_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."city" (
                                 "countrycode_codestore_id" SERIAL,
                                 "deleted" BIGINT NOT NULL DEFAULT 0,
                                 "id" SERIAL,
                                 "name" VARCHAR(255) NOT NULL,
                                 "zipcode" VARCHAR(255) NOT NULL,
                                 CONSTRAINT "city_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."orderelement" (
                                         "grossprice" INTEGER NOT NULL,
                                         "netprice" INTEGER NOT NULL,
                                         "taxprice" DOUBLE PRECISION NOT NULL,
                                         "unit" INTEGER NOT NULL,
                                         "unitnetprice" INTEGER NOT NULL,
                                         "customer_customer_id" BIGINT NULL,
                                         "deleted" BIGINT NOT NULL DEFAULT 0,
                                         "id" SERIAL,
                                         "product_product_id" SERIAL,
                                         "supplier_supplier_id" BIGINT NULL,
                                         "taxkey_codestore_id" SERIAL,
                                         CONSTRAINT "orderelement_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."orders" (
                                   "currency_codestore_id" SERIAL,
                                   "customer_customer_id" BIGINT NULL,
                                   "deleted" BIGINT NOT NULL DEFAULT 0,
                                   "id" SERIAL,
                                   "paymenttype_codestore_id" SERIAL,
                                   "state_codestore_id" SERIAL,
                                   "supplier_supplier_id" BIGINT NULL,
                                   "timeoforder" TIMESTAMP NOT NULL,
                                   CONSTRAINT "orders_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."permission" (
                                       "deleted" BIGINT NOT NULL DEFAULT 0,
                                       "id" SERIAL,
                                       "name" VARCHAR(255) NOT NULL,
                                       CONSTRAINT "permission_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."address" (
                                    "city_city_id" SERIAL,
                                    "countrycode_codestore_id" SERIAL,
                                    "deleted" BIGINT NOT NULL DEFAULT 0,
                                    "id" SERIAL,
                                    "streettype_codestore_id" SERIAL,
                                    "housenumber" VARCHAR(255) NOT NULL,
                                    "streetname" VARCHAR(255) NOT NULL,
                                    CONSTRAINT "address_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."loginuser" (
                                      "enabled" BOOLEAN NOT NULL,
                                      "deleted" BIGINT NOT NULL DEFAULT 0,
                                      "id" SERIAL,
                                      "role_role_id" SERIAL,
                                      "passwordhash" VARCHAR(255) NULL,
                                      "username" VARCHAR(255) NULL,
                                      CONSTRAINT "loginuser_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."role" (
                                 "deleted" BIGINT NOT NULL DEFAULT 0,
                                 "id" SERIAL,
                                 "name" VARCHAR(255) NOT NULL,
                                 CONSTRAINT "role_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."product" (
                                    "amount" DOUBLE PRECISION NOT NULL,
                                    "buyingpricenet" DOUBLE PRECISION NOT NULL,
                                    "sellingpricenet" DOUBLE PRECISION NOT NULL,
                                    "packingunit_codestore_id" SERIAL,
                                    "buyingpricecurrency_codestore_id" SERIAL,
                                    "deleted" BIGINT NOT NULL DEFAULT 0,
                                    "id" SERIAL,
                                    "sellingpricecurrency_codestore_id" SERIAL,
                                    "taxkey_codestore_id" SERIAL,
                                    "name" VARCHAR(255) NOT NULL,
                                    CONSTRAINT "product_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."supplier" (
                                     "address_address_id" SERIAL,
                                     "deleted" BIGINT NOT NULL DEFAULT 0,
                                     "id" SERIAL,
                                     "name" VARCHAR(255) NOT NULL,
                                     CONSTRAINT "supplier_pkey" PRIMARY KEY ("id")
);
CREATE TABLE "public"."orders_orderelement" (
                                                "order_id" SERIAL,
                                                "orderelements_id" SERIAL,
                                                CONSTRAINT "orders_orderelement_pkey" PRIMARY KEY ("orderelements_id")
);
CREATE TABLE "public"."permission_role" (
                                            "permission_id" SERIAL,
                                            "roles_id" SERIAL,
                                            CONSTRAINT "permission_role_pkey" PRIMARY KEY ("permission_id", "roles_id")
);
CREATE TABLE "public"."roles_permissions" (
                                              "permission_id" SERIAL,
                                              "role_id" SERIAL,
                                              CONSTRAINT "roles_permissions_pkey" PRIMARY KEY ("permission_id", "role_id")
);
CREATE TABLE "public"."databasechangelog" (
                                              "id" VARCHAR(255) NOT NULL,
                                              "author" VARCHAR(255) NOT NULL,
                                              "filename" VARCHAR(255) NOT NULL,
                                              "dateexecuted" TIMESTAMP NOT NULL,
                                              "orderexecuted" INTEGER NOT NULL,
                                              "exectype" VARCHAR(10) NOT NULL,
                                              "md5sum" VARCHAR(35) NULL,
                                              "description" VARCHAR(255) NULL,
                                              "comments" VARCHAR(255) NULL,
                                              "tag" VARCHAR(255) NULL,
                                              "liquibase" VARCHAR(20) NULL,
                                              "contexts" VARCHAR(255) NULL,
                                              "labels" VARCHAR(255) NULL,
                                              "deployment_id" VARCHAR(10) NULL
);
CREATE TABLE "public"."databasechangeloglock" (
                                                  "id" INTEGER NOT NULL,
                                                  "locked" BOOLEAN NOT NULL,
                                                  "lockgranted" TIMESTAMP NULL,
                                                  "lockedby" VARCHAR(255) NULL,
                                                  CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
);
ALTER TABLE "public"."codestore" ADD CONSTRAINT "fkai5wk8uuktytntw296bbx30l6" FOREIGN KEY ("parentcodestore_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."currency" ADD CONSTRAINT "fk8ipu1t97egiq5lhjihyq8q7di" FOREIGN KEY ("basecurrency_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."customer" ADD CONSTRAINT "fkqv2o76v8od3e5nb3e118676j1" FOREIGN KEY ("address_address_id") REFERENCES "public"."address" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."employee" ADD CONSTRAINT "fkcegx1ofh7novc2hc08bvh71f3" FOREIGN KEY ("user_loginuser_id") REFERENCES "public"."loginuser" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."city" ADD CONSTRAINT "fkfc976p0k9ca5kd0ukubkpt85i" FOREIGN KEY ("countrycode_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orderelement" ADD CONSTRAINT "fkqn074x5d3mvr2yyxbmklwbl4j" FOREIGN KEY ("customer_customer_id") REFERENCES "public"."customer" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orderelement" ADD CONSTRAINT "fkanhoie8u7pvjknwua9anqpu88" FOREIGN KEY ("product_product_id") REFERENCES "public"."product" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orderelement" ADD CONSTRAINT "fkppd5pwt69heafrwyan4ttsrj2" FOREIGN KEY ("supplier_supplier_id") REFERENCES "public"."supplier" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orderelement" ADD CONSTRAINT "fkkqn7j0n7trxb6888babgveqf9" FOREIGN KEY ("taxkey_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders" ADD CONSTRAINT "fkeu261rk5ain8mea9pspqxvlfs" FOREIGN KEY ("currency_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders" ADD CONSTRAINT "fkbor3ekq2jeoy3evc2pauqjijc" FOREIGN KEY ("customer_customer_id") REFERENCES "public"."customer" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders" ADD CONSTRAINT "fkpj0sw9ypdlsa3gi2ix1xyn9vu" FOREIGN KEY ("paymenttype_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders" ADD CONSTRAINT "fk90gkjv12q4vupeu3s99af1wk2" FOREIGN KEY ("state_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders" ADD CONSTRAINT "fk160ohewchn6b928olqgc1s8ax" FOREIGN KEY ("supplier_supplier_id") REFERENCES "public"."supplier" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."address" ADD CONSTRAINT "fkb0x23bk9qu5dlnfq1jqa9rd1q" FOREIGN KEY ("city_city_id") REFERENCES "public"."city" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."address" ADD CONSTRAINT "fkayrykt91hnih8a18mf0wluyw7" FOREIGN KEY ("countrycode_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."address" ADD CONSTRAINT "fkt94ghcs4cpl0bdypyc2mhisa0" FOREIGN KEY ("streettype_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."loginuser" ADD CONSTRAINT "fk892riipyya5iucf4eroyvyijt" FOREIGN KEY ("role_role_id") REFERENCES "public"."role" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."product" ADD CONSTRAINT "fkig1cgusdeokyu9gl8ti3l4pf2" FOREIGN KEY ("packingunit_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."product" ADD CONSTRAINT "fk7a4f2emy0uj1qdi66log9isro" FOREIGN KEY ("buyingpricecurrency_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."product" ADD CONSTRAINT "fkko49qtapgcbuf7c5lkh98jkyh" FOREIGN KEY ("sellingpricecurrency_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."product" ADD CONSTRAINT "fkdgtgmvh79i15cv6mc0yqa4oys" FOREIGN KEY ("taxkey_codestore_id") REFERENCES "public"."codestore" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."supplier" ADD CONSTRAINT "fk617aiamurgon6t6l9khr6eysu" FOREIGN KEY ("address_address_id") REFERENCES "public"."address" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders_orderelement" ADD CONSTRAINT "fkatry1jvpdrf9katyy1u5ch91o" FOREIGN KEY ("order_id") REFERENCES "public"."orders" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."orders_orderelement" ADD CONSTRAINT "fkdyruihskr5l2puetvd0x4tqv8" FOREIGN KEY ("orderelements_id") REFERENCES "public"."orderelement" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."permission_role" ADD CONSTRAINT "fkmpmbkgtlo4vvlvc0056ng225r" FOREIGN KEY ("roles_id") REFERENCES "public"."role" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."permission_role" ADD CONSTRAINT "fkhx4216a5jo1fvqo6h3692ja41" FOREIGN KEY ("permission_id") REFERENCES "public"."permission" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."roles_permissions" ADD CONSTRAINT "fkwq1nbrf9xb1qhttmk7ahtyte" FOREIGN KEY ("permission_id") REFERENCES "public"."permission" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."roles_permissions" ADD CONSTRAINT "fkgmkdrle9p0lgnp6pbwm7mpihb" FOREIGN KEY ("role_id") REFERENCES "public"."role" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
