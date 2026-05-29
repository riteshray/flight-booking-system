-- Insert Sample Airports
INSERT INTO airport (code, name, city, country) VALUES ('JFK', 'John F. Kennedy International Airport', 'New York', 'USA');
INSERT INTO airport (code, name, city, country) VALUES ('LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA');
INSERT INTO airport (code, name, city, country) VALUES ('ORD', 'O''Hare International Airport', 'Chicago', 'USA');
INSERT INTO airport (code, name, city, country) VALUES ('LHR', 'London Heathrow Airport', 'London', 'UK');
INSERT INTO airport (code, name, city, country) VALUES ('CDG', 'Charles de Gaulle Airport', 'Paris', 'France');
INSERT INTO airport (code, name, city, country) VALUES ('NRT', 'Narita International Airport', 'Tokyo', 'Japan');
INSERT INTO airport (code, name, city, country) VALUES ('DXB', 'Dubai International Airport', 'Dubai', 'UAE');
INSERT INTO airport (code, name, city, country) VALUES ('SIN', 'Singapore Changi Airport', 'Singapore', 'Singapore');

-- Insert Sample Flights
INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('AA101', 1, 2, '2026-06-15 08:00:00', '2026-06-15 11:30:00', 299.99, 150);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('UA202', 2, 1, '2026-06-15 14:00:00', '2026-06-15 22:30:00', 349.99, 180);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('DL303', 1, 3, '2026-06-16 09:00:00', '2026-06-16 11:00:00', 189.99, 120);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('BA404', 1, 4, '2026-06-17 18:00:00', '2026-06-18 06:00:00', 599.99, 200);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('AF505', 4, 5, '2026-06-18 10:00:00', '2026-06-18 11:30:00', 129.99, 100);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('JL606', 2, 6, '2026-06-19 12:00:00', '2026-06-20 16:00:00', 899.99, 250);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('EK707', 4, 7, '2026-06-20 22:00:00', '2026-06-21 06:00:00', 479.99, 300);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('SQ808', 7, 8, '2026-06-21 08:00:00', '2026-06-21 14:30:00', 359.99, 180);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('AA909', 3, 2, '2026-06-22 07:00:00', '2026-06-22 09:30:00', 249.99, 140);

INSERT INTO flight (flight_number, origin_id, destination_id, departure_time, arrival_time, price, available_seats) 
VALUES ('UA110', 2, 3, '2026-06-22 15:00:00', '2026-06-22 19:00:00', 279.99, 160);
