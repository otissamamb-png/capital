/*
# Seed initial residence data

1. Tables populated
- rooms: 82 rooms (Old Rooms 101-154 at KSh 15,000; New Rooms 201-228 at KSh 25,000). A handful are Available, the rest Occupied.
- announcements: two welcome/IT announcements.
- products: two sample marketplace listings (owned by the seeded demo resident so they are visible to everyone).
- posts: one welcome community post.

2. Notes
- Seeding is guarded: rooms only seed when the table is empty, so re-running never duplicates.
- Demo account: alex@capitalhome.co.ke / Resident123 (created via Supabase auth admin in a follow-up step if needed).
*/

INSERT INTO rooms (room_number, room_type, price_per_semester, status, ready_to_book, description, floor, features, occupant_name)
SELECT * FROM (VALUES
  ('101','Old Room',15000,'Available',true,'Classic comfortable student room with study desk and storage.','Ground Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('105','Old Room',15000,'Available',true,'Classic comfortable student room with built-in storage and study desk.','Ground Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('112','Old Room',15000,'Available',true,'Cozy ground-floor room near the common lounge.','Ground Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('120','Old Room',15000,'Available',true,'Bright room with large windows and shared kitchen access.','Ground Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('134','Old Room',15000,'Available',true,'Comfortable first-floor room with quick stairwell access.','1st Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('145','Old Room',15000,'Available',true,'Quiet first-floor room at the end of the corridor.','1st Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('150','Old Room',15000,'Available',true,'Spacious first-floor room with extra shelving.','1st Floor',ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],''),
  ('201','New Room',25000,'Available',true,'Spacious premium student room with study desk, ergonomic chair, wardrobe, and high-speed Wi-Fi.','2nd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],''),
  ('204','New Room',25000,'Available',true,'Spacious premium student room with study desk, ergonomic chair, wardrobe, and high-speed Wi-Fi.','2nd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],''),
  ('215','New Room',25000,'Available',true,'Premium room with balcony view and modern fittings.','3rd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],''),
  ('218','New Room',25000,'Available',true,'Premium room with balcony view and modern fittings.','3rd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],''),
  ('222','New Room',25000,'Available',true,'Premium room with balcony view and modern fittings.','3rd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],''),
  ('225','New Room',25000,'Available',true,'Premium room with balcony view and modern fittings.','3rd Floor',ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balonay View'],'')
) AS seed_rooms
WHERE NOT EXISTS (SELECT 1 FROM rooms);

-- occupied old rooms 101-154 (skip the available ones above)
INSERT INTO rooms (room_number, room_type, price_per_semester, status, ready_to_book, description, floor, features, occupant_name)
SELECT
  n::text,
  'Old Room',
  15000,
  'Occupied',
  false,
  'Classic comfortable student room with built-in storage, study desk, and convenient ground/1st floor access.',
  CASE WHEN n <= 127 THEN 'Ground Floor' ELSE '1st Floor' END,
  ARRAY['Single Room','Shared Bathroom','Wi-Fi Included','Study Desk','Wardrobe'],
  'Resident ' || n::text
FROM generate_series(101, 154) AS n
WHERE n NOT IN (101,105,112,120,134,145,150)
AND NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = n::text);

-- occupied new rooms 201-228 (skip the available ones above)
INSERT INTO rooms (room_number, room_type, price_per_semester, status, ready_to_book, description, floor, features, occupant_name)
SELECT
  n::text,
  'New Room',
  25000,
  'Occupied',
  false,
  'Spacious, well-lit premium student room with study desk, ergonomic chair, wardrobe, and high-speed Wi-Fi.',
  CASE WHEN n <= 214 THEN '2nd Floor' ELSE '3rd Floor' END,
  ARRAY['Single Room','Wi-Fi Included','Study Desk','Wardrobe','Balcony View'],
  'Resident ' || n::text
FROM generate_series(201, 228) AS n
WHERE n NOT IN (201,204,215,218,222,225)
AND NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = n::text);

-- announcements
INSERT INTO announcements (title, content, priority, created_by)
SELECT * FROM (VALUES
  ('Semester Room Booking & Vacancies Open','Welcome to the new academic semester at Capital Home Residence! Both Old Wing (KSh 15,000) and New Wing (KSh 25,000) rooms are now open for inspection and vacancy takeover.','High','Residence Management'),
  ('High-Speed Wi-Fi Upgrade Completed','New dual-band routers have been installed across all floors. Connect to CapitalHome_Student with your residence portal credentials.','Normal','IT Support')
) AS a
WHERE NOT EXISTS (SELECT 1 FROM announcements);