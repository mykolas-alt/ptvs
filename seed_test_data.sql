INSERT INTO vendor_contact (id, name, email, vendor_name, phone, address, department) 
VALUES (
    gen_random_uuid(), 
    'John Doe', 
    'support@mock.com', 
    'Mock Cloud Services', 
    '12345', 
    '123 Cloud St', 
    'Billing'
);


INSERT INTO third_party_service (
    id, 
    service_name, 
    monthly_cost, 
    contract_start_date, 
    contract_end_date, 
    status, 
    vendor_contact_id
)
SELECT 
    gen_random_uuid(), 
    'Test Hosting', 
    300.00, 
    '2024-01-01', 
    '2024-01-10', 
    'ACTIVE',  
    id 
FROM vendor_contact 
WHERE vendor_name = 'Mock Cloud Services';