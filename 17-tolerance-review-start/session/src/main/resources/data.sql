INSERT INTO session (id, schedule) VALUES ('s-1-1', 1), ('s-1-2', 2), ('s-2-1', 1);
INSERT INTO speaker (name, uuid) VALUES ('Emmanuel', 's-1-1'), ('Clement', 's-1-2'), ('Alex', 's-1-3'), ('Burr', 's-1-4');
INSERT INTO session_speakers (session_id, speaker_id) SELECT 's-1-1', id FROM speaker WHERE name='Emmanuel';
INSERT INTO session_speakers (session_id, speaker_id) SELECT 's-1-2', id FROM speaker WHERE name='Burr';
INSERT INTO session_speakers (session_id, speaker_id) SELECT 's-2-1', id FROM speaker WHERE name='Alex';
