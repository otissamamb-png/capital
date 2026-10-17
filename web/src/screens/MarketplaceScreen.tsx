import { useEffect, useState } from 'react'
import { supabase, Product, CartItem, Order } from '../lib/supabase'
import { useAuth, formatKSh } from '../auth'

type View = 'browse' | 'cart' | 'orders' | 'checkout'

async function getProductSeller(productId: string): Promise<string> {
  const { data } = await supabase.from('products').select('seller_id').eq('id', productId).maybeSingle()
  return (data as { seller_id: string } | null)?.seller_id || productId
}

export default function MarketplaceScreen() {
  const { session, profile } = useAuth()
  const [view, setView] = useState<View>('browse')
  const [products, setProducts] = useState<Product[]>([])
  const [cart, setCart] = useState<CartItem[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [showAdd, setShowAdd] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  async function refresh() {
    if (!session) return
    const [prodRes, cartRes, orderRes] = await Promise.all([
      supabase.from('products').select('*').eq('status', 'Active').order('created_at', { ascending: false }),
      supabase.from('cart_items').select('*').eq('user_id', session.user.id).order('created_at', { ascending: false }),
      supabase.from('orders').select('*').eq('buyer_id', session.user.id).order('created_at', { ascending: false }),
    ])
    setProducts(prodRes.data ?? [])
    setCart(cartRes.data ?? [])
    setOrders(orderRes.data ?? [])
    setLoading(false)
  }

  useEffect(() => { refresh() }, [session])

  async function addToCart(p: Product) {
    const { error } = await supabase.from('cart_items').upsert({
      product_id: p.id,
      product_name: p.name,
      price: p.price,
      quantity: 1,
      image_url: p.image_url,
      seller_name: p.seller_name,
      seller_id: p.seller_id,
    }, { onConflict: 'user_id,product_id' })
    if (error) setMessage(`Could not add to cart: ${error.message}`)
    else { setMessage(`Added "${p.name}" to cart`); refresh() }
  }

  async function removeFromCart(id: string) {
    await supabase.from('cart_items').delete().eq('id', id)
    refresh()
  }

  async function checkout(item: CartItem) {
    if (!session) return
    const orderNumber = 'CHR-' + Math.floor(1000 + Math.random() * 9000)
    const { data, error } = await supabase.from('orders').insert({
      order_number: orderNumber,
      buyer_name: profile?.full_name || '',
      buyer_phone: profile?.phone || '',
      seller_id: item.seller_id || (await getProductSeller(item.product_id)),
      seller_name: item.seller_name,
      product_id: item.product_id,
      product_name: item.product_name,
      product_image: item.image_url,
      quantity: item.quantity,
      unit_price: item.price,
      total_amount: item.price * item.quantity,
      order_status: 'Pending Payment',
      payment_status: 'Payment Pending',
    }).select().single()
    if (error) { setMessage(`Checkout failed: ${error.message}`); return }
    await supabase.from('cart_items').delete().eq('id', item.id)
    setView('orders')
    refresh()
  }

  const cartTotal = cart.reduce((sum, c) => sum + c.price * c.quantity, 0)

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 className="section-title">Marketplace</h1>
          <p className="section-sub">Buy and sell within the residence</p>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className={`btn ${view === 'browse' ? 'btn-primary' : 'btn-outline'} btn-sm`} onClick={() => setView('browse')}>Browse</button>
          <button className={`btn ${view === 'cart' ? 'btn-primary' : 'btn-outline'} btn-sm`} onClick={() => setView('cart')}>Cart ({cart.length})</button>
          <button className={`btn ${view === 'orders' ? 'btn-primary' : 'btn-outline'} btn-sm`} onClick={() => setView('orders')}>Orders ({orders.length})</button>
          <button className="btn btn-primary btn-sm" onClick={() => setShowAdd(true)}>+ Sell Item</button>
        </div>
      </div>

      {message && <div className="alert alert-info" onClick={() => setMessage(null)} style={{ cursor: 'pointer' }}>{message}</div>}

      {view === 'browse' && (
        products.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">🛍️</div>No products listed yet. Be the first seller!</div>
        ) : (
          <div className="grid grid-3">
            {products.map(p => (
              <div key={p.id} className="card hover">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                  <strong style={{ display: 'block' }}>{p.name}</strong>
                  <span className="pill pill-info">{p.category}</span>
                </div>
                <p style={{ fontSize: 13, color: 'var(--slate-600)', marginTop: 6 }}>{p.description}</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12 }}>
                  <span style={{ color: 'var(--royal)', fontWeight: 800 }}>{formatKSh(p.price)}</span>
                  <span style={{ fontSize: 12, color: 'var(--slate-500)' }}>{p.quantity} in stock • {p.condition}</span>
                </div>
                <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                  <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => addToCart(p)} disabled={p.quantity <= 0}>
                    {p.quantity <= 0 ? 'Out of Stock' : 'Add to Cart'}
                  </button>
                </div>
                <p style={{ fontSize: 11, color: 'var(--slate-400)', marginTop: 8 }}>Sold by {p.seller_name}</p>
              </div>
            ))}
          </div>
        )
      )}

      {view === 'cart' && (
        cart.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">🛒</div>Your cart is empty.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {cart.map(c => (
              <div key={c.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong>{c.product_name}</strong>
                  <p style={{ fontSize: 13, color: 'var(--slate-500)' }}>Qty {c.quantity} • {formatKSh(c.price * c.quantity)}</p>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button className="btn btn-primary btn-sm" onClick={() => checkout(c)}>Checkout</button>
                  <button className="btn btn-danger btn-sm" onClick={() => removeFromCart(c.id)}>Remove</button>
                </div>
              </div>
            ))}
            <div className="card" style={{ textAlign: 'right' }}>
              <strong>Total: {formatKSh(cartTotal)}</strong>
            </div>
          </div>
        )
      )}

      {view === 'orders' && (
        orders.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📦</div>No orders yet. Your purchases will appear here.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {orders.map(o => (
              <div key={o.id} className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                  <div>
                    <strong>Order #{o.order_number}</strong>
                    <p style={{ fontSize: 13, color: 'var(--slate-500)' }}>{o.product_name} • Qty {o.quantity} • {formatKSh(o.total_amount)}</p>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <span className={`pill ${o.order_status === 'Completed' ? 'pill-available' : o.order_status === 'Cancelled' ? 'pill-occupied' : 'pill-pending'}`}>{o.order_status}</span>
                    <span className="pill pill-info">{o.payment_status}</span>
                  </div>
                </div>
                {o.order_status === 'Pending Payment' && o.payment_status === 'Payment Pending' && (
                  <MpesaSection orderId={o.id} onSubmitted={refresh} />
                )}
              </div>
            ))}
          </div>
        )
      )}

      {showAdd && <AddProductDialog onClose={() => setShowAdd(false)} onAdded={() => { setShowAdd(false); refresh() }} />}
    </div>
  )
}

function MpesaSection({ orderId, onSubmitted }: { orderId: string; onSubmitted: () => void }) {
  const [ref, setRef] = useState('')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (ref.trim().length < 5) { setErr('Enter a valid M-Pesa code (at least 5 characters).'); return }
    setBusy(true)
    const { error } = await supabase.from('orders').update({
      payment_status: 'Reference Submitted',
      order_status: 'Payment Verification',
      mpesa_reference: ref.trim().toUpperCase(),
    }).eq('id', orderId)
    setBusy(false)
    if (error) setErr(error.message)
    else onSubmitted()
  }

  return (
    <form onSubmit={submit} style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid var(--slate-200)' }}>
      <label className="form-label">M-Pesa Transaction Reference</label>
      <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
        <input className="field" placeholder="e.g. QK78HJ23PX" value={ref} onChange={e => setRef(e.target.value)} />
        <button className="btn btn-primary btn-sm" disabled={busy}>Submit</button>
      </div>
      {err && <p style={{ color: 'var(--rose)', fontSize: 12, marginTop: 6 }}>{err}</p>}
    </form>
  )
}

function AddProductDialog({ onClose, onAdded }: { onClose: () => void; onAdded: () => void }) {
  const { profile } = useAuth()
  const [name, setName] = useState('')
  const [category, setCategory] = useState('Electronics')
  const [price, setPrice] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [description, setDescription] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    const { error } = await supabase.from('products').insert({
      seller_name: profile?.full_name || '',
      category,
      name: name.trim(),
      description: description.trim(),
      price: Number(price) || 0,
      quantity: Number(quantity) || 1,
      condition: 'New',
    })
    setBusy(false)
    if (error) setError(error.message)
    else onAdded()
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h2 style={{ marginBottom: 16 }}>List a New Product</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit}>
          <div className="form-group">
            <label className="form-label">Product Name</label>
            <input className="field" value={name} onChange={e => setName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="field" value={category} onChange={e => setCategory(e.target.value)}>
              {['Electronics', 'Fashion', 'Books', 'Food', 'Furniture', 'General'].map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Price (KSh)</label>
            <input className="field" type="number" min="1" value={price} onChange={e => setPrice(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Quantity</label>
            <input className="field" type="number" min="1" value={quantity} onChange={e => setQuantity(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="field" rows={3} value={description} onChange={e => setDescription(e.target.value)} />
          </div>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-outline" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Listing…' : 'List Product'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
