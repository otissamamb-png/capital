/*
# Add seller_id to cart_items

1. Modified tables
- `cart_items`: new nullable column `seller_id` (uuid, references auth.users) so orders created from a cart line record the actual seller rather than falling back to the product id.

2. Security
- No policy changes; existing owner-scoped cart policies still apply.
*/

ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS seller_id uuid REFERENCES auth.users(id) ON DELETE SET NULL;