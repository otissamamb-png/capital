import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { supabase, Profile } from './lib/supabase'
import type { Session } from '@supabase/supabase-js'

interface AuthCtx {
  session: Session | null
  profile: Profile | null
  loading: boolean
  signUp: (email: string, password: string, fullName: string, phone: string, roomNumber: string) => Promise<{ error: string | null }>
  signIn: (email: string, password: string) => Promise<{ error: string | null }>
  signOut: () => Promise<void>
}

const Ctx = createContext<AuthCtx | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null)
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session)
    })

    const { data: sub } = supabase.auth.onAuthStateChange((_event, s) => {
      setSession(s)
      ;(async () => {
        if (s?.user) {
          const { data } = await supabase.from('profiles').select('*').eq('id', s.user.id).maybeSingle()
          setProfile(data as Profile | null)
        } else {
          setProfile(null)
        }
        setLoading(false)
      })()
    })

    // initial load
    ;(async () => {
      const { data: { user } } = await supabase.auth.getUser()
      if (user) {
        const { data } = await supabase.from('profiles').select('*').eq('id', user.id).maybeSingle()
        setProfile(data as Profile | null)
      }
      setLoading(false)
    })()

    return () => { sub?.subscription.unsubscribe() }
  }, [])

  async function signUp(email: string, password: string, fullName: string, phone: string, roomNumber: string) {
    const { error } = await supabase.auth.signUp({
      email,
      password,
      options: { data: { full_name: fullName, phone, room_number: roomNumber } },
    })
    if (error) return { error: error.message }
    return { error: null }
  }

  async function signIn(email: string, password: string) {
    const { error } = await supabase.auth.signInWithPassword({ email, password })
    if (error) return { error: error.message }
    return { error: null }
  }

  async function signOut() {
    await supabase.auth.signOut()
    setProfile(null)
    setSession(null)
  }

  return (
    <Ctx.Provider value={{ session, profile, loading, signUp, signIn, signOut }}>
      {children}
    </Ctx.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(Ctx)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

export function formatKSh(n: number): string {
  return `KSh ${n.toLocaleString('en-KE')}`
}

export function timeAgo(iso: string): string {
  const secs = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (secs < 60) return 'just now'
  if (secs < 3600) return `${Math.floor(secs / 60)}m ago`
  if (secs < 86400) return `${Math.floor(secs / 3600)}h ago`
  return `${Math.floor(secs / 86400)}d ago`
}
