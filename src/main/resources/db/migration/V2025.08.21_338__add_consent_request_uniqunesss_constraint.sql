CREATE UNIQUE INDEX uk_consent_request_data_request_unarchived
ON data_request_data_product (data_request_id, data_product_id)
WHERE archived = false;
