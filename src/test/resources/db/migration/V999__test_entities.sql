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