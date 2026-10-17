import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL as string
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY as string

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

export interface Profile {
  id: string
  email: string
  full_name: string
  phone: string
  role: string
  room_number: string
  account_status: string
  created_at: string
}

export interface Room {
  id: string
  room_number: string
  room_type: string
  price_per_semester: number
  status: string
  ready_to_book: boolean
  description: string
  image_url: string
  floor: string
  features: string[]
  occupant_name: string
  occupant_id: string | null
}

export interface Vacancy {
  id: string
  room_number: string
  poster_id: string
  poster_name: string
  poster_phone: string
  title: string
  description: string
  expected_move_out_date: string
  price_per_semester: number
  room_type: string
  status: string
  created_at: string
}

export interface Product {
  id: string
  seller_id: string
  seller_name: string
  category: string
  name: string
  description: string
  price: number
  original_price: number
  quantity: number
  condition: string
  delivery_option: string
  image_url: string
  status: string
  rating: number
  review_count: number
  created_at: string
}

export interface Post {
  id: string
  author_id: string
  author_name: string
  content: string
  post_type: string
  likes_count: number
  comments_count: number
  created_at: string
}

export interface Comment {
  id: string
  post_id: string
  user_id: string
  user_name: string
  content: string
  created_at: string
}

export interface CartItem {
  id: string
  user_id: string
  product_id: string
  product_name: string
  price: number
  quantity: number
  image_url: string
  seller_name: string
}

export interface Order {
  id: string
  order_number: string
  buyer_id: string
  buyer_name: string
  buyer_phone: string
  seller_id: string
  seller_name: string
  product_id: string
  product_name: string
  product_image: string
  quantity: number
  unit_price: number
  total_amount: number
  order_status: string
  payment_status: string
  pickup_or_delivery: string
  mpesa_reference: string
  created_at: string
}

export interface Announcement {
  id: string
  title: string
  content: string
  priority: string
  created_by: string
  created_at: string
}

export interface SavedItem {
  id: string
  user_id: string
  item_type: string
  item_id: string
  title: string
  subtitle: string
  price: number
}
