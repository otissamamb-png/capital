import { Component, ReactNode, useState } from 'react'
import { AuthProvider, useAuth } from './auth'
import AuthScreen from './screens/AuthScreen'
import HomeScreen from './screens/HomeScreen'
import RoomsScreen from './screens/RoomsScreen'
import MarketplaceScreen from './screens/MarketplaceScreen'
import CommunityScreen from './screens/CommunityScreen'
import ProfileScreen from './screens/ProfileScreen'

class ErrorBoundary extends Component<{ children: ReactNode }, { error: Error | null }> {
  state = { error: null as Error | null }
  static getDerivedStateFromError(error: Error) { return { error } }
  render() {
    if (this.state.error) {
      return (
        <div style={{ padding: 40, fontFamily: 'sans-serif' }}>
          <h2>Something went wrong</h2>
          <p style={{ color: '#64748b' }}>{this.state.error.message}</p>
        </div>
      )
    }
    return this.props.children
  }
}

const TABS = [
  { id: 'home', label: 'Home' },
  { id: 'rooms', label: 'Rooms' },
  { id: 'market', label: 'Market' },
  { id: 'community', label: 'Posts' },
  { id: 'profile', label: 'Profile' },
] as const

type TabId = (typeof TABS)[number]['id']

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <MainApp />
      </AuthProvider>
    </ErrorBoundary>
  )
}

function MainApp() {
  const { session, profile, loading, signOut } = useAuth()
  const [tab, setTab] = useState<TabId>('home')

  if (loading) {
    return <div className="loading"><div className="spinner" /></div>
  }

  if (!session) return <AuthScreen />

  const unreadBadge = 0

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-brand">
          <div className="brand-mark">CH</div>
          <div>
            <div className="brand-name">Capital Home</div>
            <div className="brand-sub">RESIDENCE</div>
          </div>
        </div>

        <nav className="nav-tabs">
          {TABS.map(t => (
            <button key={t.id} className={`nav-tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </nav>

        <div className="topbar-right">
          {unreadBadge > 0 && (
            <button className="icon-btn" title="Notifications">
              🔔<span className="badge-dot">{unreadBadge}</span>
            </button>
          )}
          <div className="avatar" title={profile?.full_name}>
            {(profile?.full_name || 'U').charAt(0).toUpperCase()}
          </div>
          <button className="btn btn-outline btn-sm" onClick={() => signOut()}>Sign Out</button>
        </div>
      </header>

      <main className="page">
        {tab === 'home' && <HomeScreen onNavigate={setTab} />}
        {tab === 'rooms' && <RoomsScreen />}
        {tab === 'market' && <MarketplaceScreen />}
        {tab === 'community' && <CommunityScreen />}
        {tab === 'profile' && <ProfileScreen />}
      </main>
    </div>
  )
}
