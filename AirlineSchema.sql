CREATE TABLE "Country" (
                           "country_id" SERIAL PRIMARY KEY ,
                           "country_name" VARCHAR UNIQUE NOT NULL,
                           "abbreviation" VARCHAR(2) UNIQUE NOT NULL,
                           "continent" VARCHAR NOT NULL,
                           "gmt" INTEGER NOT NULL
);

CREATE TABLE "City" (
                        "city_id" SERIAL PRIMARY KEY,
                        "city_name" VARCHAR UNIQUE NOT NULL,
                        "country_id" INTEGER NOT NULL REFERENCES "Country" ("country_id")
);

CREATE TABLE "Airline" (
                           "airline_id" SERIAL PRIMARY KEY,
                           "airline_name" VARCHAR UNIQUE NOT NULL,
                           "city_id" INTEGER NOT NULL REFERENCES "City" ("city_id"),
                           "address" VARCHAR NOT NULL,
                           "website" VARCHAR NOT NULL,
                           "max_tickets_per_order" INTEGER
);

CREATE TABLE "Airport" (
                           "airport_id" SERIAL PRIMARY KEY,
                           "airport_name" VARCHAR UNIQUE NOT NULL,
                           "city_id" INTEGER NOT NULL REFERENCES "City" ("city_id"),
                           "address" VARCHAR NOT NULL
);

CREATE TABLE "Aircraft" (
                            "aircraft_id" SERIAL PRIMARY KEY,
                            "aircraft_name" VARCHAR UNIQUE NOT NULL,
                            "airline_id" INTEGER NOT NULL REFERENCES "Airline" ("airline_id"),
                            "seat_count" INTEGER NOT NULL
);

CREATE TABLE "SeatClass" (
                             "class_id" SERIAL PRIMARY KEY,
                             "class_name" VARCHAR UNIQUE NOT NULL,
                             "aircraft_id" INTEGER NOT NULL REFERENCES "Aircraft" ("aircraft_id"),
                             "price" INTEGER NOT NULL CHECK ("price" >= 0)
);

CREATE TABLE "Seat" (
                        "seat_id" SERIAL PRIMARY KEY,
                        "class_id" INTEGER NOT NULL REFERENCES "SeatClass" ("class_id"),
                        "seat_code" VARCHAR NOT NULL,
                        "is_available" BOOLEAN DEFAULT TRUE
);

CREATE TABLE "FlightRoute" (
                               "route_id" SERIAL PRIMARY KEY,
                               "departure_airport" INTEGER NOT NULL REFERENCES "Airport" ("airport_id"),
                               "arrival_airport" INTEGER NOT NULL REFERENCES "Airport" ("airport_id")
);

CREATE TABLE "Flight" (
                          "flight_id" SERIAL PRIMARY KEY,
                          "route_id" INTEGER NOT NULL REFERENCES "FlightRoute" ("route_id"),
                          "aircraft_id" INTEGER NOT NULL REFERENCES "Aircraft" ("aircraft_id"),
                          "departure_time" TIMESTAMP NOT NULL,
                          "arrival_time" TIMESTAMP NOT NULL CHECK ("arrival_time" > "departure_time")
);

CREATE TABLE "Ticket" (
                          "ticket_id" SERIAL PRIMARY KEY,
                          "airline_id" INTEGER NOT NULL REFERENCES "Airline" ("airline_id"),
                          "flight_id" INTEGER NOT NULL REFERENCES "Flight" ("flight_id"),
                          "seat_id" INTEGER NOT NULL REFERENCES "Seat" ("seat_id"),
                          "is_booked" BOOLEAN NOT NULL DEFAULT FALSE,
                          "is_paid" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "Customer" (
                            "customer_id" SERIAL PRIMARY KEY,
                            "full_name" VARCHAR NOT NULL,
                            "id_number" VARCHAR UNIQUE NOT NULL,
                            "phone_number" VARCHAR NOT NULL,
                            "email" VARCHAR
);

CREATE TABLE "BookedTicket" (
                                "ticket_id" INTEGER REFERENCES "Ticket" ("ticket_id"),
                                "customer_id" INTEGER REFERENCES "Customer" ("customer_id"),
                                "order_id" INTEGER NOT NULL REFERENCES "TicketOrder" ("order_id"),
                                PRIMARY KEY ("ticket_id", "customer_id")
);

CREATE TABLE "TicketOrder" (
                               "order_id" SERIAL PRIMARY KEY,
                               "promotion_id" INTEGER REFERENCES "Promotion" ("promotion_id"),
                               "total_price" BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE "Promotion" (
                             "promotion_id" SERIAL PRIMARY KEY,
                             "promotion_name" VARCHAR UNIQUE NOT NULL,
                             "discount_percent" INTEGER NOT NULL DEFAULT 0 CHECK ("discount_percent" BETWEEN 0 AND 100),
                             "start_time" TIMESTAMP NOT NULL,
                             "end_time" TIMESTAMP NOT NULL CHECK ("end_time" > "start_time")
);

CREATE TABLE "Invoice" (
                           "invoice_id" SERIAL PRIMARY KEY,
                           "order_id" INTEGER NOT NULL REFERENCES "TicketOrder" ("order_id"),
                           "paid_amount" BIGINT NOT NULL DEFAULT 0,
                           "payment_date" TIMESTAMP,
                           "unpaid_amount" BIGINT NOT NULL DEFAULT 0,
                           "issue_date" TIMESTAMP NOT NULL
);

grant select on table pg_stat_bgwriter to tuan;
grant select on table pg_stat_activity to tuan;
grant select on table pg_stat_database to tuan;
grant select on table pg_stat_statements to tuan;