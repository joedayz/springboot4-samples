INSERT INTO associate (id, name) VALUES (1, 'Jaime') ON CONFLICT DO NOTHING;
INSERT INTO associate (id, name) VALUES (2, 'Pablo') ON CONFLICT DO NOTHING;

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Desk', 'CASH', 150.50, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Desk' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Online Learning', 'CREDIT_CARD', 75.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Online Learning' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Books', 'CASH', 50.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Books' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Internet', 'CREDIT_CARD', 20.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Internet' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Phone', 'CASH', 15.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Phone' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Bookshelf', 'CASH', 150.50, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Bookshelf' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Printer Cartridges', 'CREDIT_CARD', 15.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Printer Cartridges' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Online Learning', 'CASH', 50.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Online Learning' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Internet', 'CREDIT_CARD', 20.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Internet' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Phone', 'CASH', 15.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Phone' AND associate_id = 2);
