import { useState } from 'react'
import { useAuth } from '../auth'

export default function AuthScreen() {
  const { signIn, signUp } = useAuth()
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [phone, setPhone] = useState('')
  const [roomNumber, setRoomNumber] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    if (mode === 'login') {
      const { error } = await signIn(email.trim(), password)
      setBusy(false)
      if (error) setError(error)
    } else {
      if (!fullName.trim()) { setError('Please enter your full name.'); setBusy(false); return }
      const { error } = await signUp(email.trim(), password, fullName.trim(), phone.trim(), roomNumber.trim())
      setBusy(false)
      if (error) setError(error)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <div className="brand-mark" style={{ width: 64, height: 64, borderRadius: 18, margin: '0 auto 12px', fontSize: 20 }}>CH</div>
          <div style={{ fontWeight: 800, fontSize: 22, color: 'var(--navy)' }}>CAPITAL HOME</div>
          <div className="brand-sub" style={{ fontSize: 13, marginTop: 2 }}>RESIDENCE</div>
          <div className="pill pill-info" style={{ marginTop: 10 }}>Live. Connect. Buy. Sell.</div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
          <button
            className={`btn ${mode === 'login' ? 'btn-primary' : 'btn-outline'}`}
            style={{ flex: 1 }}
            onClick={() => { setMode('login'); setError(null) }}
          >Log In</button>
          <button
            className={`btn ${mode === 'signup' ? 'btn-primary' : 'btn-outline'}`}
            style={{ flex: 1 }}
            onClick={() => { setMode('signup'); setError(null) }}
          >Create Account</button>
        </div>

        <form onSubmit={handleSubmit}>
          {mode === 'signup' && (
            <>
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input className="field" placeholder="e.g. Alex Kimani" value={fullName} onChange={e => setFullName(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Phone Number</label>
                <input className="field" placeholder="+254 712 345 678" value={phone} onChange={e => setPhone(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Room Number</label>
                <input className="field" placeholder="e.g. 204" value={roomNumber} onChange={e => setRoomNumber(e.target.value)} />
              </div>
            </>
          )}
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input className="field" type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input className="field" type="password" placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)} minLength={6} required />
          </div>
          <button className="btn btn-primary" style={{ width: '100%', marginTop: 8 }} disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Log In' : 'Create Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: 12, color: 'var(--slate-500)', marginTop: 16 }}>
          Join the Capital Home Residence community to manage rooms, buy &amp; sell items, and connect with fellow residents.
        </p>
      </div>
    </div>
  )
}
