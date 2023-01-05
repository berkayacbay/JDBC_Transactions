create table if not exists sample(
    id serial PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    data text,
    value int default 0
);

create FUNCTION sample_trigger() RETURNS TRIGGER AS
'
    BEGIN
        IF (SELECT value FROM sample where id = NEW.id ) > 1000
           THEN
           RAISE SQLSTATE ''23503'';
           END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;





create TRIGGER sample_value AFTER insert ON sample
    FOR EACH ROW EXECUTE PROCEDURE sample_trigger();






       create table if not exists zip_city(
           zip int PRIMARY KEY,
           city VARCHAR(80)
           );







         create table if not exists company (
           name VARCHAR (80) PRIMARY KEY,
           country VARCHAR(80) not null,
           zip int not null,
           street VARCHAR(80) not null ,
           phone VARCHAR(80)  UNIQUE NOT NULl

           );

		create table if not exists product(
                                      id SERIAL PRIMARY KEY,
                                      brandName VARCHAR(64),
          								name VARCHAR(64) NOT NULL,
                                      description VARCHAR(64)

	);

        create table if not exists production(
             produceId serial PRIMARY KEY,
             company VARCHAR(80) NOT NULL,
             product_id int NOT NULL,
             capacity int,
         foreign key (company) references company(name),
         foreign key(product_id) references product(id)
        );



             create table if not exists product_order(
            id serial PRIMARY KEY,
            company VARCHAR(80) NOT NULL,
            product_id int NOT NULL,
            amount int,
            order_date timestamp with time zone,
            foreign key(company) references company(name),
            foreign key(product_id) references product(id)
           );




           create table if not exists emails(
           name VARCHAR(80),
           email VARCHAR(80),
           primary key(name,email),
           foreign key(name) references company (name));


           create table if not exists transaction_history(
               id serial,
                  key VARCHAR(100),
                 value int,
               amount int,
               order_date timestamp with time zone
           );









CREATE FUNCTION ordered_trigger() RETURNS TRIGGER AS
'
    BEGIN
        IF (Select capacity
            FROM production p1
            WHERE p1.product_id=NEW.product_id AND p1.company=NEW.company ) < (SELECT SUM(amount)
                                   FROM product_order o1
                                   WHERE o1.company=NEW.company AND o1.product_id=NEW.product_id
                )
        THEN
            RAISE SQLSTATE ''23503'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER order_amount AFTER insert ON product_order
    FOR EACH ROW EXECUTE PROCEDURE ordered_trigger();