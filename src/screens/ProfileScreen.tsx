import { useState } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth } from '../auth'

export default function ProfileScreen() {
  const { session, profile, signOut } = useAuth()
  const [editing, setEditing] = useState(false)
  const [fullName, setFullName] = useState(profile?.full_name || '')
  const [phone, setPhone] = useState(profile?.phone || '')
  const [roomNumber, setRoomNumber] = useState(profile?.room_number || '')
  const [message, setMessage] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function save(e: React.FormEvent) {
    e.preventDefault()
    if (!session) return
    setBusy(true)
    const { error } = await supabase.from('profiles').update({
      full_name: fullName.trim(),
      phone: phone.trim(),
      room_number: roomNumber.trim(),
    }).eq('id', session.user.id)
    setBusy(false)
    if (error) setMessage(`Update failed: ${error.message}`)
    else {
      setMessage('Profile updated successfully')
      setEditing(false)
      setTimeout(() => window.location.reload(), 800)
    }
  }

  if (!profile) return <div className="loading">Loading profile…</div>

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <h1 className="section-title">My Profile</h1>
      <p className="section-sub">Manage your residence account</p>

      {message && <div className="alert alert-info">{message}</div>}

      <div className="card" style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
          <div className="avatar" style={{ width: 64, height: 64, fontSize: 24 }}>
            {profile.full_name?.charAt(0).toUpperCase() || 'U'}
          </div>
          <div>
            <strong style={{ fontSize: 18 }}>{profile.full_name}</strong>
            <p style={{ fontSize: 13, color: 'var(--slate-500)' }}>{profile.email}</p>
            <span className={`pill ${profile.role === 'admin' ? 'pill-occupied' : profile.role === 'seller' ? 'pill-pending' : 'pill-info'}`} style={{ marginTop: 4 }}>
              {profile.role.toUpperCase()}
            </span>
          </div>
        </div>

        {editing ? (
          <form onSubmit={save}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input className="field" value={fullName} onChange={e => setFullName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="field" value={phone} onChange={e => setPhone(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Room Number</label>
              <input className="field" value={roomNumber} onChange={e => setRoomNumber(e.target.value)} />
            </div>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <button type="button" className="btn btn-outline" onClick={() => setEditing(false)}>Cancel</button>
              <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save Changes'}</button>
            </div>
          </form>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <DetailRow label="Phone" value={profile.phone || 'Not set'} />
            <DetailRow label="Room" value={profile.room_number || 'Not assigned'} />
            <DetailRow label="Member Since" value={new Date(profile.created_at).toLocaleDateString()} />
            <button className="btn btn-primary" style={{ marginTop: 8 }} onClick={() => setEditing(true)}>Edit Profile</button>
          </div>
        )}
      </div>

      <button className="btn btn-danger" style={{ width: '100%' }} onClick={() => signOut()}>Sign Out</button>
    </div>
  )
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--slate-100)' }}>
      <span style={{ color: 'var(--slate-500)', fontSize: 13, fontWeight: 600 }}>{label}</span>
      <span style={{ fontSize: 13, fontWeight: 600 }}>{value}</span>
    </div>
  )
}
