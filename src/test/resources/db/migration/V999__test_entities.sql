CREATE TABLE test_entity (
    id BIGSERIAL PRIMARY KEY,
    firstName VARCHAR(255),
    name VARCHAR(255),
    description TEXT,
    category VARCHAR(100)
);

INSERT INTO test_entity (firstName, name, description, category) VALUES
('John', 'Doe', 'Software Developer', 'IT'),
('Jane', 'Smith', 'Project Manager', 'Management'),
('Bob', 'Johnson', 'Database Administrator', 'IT'),
('John', 'Williams', 'DevOps Engineer', 'IT'),
('Sarah', 'Johnson', 'Business Analyst', 'Management'),
('Mike', 'Brown', 'Quality Assurance', 'IT'),
('Jane', 'Davis', 'HR Specialist', 'HR'),
('David', 'Smith', 'Network Administrator', 'IT'),
('Lisa', 'Wilson', 'Marketing Manager', 'Marketing'),
('Bob', 'Miller', 'Financial Analyst', 'Finance'),
('Emily', 'Johnson', 'UX Designer', 'Design'),
('John', 'Taylor', 'System Administrator', 'IT'),
('Amanda', 'Brown', 'Sales Representative', 'Sales');

CREATE TABLE multilingual_test_entity (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20),
    category VARCHAR(50),
    name JSONB,
    description JSONB
);

-- Test data is chosen so that the alphabetical order of "name" DIFFERS per language,
-- which makes language-specific sorting assertions meaningful.
-- P-600 deliberately has NO French name translation (missing-translation case).
INSERT INTO multilingual_test_entity (code, category, name, description) VALUES
('P-100', 'FRUIT',     '{"de": "Apfel",   "fr": "Pomme",   "it": "Mela"}',     '{"de": "Süss und knackig",   "fr": "Sucrée et croquante",  "it": "Dolce e croccante"}'),
('P-200', 'FRUIT',     '{"de": "Birne",   "fr": "Poire",   "it": "Pera"}',     '{"de": "Saftig und mild",    "fr": "Juteuse et douce",     "it": "Succosa e delicata"}'),
('P-300', 'VEGETABLE', '{"de": "Zwiebel", "fr": "Oignon",  "it": "Cipolla"}',  '{"de": "Scharf im Geschmack","fr": "Goût piquant",         "it": "Gusto piccante"}'),
('P-400', 'VEGETABLE', '{"de": "Karotte", "fr": "Carotte", "it": "Carota"}',   '{"de": "Reich an Vitaminen", "fr": "Riche en vitamines",   "it": "Ricca di vitamine"}'),
('P-500', 'OTHER',     '{"de": "Milch",   "fr": "Lait",    "it": "Latte"}',    '{"de": "Täglich frisch",     "fr": "Fraîche chaque jour",  "it": "Fresco ogni giorno"}'),
('P-600', 'OTHER',     '{"de": "Ei",                       "it": "Uovo"}',     '{"de": "Frisch vom Hof",     "fr": "Frais de la ferme",    "it": "Fresco di fattoria"}');
