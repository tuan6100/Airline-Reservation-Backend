create table country
(
    country_id   integer default nextval('"Country_country_id_seq"'::regclass) not null
        constraint "Country_pkey"
            primary key,
    country_name varchar                                                       not null
        constraint "Country_country_name_key"
            unique,
    abbreviation varchar(2)                                                    not null
        constraint "Country_abbreviation_key"
            unique,
    continent    varchar                                                       not null,
    gmt          integer                                                       not null
);

create table city
(
    city_id    integer default nextval('"City_city_id_seq"'::regclass) not null
        constraint "City_pkey"
            primary key,
    city_name  varchar                                                 not null
        constraint "City_city_name_key"
            unique,
    country_id integer                                                 not null
        constraint "City_country_id_fkey"
            references country
);

create table airline
(
    airline_id            integer default nextval('"Airline_airline_id_seq"'::regclass) not null
        constraint "Airline_pkey"
            primary key,
    airline_name          varchar                                                       not null
        constraint "Airline_airline_name_key"
            unique,
    city_id               integer                                                       not null
        constraint "Airline_city_id_fkey"
            references city,
    address               varchar                                                       not null,
    website               varchar                                                       not null,
    max_tickets_per_order integer
);

create table airport
(
    airport_id   integer default nextval('"Airport_airport_id_seq"'::regclass) not null
        constraint "Airport_pkey"
            primary key,
    airport_name varchar                                                       not null
        constraint "Airport_airport_name_key"
            unique,
    city_id      integer                                                       not null
        constraint "Airport_city_id_fkey"
            references city,
    address      varchar                                                       not null
);

create table aircraft
(
    aircraft_id   integer default nextval('"Aircraft_aircraft_id_seq"'::regclass) not null
        constraint "Aircraft_pkey"
            primary key,
    aircraft_name varchar                                                         not null
        constraint "Aircraft_aircraft_name_key"
            unique,
    airline_id    integer                                                         not null
        constraint "Aircraft_airline_id_fkey"
            references airline,
    seat_count    integer                                                         not null
);

create table voucher
(
    voucher_id       integer default nextval('"Voucher_voucher_id_seq"'::regclass) not null
        constraint "Voucher_pkey"
            primary key,
    voucher_name     varchar                                                       not null
        constraint "Voucher_voucher_name_key"
            unique,
    discount_percent integer default 0                                             not null
        constraint "Voucher_discount_percent_check"
            check ((discount_percent >= 0) AND (discount_percent <= 100)),
    amount           integer                                                       not null
        constraint "Voucher_amount_check"
            check (amount >= 0),
    start_time       timestamp                                                     not null,
    end_time         timestamp                                                     not null,
    constraint "Voucher_check"
        check (end_time > start_time)
);

create table customer_auth
(
    customer_id  integer     not null
        constraint "Customer_Auth_pkey"
            primary key,
    phone_number varchar(10) not null,
    password     varchar     not null
        constraint "Customer_Auth_password_key"
            unique
);

create table flight_route
(
    route_id          integer default nextval('"FlightRoute_route_id_seq"'::regclass) not null
        constraint "FlightRoute_pkey"
            primary key,
    departure_airport integer                                                         not null
        constraint "FlightRoute_departure_airport_fkey"
            references airport,
    arrival_airport   integer                                                         not null
        constraint "FlightRoute_arrival_airport_fkey"
            references airport
);

create index idx_flight_route_airports
    on flight_route (departure_airport, arrival_airport);

create table flight
(
    flight_id                 integer default nextval('"Flight_flight_id_seq"'::regclass) not null,
    route_id                  integer                                                     not null
        constraint "Flight_route_id_fkey"
            references flight_route,
    aircraft_id               integer                                                     not null
        constraint "Flight_aircraft_id_fkey"
            references aircraft,
    departure_time            timestamp                                                   not null,
    estimated_flight_duration time,
    constraint "Flight_pkey"
        primary key (flight_id, departure_time)
)
    partition by RANGE (departure_time);

CREATE OR REPLACE PROCEDURE create_monthly_flight_partitions(
    start_date DATE,
    end_date DATE
) LANGUAGE plpgsql AS $$
DECLARE
    current_date_var DATE := start_date;
    next_date DATE;
    partition_name TEXT;
    sql_command TEXT;
BEGIN
    WHILE current_date_var <= end_date LOOP
            next_date := current_date_var + INTERVAL '1 month';
            partition_name := 'flight_' || TO_CHAR(current_date_var, 'YYYYMM');
            sql_command := 'CREATE TABLE IF NOT EXISTS "' || partition_name || '" PARTITION OF "flight" ' ||
                           'FOR VALUES FROM (''' || current_date_var || ' 00:00:00'') TO (''' || next_date || ' 00:00:00'');';
            EXECUTE sql_command;
            current_date_var := next_date;
        END LOOP;
END;
$$;

CALL create_monthly_flight_partitions(
                CURRENT_DATE,
                (CURRENT_DATE + INTERVAL '12 months')::DATE
);

create index idx_flight_route_id
    on flight (route_id);

create index idx_flight_departure_time
    on flight (departure_time, estimated_flight_duration);

create table seat_class
(
    seat_class_id   integer default nextval('"SeatClass_seat_class_id_seq"'::regclass) not null
        constraint "SeatClass_pkey"
            primary key,
    seat_class_name varchar                                                            not null,
    airline_id      integer                                                            not null
        constraint "SeatClass_airline_id_fkey"
            references airline,
    price           integer                                                            not null
        constraint "SeatClass_price_check"
            check (price >= 0),
    constraint seatclass_seat_class_name_airline_unique
        unique (seat_class_name, airline_id)
);


create table customer
(
    customer_id bigint generated by default as identity
        primary key,
    country_id  bigint,
    email       varchar(255),
    full_name   varchar(255) not null,
    gender      varchar(255) not null
);

create table booking
(
    booking_id            varchar(255) not null
        constraint booking_pkey1
            primary key,
    created_at            timestamp(6),
    currency              varchar(255),
    customer_id           bigint,
    expires_at            timestamp(6),
    flight_departure_time timestamp(6),
    flight_id             bigint,
    seat_count            integer,
    status                varchar(255)
        constraint booking_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'EXPIRED'::character varying])::text[])),
    ticket_count          integer,
    total_amount          double precision,
    updated_at            timestamp(6),
    version               integer
);

create table seat
(
    seat_id       bigint generated by default as identity
        primary key,
    aircraft_id   bigint       not null,
    seat_code     varchar(255) not null,
    status        varchar(255)
        constraint seat_status_check
            check ((status)::text = ANY
                   ((ARRAY ['AVAILABLE'::character varying, 'ON_HOLD'::character varying, 'RESERVED'::character varying])::text[])),
    version       integer,
    seat_class_id bigint
        constraint fku0t0tn8wto9nuyqm39qwbeer
            references seat_class (seat_class_id) ,
    updated_at    timestamp(6)
);

create table ticket
(
    ticket_id             bigint generated by default as identity
        primary key,
    booking_id            varchar(255),
    created_at            timestamp(6) not null,
    flight_departure_time timestamp(6),
    flight_id             bigint       not null,
    status                smallint
        constraint ticket_status_check
            check ((status >= 0) AND (status <= 3)),
    ticket_code           uuid,
    updated_at            timestamp(6),
    version               integer,
    seat_id               bigint
        constraint fkrb89nimf0wy995rkhigasnq0w
            references seat,
    foreign key ("flight_id", "flight_departure_time") references flight(flight_id, departure_time)
);


create table order_item
(
    id          bigint generated by default as identity
        constraint order_item_pkey1
            primary key,
    currency    varchar(255),
    description varchar(255),
    flight_id   bigint,
    order_id    bigint,
    price       numeric(38, 2),
    seat_id     bigint,
    ticket_id   bigint
);

create table ticket_order
(
    order_id       bigint generated by default as identity
        primary key,
    booking_id     varchar(255),
    created_at     timestamp(6),
    currency       varchar(255),
    customer_id    bigint,
    payment_status varchar(255),
    promotion_id   bigint,
    status         varchar(255),
    total_amount   numeric(38, 2),
    updated_at     timestamp(6),
    version        integer
);

create table booked_ticket
(
    ticket_id bigint not null
        primary key,
    order_id  bigint not null
        constraint fk52x4fmhanhf1gl1lg223nts8w
            references ticket_order
);

create table invoice
(
    invoice_id   bigint generated by default as identity
        primary key,
    issue_date   timestamp(6) not null,
    order_id     bigint       not null
        constraint fkh2ruo9508vybbtrloqal9nu9f
            references ticket_order,
    total_amount bigint       not null
);

create table payment
(
    payment_id     bigint generated by default as identity
        primary key,
    amount         bigint       not null,
    invoice_id     bigint       not null
        constraint fkdpyta813lofdsu8dlhsybxtdc
            references invoice,
    payment_date   timestamp(6) not null,
    payment_method varchar(255) not null
);


