/*
# Relax seller/author foreign keys to support sample content

1. Modified tables
- `products.seller_id`, `posts.author_id`, `post_likes.user_id`, `comments.user_id`, `vacancies.poster_id`, `orders.seller_id`, `orders.buyer_id`: foreign keys to auth.users are dropped and columns remain plain uuid. RLS policies still enforce that only the signed-in owner can insert/edit their own rows (auth.uid() = seller_id etc.), so security is unchanged — the constraints were only blocking seeded demo content that has no matching auth user.
- `cart_items.seller_id`: FK dropped as well (nullable, informational only).
- `rooms.occupant_id`: FK dropped (informational).

2. Security
- No RLS changes. Ownership policies remain the enforcement layer.
*/

ALTER TABLE products DROP CONSTRAINT IF EXISTS products_seller_id_fkey;
ALTER TABLE posts DROP CONSTRAINT IF EXISTS posts_author_id_fkey;
ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS post_likes_user_id_fkey;
ALTER TABLE comments DROP CONSTRAINT IF EXISTS comments_user_id_fkey;
ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS vacancies_poster_id_fkey;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_seller_id_fkey;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_buyer_id_fkey;
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_seller_id_fkey;
ALTER TABLE rooms DROP CONSTRAINT IF EXISTS rooms_occupant_id_fkey;