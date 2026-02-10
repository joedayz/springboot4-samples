INSERT INTO associate (name) VALUES ('Jaime');
INSERT INTO associate (name) VALUES ('Pablo');

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Desk', 'CASH', 150.50, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Online Learning', 'CREDIT_CARD', 75.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Books', 'CASH', 50.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Internet', 'CREDIT_CARD', 20.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Phone', 'CASH', 15.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Bookshelf', 'CASH', 150.50, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Printer Cartridges', 'CREDIT_CARD', 15.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Online Learning', 'CASH', 50.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Internet', 'CREDIT_CARD', 20.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (RANDOM_UUID(), 'Phone', 'CASH', 15.00, 2, CURRENT_TIMESTAMP);
