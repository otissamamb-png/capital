import { useEffect, useState } from 'react'
import { supabase, Room, Product, Announcement, Order } from '../lib/supabase'
import { useAuth, formatKSh, timeAgo } from '../auth'

export default function HomeScreen({ onNavigate }: { onNavigate: (tab: any) => void }) {
  const { profile, session } = useAuth()
  const [rooms, setRooms] = useState<Room[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [announcements, setAnnouncements] = useState<Announcement[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    ;(async () => {
      const [roomsRes, productsRes, annRes, ordersRes] = await Promise.all([
        supabase.from('rooms').select('*').order('room_number'),
        supabase.from('products').select('*').eq('status', 'Active').order('created_at', { ascending: false }).limit(6),
        supabase.from('announcements').select('*').order('created_at', { ascending: false }).limit(2),
        supabase.from('orders').select('*').eq('buyer_id', session!.user.id)
          .not('order_status', 'in', '("Completed","Cancelled")').order('created_at', { ascending: false }).limit(1),
      ])
      setRooms(roomsRes.data ?? [])
      setProducts(productsRes.data ?? [])
      setAnnouncements(annRes.data ?? [])
      setOrders(ordersRes.data ?? [])
      setLoading(false)
    })()
  }, [session])

  if (loading) return <div className="loading"><div className="spinner" /></div>

  const available = rooms.filter(r => r.status === 'Available')
  const occupied = rooms.filter(r => r.status === 'Occupied')
  const latestOrder = orders[0]

  const greeting = (() => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  })()

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 className="section-title">{greeting}, {profile?.full_name?.split(' ')[0] || 'Resident'}</h1>
        <p className="section-sub">Welcome to Capital Home Residence</p>
      </div>

      {latestOrder && (
        <div className="card hover" style={{ cursor: 'pointer' }} onClick={() => onNavigate('market')}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <strong>Order Tracking • #{latestOrder.order_number}</strong>
            <span className="pill pill-pending">{latestOrder.order_status}</span>
          </div>
          <p style={{ fontSize: 13, color: 'var(--slate-500)' }}>
            {latestOrder.product_name} • {formatKSh(latestOrder.total_amount)} • Sold by {latestOrder.seller_name}
          </p>
        </div>
      )}

      <div className="grid grid-3">
        <div className="card stat-card"><div className="stat-num" style={{ color: 'var(--emerald)' }}>{available.length}</div><div className="stat-label">Available Rooms</div></div>
        <div className="card stat-card"><div className="stat-num" style={{ color: 'var(--royal)' }}>{occupied.length}</div><div className="stat-label">Occupied Rooms</div></div>
        <div className="card stat-card"><div className="stat-num" style={{ color: 'var(--slate-700)' }}>{rooms.length}</div><div className="stat-label">Total Rooms</div></div>
      </div>

      {announcements.length > 0 && (
        <div>
          <h2 className="section-title">Important Announcements</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: 12 }}>
            {announcements.map(a => (
              <div key={a.id} className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                  <strong>{a.title}</strong>
                  <span className={`pill ${a.priority === 'High' ? 'pill-occupied' : 'pill-info'}`}>{a.priority}</span>
                </div>
                <p style={{ fontSize: 13, color: 'var(--slate-600)' }}>{a.content}</p>
                <p style={{ fontSize: 11, color: 'var(--slate-400)', marginTop: 6 }}>{a.created_by} • {timeAgo(a.created_at)}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 12 }}>
          <h2 className="section-title" style={{ marginBottom: 0 }}>Available Rooms</h2>
          <button className="btn btn-outline btn-sm" onClick={() => onNavigate('rooms')}>See All</button>
        </div>
        {available.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">🏠</div>No rooms currently available. Check back soon!</div>
        ) : (
          <div className="grid grid-3">
            {available.slice(0, 6).map(r => (
              <div key={r.id} className="card hover" style={{ cursor: 'pointer' }} onClick={() => onNavigate('rooms')}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <strong>Room {r.room_number}</strong>
                  <span className={`pill ${r.status === 'Available' ? 'pill-available' : 'pill-occupied'}`}>{r.status}</span>
                </div>
                <p style={{ color: 'var(--royal)', fontWeight: 700, marginTop: 6 }}>{formatKSh(r.price_per_semester)} / sem</p>
                <p style={{ fontSize: 12, color: 'var(--slate-500)', marginTop: 4 }}>{r.room_type} • {r.floor}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 12 }}>
          <h2 className="section-title" style={{ marginBottom: 0 }}>Newest Marketplace Products</h2>
          <button className="btn btn-outline btn-sm" onClick={() => onNavigate('market')}>Explore All</button>
        </div>
        <div className="grid grid-3">
          {products.map(p => (
            <div key={p.id} className="card hover" style={{ cursor: 'pointer' }} onClick={() => onNavigate('market')}>
              <strong style={{ display: 'block' }}>{p.name}</strong>
              <p style={{ color: 'var(--royal)', fontWeight: 700, marginTop: 4 }}>{formatKSh(p.price)}</p>
              <p style={{ fontSize: 12, color: 'var(--slate-500)', marginTop: 2 }}>Sold by {p.seller_name}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
