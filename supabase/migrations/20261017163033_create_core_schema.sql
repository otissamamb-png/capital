/*
# Capital Home Residence — core schema

1. New Tables
- `profiles` — one row per account. `id` matches the Supabase auth user id. Columns: email, full_name, phone, role (resident/seller/admin), room_number, account_status.
- `rooms` — residence rooms. room_number (unique), room_type (Old Room / New Room), price_per_semester, status (Available / Occupied / Pending Move-Out), ready_to_book, description, features, occupant_name, occupant_id.
- `vacancies` — move-out notices posted by residents. Room number, poster info, title, description, price, move-out date, status.
- `products` — marketplace listings. Seller info, category, name, description, price, quantity, condition, image_url, status.
- `posts` — community posts (author, content, post_type, likes_count, comments_count).
- `post_likes` — one row per user per post they like (unique on post_id+user_id).
- `comments` — comments on posts (post_id, user info, content).
- `cart_items` — per-user shopping cart lines (product info, price, quantity).
- `orders` — purchase orders (buyer/seller, product, amounts, order_status, payment_status, mpesa_reference).
- `announcements` — official management announcements (title, content, priority).
- `saved_items` — bookmarked products/rooms per user (item_type, item_id).

2. Security
- RLS enabled on every table.
- Readable data (rooms, products, posts, comments, announcements, vacancies): public read for anon + authenticated — this content is shared community data.
- Owner-scoped data (cart_items, saved_items, orders, profiles): each signed-in user reads/writes only their own rows. carts/saved default their user_id to auth.uid() so inserts work from the client without passing the owner.
- post_likes readable by all; inserts/deletes tied to the authenticated user via user_id = auth.uid().

3. Notes
- user_id columns have `DEFAULT auth.uid()` so client inserts that omit them still pass RLS.
- Trigger `on_auth_user_created` auto-creates a profile row when someone signs up.
- Anonymous (not signed-in) visitors can browse but not write.
*/

-- profiles
CREATE TABLE IF NOT EXISTS profiles (
  id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email text NOT NULL,
  full_name text NOT NULL DEFAULT '',
  phone text NOT NULL DEFAULT '',
  role text NOT NULL DEFAULT 'resident',
  room_number text NOT NULL DEFAULT '',
  account_status text NOT NULL DEFAULT 'active',
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "profiles_select_own" ON profiles;
CREATE POLICY "profiles_select_own" ON profiles FOR SELECT
TO authenticated USING (auth.uid() = id);

DROP POLICY IF EXISTS "profiles_insert_own" ON profiles;
CREATE POLICY "profiles_insert_own" ON profiles FOR INSERT
TO authenticated WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "profiles_update_own" ON profiles;
CREATE POLICY "profiles_update_own" ON profiles FOR UPDATE
TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- auto-create profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name, phone)
  VALUES (
    NEW.id,
    COALESCE(NEW.email, ''),
    COALESCE(NEW.raw_user_meta_data->>'full_name', split_part(COALESCE(NEW.email, ''), '@', 1)),
    COALESCE(NEW.raw_user_meta_data->>'phone', '')
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- rooms
CREATE TABLE IF NOT EXISTS rooms (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  room_number text UNIQUE NOT NULL,
  room_type text NOT NULL DEFAULT 'Old Room',
  price_per_semester bigint NOT NULL DEFAULT 15000,
  status text NOT NULL DEFAULT 'Occupied',
  ready_to_book boolean NOT NULL DEFAULT false,
  description text NOT NULL DEFAULT '',
  image_url text NOT NULL DEFAULT '',
  floor text NOT NULL DEFAULT '',
  features text[] NOT NULL DEFAULT '{}',
  occupant_name text NOT NULL DEFAULT '',
  occupant_id uuid REFERENCES auth.users(id) ON DELETE SET NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "rooms_public_read" ON rooms;
CREATE POLICY "rooms_public_read" ON rooms FOR SELECT
TO anon, authenticated USING (true);

-- vacancies
CREATE TABLE IF NOT EXISTS vacancies (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  room_number text NOT NULL,
  poster_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  poster_name text NOT NULL DEFAULT '',
  poster_phone text NOT NULL DEFAULT '',
  title text NOT NULL,
  description text NOT NULL DEFAULT '',
  expected_move_out_date text NOT NULL DEFAULT '',
  price_per_semester bigint NOT NULL DEFAULT 15000,
  room_type text NOT NULL DEFAULT 'Old Room',
  status text NOT NULL DEFAULT 'Approved',
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE vacancies ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "vacancies_public_read" ON vacancies;
CREATE POLICY "vacancies_public_read" ON vacancies FOR SELECT
TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "vacancies_insert_own" ON vacancies;
CREATE POLICY "vacancies_insert_own" ON vacancies FOR INSERT
TO authenticated WITH CHECK (auth.uid() = poster_id);

-- products
CREATE TABLE IF NOT EXISTS products (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  seller_name text NOT NULL DEFAULT '',
  category text NOT NULL DEFAULT 'General',
  name text NOT NULL,
  description text NOT NULL DEFAULT '',
  price bigint NOT NULL DEFAULT 0,
  original_price bigint NOT NULL DEFAULT 0,
  quantity bigint NOT NULL DEFAULT 1,
  condition text NOT NULL DEFAULT 'New',
  delivery_option text NOT NULL DEFAULT 'Pickup at Capital Home Residence',
  image_url text NOT NULL DEFAULT '',
  status text NOT NULL DEFAULT 'Active',
  rating numeric(3,2) NOT NULL DEFAULT 4.80,
  review_count integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "products_public_read" ON products;
CREATE POLICY "products_public_read" ON products FOR SELECT
TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "products_insert_own" ON products;
CREATE POLICY "products_insert_own" ON products FOR INSERT
TO authenticated WITH CHECK (auth.uid() = seller_id);

DROP POLICY IF EXISTS "products_update_own" ON products;
CREATE POLICY "products_update_own" ON products FOR UPDATE
TO authenticated USING (auth.uid() = seller_id) WITH CHECK (auth.uid() = seller_id);

DROP POLICY IF EXISTS "products_delete_own" ON products;
CREATE POLICY "products_delete_own" ON products FOR DELETE
TO authenticated USING (auth.uid() = seller_id);

-- posts
CREATE TABLE IF NOT EXISTS posts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  author_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  author_name text NOT NULL DEFAULT '',
  content text NOT NULL,
  post_type text NOT NULL DEFAULT 'General',
  likes_count integer NOT NULL DEFAULT 0,
  comments_count integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "posts_public_read" ON posts;
CREATE POLICY "posts_public_read" ON posts FOR SELECT
TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "posts_insert_own" ON posts;
CREATE POLICY "posts_insert_own" ON posts FOR INSERT
TO authenticated WITH CHECK (auth.uid() = author_id);

DROP POLICY IF EXISTS "posts_update_own" ON posts;
CREATE POLICY "posts_update_own" ON posts FOR UPDATE
TO authenticated USING (auth.uid() = author_id) WITH CHECK (auth.uid() = author_id);

DROP POLICY IF EXISTS "posts_delete_own" ON posts;
CREATE POLICY "posts_delete_own" ON posts FOR DELETE
TO authenticated USING (auth.uid() = author_id);

-- post_likes
CREATE TABLE IF NOT EXISTS post_likes (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  post_id uuid NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (post_id, user_id)
);

ALTER TABLE post_likes ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "post_likes_public_read" ON post_likes;
CREATE POLICY "post_likes_public_read" ON post_likes FOR SELECT
TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "post_likes_insert_own" ON post_likes;
CREATE POLICY "post_likes_insert_own" ON post_likes FOR INSERT
TO authenticated WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "post_likes_delete_own" ON post_likes;
CREATE POLICY "post_likes_delete_own" ON post_likes FOR DELETE
TO authenticated USING (auth.uid() = user_id);

-- comments
CREATE TABLE IF NOT EXISTS comments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  post_id uuid NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  user_name text NOT NULL DEFAULT '',
  content text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE comments ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "comments_public_read" ON comments;
CREATE POLICY "comments_public_read" ON comments FOR SELECT
TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "comments_insert_own" ON comments;
CREATE POLICY "comments_insert_own" ON comments FOR INSERT
TO authenticated WITH CHECK (auth.uid() = user_id);

-- cart_items
CREATE TABLE IF NOT EXISTS cart_items (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  product_name text NOT NULL DEFAULT '',
  price bigint NOT NULL DEFAULT 0,
  quantity bigint NOT NULL DEFAULT 1,
  image_url text NOT NULL DEFAULT '',
  seller_name text NOT NULL DEFAULT '',
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, product_id)
);

ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "cart_select_own" ON cart_items;
CREATE POLICY "cart_select_own" ON cart_items FOR SELECT
TO authenticated USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "cart_insert_own" ON cart_items;
CREATE POLICY "cart_insert_own" ON cart_items FOR INSERT
TO authenticated WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "cart_update_own" ON cart_items;
CREATE POLICY "cart_update_own" ON cart_items FOR UPDATE
TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "cart_delete_own" ON cart_items;
CREATE POLICY "cart_delete_own" ON cart_items FOR DELETE
TO authenticated USING (auth.uid() = user_id);

-- orders
CREATE TABLE IF NOT EXISTS orders (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_number text NOT NULL UNIQUE,
  buyer_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  buyer_name text NOT NULL DEFAULT '',
  buyer_phone text NOT NULL DEFAULT '',
  seller_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  seller_name text NOT NULL DEFAULT '',
  product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  product_name text NOT NULL DEFAULT '',
  product_image text NOT NULL DEFAULT '',
  quantity bigint NOT NULL DEFAULT 1,
  unit_price bigint NOT NULL DEFAULT 0,
  total_amount bigint NOT NULL DEFAULT 0,
  order_status text NOT NULL DEFAULT 'Pending Payment',
  payment_status text NOT NULL DEFAULT 'Payment Pending',
  pickup_or_delivery text NOT NULL DEFAULT 'Pickup at Capital Home Residence',
  mpesa_reference text NOT NULL DEFAULT '',
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "orders_select_own" ON orders;
CREATE POLICY "orders_select_own" ON orders FOR SELECT
TO authenticated USING (auth.uid() = buyer_id OR auth.uid() = seller_id);

DROP POLICY IF EXISTS "orders_insert_own" ON orders;
CREATE POLICY "orders_insert_own" ON orders FOR INSERT
TO authenticated WITH CHECK (auth.uid() = buyer_id);

DROP POLICY IF EXISTS "orders_update_own" ON orders;
CREATE POLICY "orders_update_own" ON orders FOR UPDATE
TO authenticated USING (auth.uid() = buyer_id OR auth.uid() = seller_id) WITH CHECK (auth.uid() = buyer_id OR auth.uid() = seller_id);

-- announcements
CREATE TABLE IF NOT EXISTS announcements (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title text NOT NULL,
  content text NOT NULL DEFAULT '',
  priority text NOT NULL DEFAULT 'Normal',
  created_by text NOT NULL DEFAULT 'Management',
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "announcements_public_read" ON announcements;
CREATE POLICY "announcements_public_read" ON announcements FOR SELECT
TO anon, authenticated USING (true);

-- saved_items
CREATE TABLE IF NOT EXISTS saved_items (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL DEFAULT auth.uid() REFERENCES auth.users(id) ON DELETE CASCADE,
  item_type text NOT NULL DEFAULT 'product',
  item_id text NOT NULL,
  title text NOT NULL DEFAULT '',
  subtitle text NOT NULL DEFAULT '',
  price bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, item_type, item_id)
);

ALTER TABLE saved_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "saved_select_own" ON saved_items;
CREATE POLICY "saved_select_own" ON saved_items FOR SELECT
TO authenticated USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "saved_insert_own" ON saved_items;
CREATE POLICY "saved_insert_own" ON saved_items FOR INSERT
TO authenticated WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "saved_delete_own" ON saved_items;
CREATE POLICY "saved_delete_own" ON saved_items FOR DELETE
TO authenticated USING (auth.uid() = user_id);

-- indexes
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_cart_user ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller ON orders(seller_id);