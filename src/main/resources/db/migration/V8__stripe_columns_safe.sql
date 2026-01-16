-- MySQL-safe "add column if missing" using INFORMATION_SCHEMA + dynamic SQL

SET @db := DATABASE();

-- stripe_session_id
SELECT COUNT(*) INTO @has_session
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME = 'stripe_session_id';

SET @sql := IF(@has_session = 0,
  'ALTER TABLE orders ADD COLUMN stripe_session_id VARCHAR(255) NULL',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- stripe_payment_intent_id
SELECT COUNT(*) INTO @has_intent
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME = 'stripe_payment_intent_id';

SET @sql := IF(@has_intent = 0,
  'ALTER TABLE orders ADD COLUMN stripe_payment_intent_id VARCHAR(255) NULL',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Optional index for session id (safe)
SELECT COUNT(*) INTO @has_idx
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'orders'
  AND INDEX_NAME = 'idx_orders_stripe_session_id';

SET @sql := IF(@has_idx = 0,
  'CREATE INDEX idx_orders_stripe_session_id ON orders (stripe_session_id)',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
