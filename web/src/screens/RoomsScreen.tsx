import { useEffect, useState } from 'react'
import { supabase, Room, Vacancy } from '../lib/supabase'
import { useAuth, formatKSh } from '../auth'

export default function RoomsScreen() {
  const { profile, session } = useAuth()
  const [rooms, setRooms] = useState<Room[]>([])
  const [filter, setFilter] = useState<'All' | 'Available' | 'Occupied'>('All')
  const [loading, setLoading] = useState(true)
  const [showVacancy, setShowVacancy] = useState(false)

  useEffect(() => {
    ;(async () => {
      const { data } = await supabase.from('rooms').select('*').order('room_number')
      setRooms(data ?? [])
      setLoading(false)
    })()
  }, [])

  const visible = filter === 'All' ? rooms : rooms.filter(r => r.status === filter)

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 className="section-title">Rooms</h1>
          <p className="section-sub">Browse rooms and manage vacancies at Capital Home Residence</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowVacancy(true)}>Announce Vacancy</button>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {(['All', 'Available', 'Occupied'] as const).map(f => (
          <button key={f} className={`btn ${filter === f ? 'btn-primary' : 'btn-outline'} btn-sm`} onClick={() => setFilter(f)}>{f}</button>
        ))}
      </div>

      {visible.length === 0 ? (
        <div className="empty-state"><div className="empty-icon">🏠</div>No rooms match this filter.</div>
      ) : (
        <div className="grid grid-3">
          {visible.map(r => (
            <div key={r.id} className="card hover">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>Room {r.room_number}</strong>
                <span className={`pill ${r.status === 'Available' ? 'pill-available' : r.status === 'Occupied' ? 'pill-occupied' : 'pill-pending'}`}>{r.status}</span>
              </div>
              <p style={{ color: 'var(--royal)', fontWeight: 700, marginTop: 6 }}>{formatKSh(r.price_per_semester)} / semester</p>
              <p style={{ fontSize: 12, color: 'var(--slate-500)', marginTop: 4 }}>{r.room_type} • {r.floor}</p>
              <p style={{ fontSize: 13, color: 'var(--slate-600)', marginTop: 8 }}>{r.description}</p>
              {r.features.length > 0 && (
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
                  {r.features.map(f => <span key={f} className="pill pill-info" style={{ fontSize: 11 }}>{f}</span>)}
                </div>
              )}
              {r.occupant_name && <p style={{ fontSize: 12, color: 'var(--slate-400)', marginTop: 8 }}>Occupant: {r.occupant_name}</p>}
            </div>
          ))}
        </div>
      )}

      {showVacancy && (
        <VacancyDialog
          roomNumber={profile?.room_number || ''}
          onClose={() => setShowVacancy(false)}
        />
      )}
    </div>
  )
}

function VacancyDialog({ roomNumber, onClose }: { roomNumber: string; onClose: () => void }) {
  const { profile } = useAuth()
  const [title, setTitle] = useState('')
  const [room, setRoom] = useState(roomNumber)
  const [description, setDescription] = useState('')
  const [moveOut, setMoveOut] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    const { error } = await supabase.from('vacancies').insert({
      room_number: room.trim(),
      title: title.trim(),
      description: description.trim(),
      expected_move_out_date: moveOut,
      poster_name: profile?.full_name || '',
      poster_phone: profile?.phone || '',
      room_type: 'Old Room',
      price_per_semester: 15000,
    })
    setBusy(false)
    if (error) setError(error.message)
    else onClose()
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h2 style={{ marginBottom: 16 }}>Announce a Vacancy</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit}>
          <div className="form-group">
            <label className="form-label">Room Number</label>
            <input className="field" value={room} onChange={e => setRoom(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Title</label>
            <input className="field" placeholder="e.g. Room moving out end of semester" value={title} onChange={e => setTitle(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Expected Move-Out Date</label>
            <input className="field" type="date" value={moveOut} onChange={e => setMoveOut(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="field" rows={3} placeholder="Details about the room and vacancy" value={description} onChange={e => setDescription(e.target.value)} />
          </div>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-outline" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Posting…' : 'Post Vacancy'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
