-- Fix User Schema - Make google_id nullable for regular users
-- This fixes the registration issue where non-Google users can't register

-- Drop the NOT NULL constraint on google_id if it exists
ALTER TABLE users ALTER COLUMN google_id DROP NOT NULL;

-- Make sure the column allows NULL values
ALTER TABLE users ALTER COLUMN google_id SET DEFAULT NULL;

-- Add comment for clarity
COMMENT ON COLUMN users.google_id IS 'Google OAuth ID - nullable for regular users';

-- Ensure email is unique and not null (should already be set)
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Ensure name is not null (should already be set)  
ALTER TABLE users ALTER COLUMN name SET NOT NULL;

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Create index on google_id for OAuth users
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;