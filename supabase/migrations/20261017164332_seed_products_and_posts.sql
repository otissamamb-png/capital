/*
# Seed sample products and community posts

1. Tables populated
- products: three sample marketplace listings (textbooks set, mini fridge, study lamp) listed under "Capital Home Store".
- posts: two community posts (welcome + quiet-hours announcement).

2. Notes
- Guarded with WHERE NOT EXISTS so re-running does not duplicate.
- These sample rows can be deleted from the app by their owner or reassigned once real sellers join.
*/

INSERT INTO products (seller_id, seller_name, category, name, description, price, original_price, quantity, condition, image_url)
SELECT
  '00000000-0000-0000-0000-000000000000'::uuid,
  'Capital Home Store',
  v.category, v.name, v.description, v.price, v.original_price, v.quantity, v.condition, ''
FROM (VALUES
  ('Books','Engineering Mathematics Textbooks Set','Complete set of first-year engineering mathematics textbooks in good condition.',2500,4500,2,'Like New'),
  ('Electronics','Mini Fridge 45L','Compact mini fridge, perfect for dorm rooms. Energy efficient and quiet.',8500,12000,1,'Used'),
  ('Electronics','LED Study Lamp with USB Port','Adjustable LED desk lamp with USB charging port and three brightness modes.',1200,2000,5,'New')
) AS v(category, name, description, price, original_price, quantity, condition)
WHERE NOT EXISTS (SELECT 1 FROM products);

INSERT INTO posts (author_id, author_name, content, post_type, likes_count)
SELECT
  '00000000-0000-0000-0000-000000000000'::uuid,
  'Capital Home Management',
  v.content,
  v.post_type,
  v.likes
FROM (VALUES
  ('General','Welcome to the new semester at Capital Home Residence! Room bookings and the marketplace are now open. 💙',12),
  ('Announcement','Reminder: Quiet hours start at 10 PM daily. Wi-Fi credentials are on the notice board in each floor lounge.',5)
) AS v(post_type, content, likes)
WHERE NOT EXISTS (SELECT 1 FROM posts);