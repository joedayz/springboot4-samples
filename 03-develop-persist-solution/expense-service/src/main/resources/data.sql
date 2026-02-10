INSERT INTO associate (id, name) VALUES (1, 'Jaime');
INSERT INTO associate (id, name) VALUES (2, 'Pablo');

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (1, RANDOM_UUID(), 'Desk', 'CASH', 150.50, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (2, RANDOM_UUID(), 'Online Learning', 'CREDIT_CARD', 75.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (3, RANDOM_UUID(), 'Books', 'CASH', 50.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (4, RANDOM_UUID(), 'Internet', 'CREDIT_CARD', 20.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (5, RANDOM_UUID(), 'Phone', 'CASH', 15.00, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (6, RANDOM_UUID(), 'Bookshelf', 'CASH', 150.50, 1, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (7, RANDOM_UUID(), 'Printer Cartridges', 'CREDIT_CARD', 15.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (8, RANDOM_UUID(), 'Online Learning', 'CASH', 50.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (9, RANDOM_UUID(), 'Internet', 'CREDIT_CARD', 20.00, 2, CURRENT_TIMESTAMP);

INSERT INTO expense (id, uuid, name, payment_method, amount, associate_id, creation_date)
VALUES (10, RANDOM_UUID(), 'Phone', 'CASH', 15.00, 2, CURRENT_TIMESTAMP);
