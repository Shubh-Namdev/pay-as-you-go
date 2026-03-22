
`Payments -` 

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME,
    status VARCHAR(20) NOT NULL,
    external_txn_id VARCHAR(100),
    created_at DATETIME NOT NULL,

    -- Indexes
    INDEX idx_payment_customer_device_status_created 
        (customer_id, device_id, status, created_at),

    INDEX idx_payment_status_created 
        (status, created_at),

    INDEX idx_payment_status (status)
);


`Device Assigments -`

CREATE TABLE device_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    device_id BIGINT,
    status VARCHAR(20),
    assigned_at DATETIME,
    last_payment_date DATETIME,
    next_due_date DATETIME,
    remaining_balance DECIMAL(10,2),

    -- Indexes
    INDEX idx_assignment_device (device_id),
    INDEX idx_assignment_due_date (next_due_date),
    INDEX idx_assignment_device_status (device_id, status)
);


`Devices -`

CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(255) NOT NULL UNIQUE,
    total_cost DECIMAL(10,2),
    payment_plan_type VARCHAR(50),
    payment_amount DECIMAL(10,2),
    created_at DATETIME NOT NULL

    INDEX idx_device_serial (serial_number)
);


`Customer -`

CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL
);